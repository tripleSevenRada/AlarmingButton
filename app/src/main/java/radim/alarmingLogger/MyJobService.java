package radim.alarmingLogger;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import radim.alarmingLogger.position.DaoSession;
import radim.alarmingLogger.position.Track;
import radim.alarmingLogger.position.Trackpoint;
import radim.alarmingLogger.position.TrackpointDao;
import radim.alarmingLogger.utils.Notif;
import radim.alarmingLogger.utils.TripName;

public final class MyJobService extends JobService implements LocationListener, IgpsConstants {

    /*To implement a Job, extend the JobService class and implement the onStartJob and onStopJob.
    If the job fails for some reason, return true from
    on the onStopJob to restart the job. The onStartJob is performed in the main thread,
    if you start asynchronous processing in this method, return true otherwise false.*/

    private static final String TAG = "Logger:MJOBSER";
    private static boolean play = true;
    private volatile LocationManager lm = null;
    private final Object locationGuard = new Object();
    private volatile Location currentLocation;//worker thread reads
    private volatile Context myContext;
    private volatile Set<Trackpoint> candidates;
    private JobParameters fjp;
    private volatile TrackpointDao trackpointDao;
    private volatile Track track;
    private volatile SharedPreferences prefs;
    private volatile SharedPreferences.Editor edit;

    public Location getCurrentLocation() {
        synchronized (locationGuard) {
            return currentLocation;
        }
    }

    public void setCurrentLocation(Location currentLocation) {
        synchronized (locationGuard) {
            this.currentLocation = currentLocation;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

        myContext = this.getApplication().getApplicationContext();

        prefs = myContext.getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        edit = prefs.edit();

        DaoSession daoSession = ((GPSLogger) getApplication()).getDaoSession();
        trackpointDao = daoSession.getTrackpointDao();
        track = new Track(trackpointDao, myContext);

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (lm == null) return;


        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATES_REQ_GPS_JOBSER, 2, this);
                Log.i(TAG, "lm is providerEnabled GPS");
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_REQ_NET_JOBSER, 2, this);
                Log.i(TAG, "lm is providerEnabled NET");
            }
        } catch (SecurityException se) {
            handleSE(se);
        }


    }

    @Override
    public boolean onStartJob(JobParameters params) {

        if (lm == null) return false;
        long delta = System.currentTimeMillis() - prefs.getLong("last_known_add",0);
        if (delta < MIN_INTERVAL_BETWEEN_ADDING_TRACKPOINT) return false;

        this.fjp = params;

        Log.i(TAG, "onStartJob");
        Log.e("paramsON_START_JOB", fjp.toString());
        Log.e("paramsON_START_JOB", params.toString());
        Log.e("paramsID_ON_START_JOB", String.valueOf(params.getJobId()));

        candidates = new HashSet<>();

        AsyncLocationListener as = new AsyncLocationListener();
        as.execute();

        return true;//asynchronous processing here

        /*If the return value is false, the system assumes that whatever task has run
        did not take long and is done by the time the method returns.
        If the return value is true, then the system assumes that
        the task is going to take some time and the burden falls on you,
        the developer, to tell the system when the given task is complete
        by calling jobFinished(JobParameters params, boolean needsRescheduled).*/

    }

    @Override
    public boolean onStopJob(JobParameters params) {

        Log.i(TAG, "onStopJob");

        if (lm == null) return false;

        doStopIt();

        //If the job fails for some reason, return true
        //from on the onStopJob to restart the job.

        return false;

        /*onStopJob(JobParameters params) is used by the system to cancel
        pending tasks when a cancel request is received. It's important to note that
        if onStartJob(JobParameters params) returns false, the system assumes
        there are no jobs currently running when a cancel request is received.
        In other words, it simply won't call onStopJob(JobParameters params).*/

    }

    private void doStopIt() {

        if (lm == null) return;

        try {

            lm.removeUpdates(this);

        } catch (SecurityException se) {
            se.printStackTrace();
            handleSE(se);
        } finally {
            lm = null;
        }

        setCurrentLocation(null);

    }

    /**
     * only called once from worker thread
     */
    private Trackpoint getBestCandidate(Set<Trackpoint> candidates) {

        float minAccuracy = Float.MAX_VALUE;
        Trackpoint best = null;

        for (Trackpoint candidate : candidates) {

            if (candidate.getAccuracy() <= minAccuracy) {
                minAccuracy = candidate.getAccuracy();
                best = candidate;
            }

        }
        return best;
    }

    /**
     *
     *
     */
    private void handleSE(Exception e) {
        Log.e(TAG, "SECURITY EXCEPTION IN MY JOB SERVICE");
        e.printStackTrace();
    }


    //-----------------------------------------------------------------------------------


    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "--------location changed");

        setCurrentLocation(new Location(location));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "------------------------ON STATUS CHANGED provider: " + provider + " STATUS " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "------------------------PROVIDER ENABLED " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "------------------------PROVIDER DISABLED " + provider);
    }

    /**
     *
     *
     *
     *
     *
     *
     *
     *
     */
    private final class AsyncLocationListener extends AsyncTask<Void, Void, Void> {

        long trackSizePreExecute;

        @Override
        protected void onPreExecute() {
            trackSizePreExecute = trackpointDao.count();//track.getTrack().size();
        }

        @Override
        protected Void doInBackground(Void... params) {

            MediaPlayer mp1 = null;

            try {

                if (play) {
                    try {

                        // You can always access the context from different Thread as long as you are not changing something
                        // and you only retrieve resources through the context I don't see a problem with Thread-safety.
                        // The problem is that the context will stay in memory and active as long as the Thread runs.
                        // This is a good thing for you because you can rely on having a valid context all the time.
                        // The bad thing is that if you pass an Activity as a context all the views and member variables
                        // from this activity will also stay in memory and this can lead to a very late garbage collection
                        // for a lot of memory

                        mp1 = MediaPlayer.create(myContext, R.raw.button3);
                        mp1.start(); // no need to call prepare(); create() does that for you

                    } catch (Exception e) {
                        //TODO read about
                    }
                }

                for (int i = 0; i < HOW_LONG_TO_TRY; i++) {

                    if (getCurrentLocation() != null) break;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                }//possible timeout

                for (int i = 0; i < HOW_LONG_TO_RECORD; i++) {

                    if (getCurrentLocation() != null) {
                        Trackpoint t = new Trackpoint(getCurrentLocation(), myContext);
                        candidates.add(t);
                        if (t.getAccuracy() < ACCURACY_TO_BREAK_RECORDING) break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                }

                if (mp1 != null) {
                    mp1.stop();
                    mp1.reset();
                    mp1.release();
                    mp1 = null;
                }

                Log.i(TAG, "candidates size " + candidates.size());

                Trackpoint best = getBestCandidate(candidates);
                if (best != null && best.getAccuracy() < ACCURACY_ABOVE_DO_NOT_ADD) {

                    track.addTrackpoint(best);
                    edit.putLong("last_known_add", System.currentTimeMillis());
                    edit.commit();

                    if (prefs.getString("tripName", "").equals(getString(R.string.defTripName))) {

                        TripName tn = new TripName();
                        edit.putString("tripName", tn.getTripName());
                        edit.commit();

                    }

                } else Log.i(TAG, "no best candidate...\n");

            } finally {

                //myContext = null;

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

            Log.e("paramsJOB_FINISHED", fjp.toString());
            Log.e("paramsID_ON_JOB_FINISH", String.valueOf(fjp.getJobId()));

            if(trackpointDao.count() != trackSizePreExecute){

                Notif n = new Notif(myContext,
                        getString(R.string.notifTitle),
                        getString(R.string.notifMessage),
                        getString(R.string.notifTicker),
                        trackpointDao);
                n.raiseNotif();

            }

            doStopIt();
            jobFinished(fjp, false);

            fjp = null;
            myContext = null;

        }
    }
}
