package radim.alarmingLogger;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import radim.alarmingLogger.logging.LogEntry;
import radim.alarmingLogger.logging.LogEntryDao;
import radim.alarmingLogger.logging.LogToFile;
import radim.alarmingLogger.logging.DaoSession;
import radim.alarmingLogger.position.Track;
import radim.alarmingLogger.position.Trackpoint;
import radim.alarmingLogger.position.TrackpointDao;
import radim.alarmingLogger.storage.StorageChecks;
import radim.alarmingLogger.utils.AccuracyFieldColor;
import radim.alarmingLogger.utils.FragmentAlertDialog;
import radim.alarmingLogger.utils.StopWatchSimple;
import radim.alarmingLogger.utils.TripName;

public class MainActivity extends AppCompatActivity implements LocationListener,
        IgpsConstants, IConstants, IPermConstants, ILocalBroadcastConstants, IAlertDialogTypes {

    private static final String TAG = "Logger:MA";
    private static final long REFRESH_STATS = 3000;
    private static final char STATUS_BAR_CHAR = '\u2588';//full block
    private static final int LIMIT_THAT_MAKES_ACCURACY_FIELD_RED = 550;
    ProgressBar progress;
    private int defaultInt = 5;
    private int minInt = 1;
    private int maxInt = (20 - minInt);
    private SharedPreferences myPreferences;
    private SharedPreferences.Editor myEditor;
    private SeekBar sb;
    private TextView tvSeekBar;
    private TextView tvPulseBar;
    private PulseBar pb;
    private Button startB;
    private Button stopB;
    private Button stopAfterRebootWhileRunning;
    private StatGridPopulation sgp;
    private VersatileTicks vtStat;
    private StringBuilder sbRecycled;
    private LocationManager lm;
    private Location currentLocation = null;
    private JobScheduler mJobScheduler;
    private Calendar cal;
    private boolean enabledGPS = false;
    private boolean enabledNET = false;
    private int currentInterval;

    private StopWatchSimple sws;
    private VersatileTicks vt;
    private TextView tvStopWatch;
    private TrackpointDao trackpointDao;
    private LogEntryDao logEntryDao;

    private LogToFile logging;

    private TextView report;
    private TextView output;

    private Export e;

    /**
     *
     *
     *
     */
    @Override
    protected void onNewIntent(Intent i) {}

    /**
     *
     *
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (myToolbar != null) {
            myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        }

        int api = Build.VERSION.SDK_INT;

        if (api >= 24) {
            minInt = 15;
            maxInt = (45 - minInt);
            defaultInt = 20;
        }

        currentInterval = defaultInt;
        progress = (ProgressBar) (findViewById(R.id.progressB));
        if (progress != null) {
            progress.setVisibility(View.INVISIBLE);
        }


        // ------------------------------------------------------------------------ ASK FOR PERMISSION


        if (!permLocIsGranted() || !permWriteIsGranted()) requestPermissions();


        Log.i(TAG, "Session: " + OnRebootBroadcastListener.getSession(this.getApplication().getApplicationContext()));

        myPreferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        myEditor = myPreferences.edit();

        if (!myPreferences.contains("firstTime")) {

            setRunningStatus(false);
            setInterval(defaultInt);
            setTimeOfStart();
            setSessionWhenStarted(OnRebootBroadcastListener.UNDEF);
            setDefAlt(DEF_ALT);
            setLastKnownAlt(DEF_ALT);

            //stats
            myEditor.putFloat("length", 0f);
            myEditor.putLong("last_known_time", 0);
            myEditor.putLong("first_known_time", 0);
            myEditor.putFloat("max_alt", 0f);
            myEditor.putLong("last_id", 0);
            //stats

            myEditor.putString("tripName", getString(R.string.defTripName));
            myEditor.putInt("starts", 0);
            myEditor.putInt("id", 0);

            //min interval between adding trackpoint
            myEditor.putLong("last_known_add", 0);

            myEditor.putBoolean("firstTime", true);

            myEditor.apply();
        }


        int starts = myPreferences.getInt("starts", 0);
        myEditor.putInt("starts", starts + 1);

        myEditor.commit();


        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Intent i = new Intent();
            String packageName = getPackageName();

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            if (pm.isIgnoringBatteryOptimizations(packageName)) {

                Log.i("BATTERY", "IS_IGNORING_OPTI");

                if (starts % 4 == 0)
                    i.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);

            } else {

                Log.i("BATTERY", "IS_NOT_IGNORING_OPTI");

                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + packageName));
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            }

            // Verify that the intent will resolve to an activity
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            }

        }
        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION
        // BATTERY OPTIMIZATION


        startB = (Button) findViewById(R.id.button1);
        stopB = (Button) findViewById(R.id.button2);
        stopAfterRebootWhileRunning = new Button(this);

        DaoSession daoSession = ((GPSLogger) getApplication()).getDaoSession();
        trackpointDao = daoSession.getTrackpointDao();
        logEntryDao = daoSession.getLogEntryDao();
        logging = new LogToFile(logEntryDao);

        String currentSession = OnRebootBroadcastListener.getSession(this.getApplication().getApplicationContext());
        if ((!currentSession.equals(getSessionWhenStarted())) && getRunningStatus()) {// REBOOT WHILE RUNNING

            mJobScheduler = (JobScheduler)                                            // in case persisted(false) does not work
                    getSystemService(Context.JOB_SCHEDULER_SERVICE);
            mJobScheduler.cancelAll();

            List<JobInfo> pendingJobs = mJobScheduler.getAllPendingJobs();

            for (JobInfo info : pendingJobs) {

                int id = info.getId();

                String message = "JOB_AFTER_REBOOT" + info.toString();
                Log.wtf("JOB_AFTER_REBOOT", message);

                logging.addEntry(new LogEntry(message));

                mJobScheduler.cancel(id);

            }

            mJobScheduler = null;

            stopJob(stopAfterRebootWhileRunning);

            doWarningAlert(getResources().getString(R.string.title2),
                    getResources().getString(R.string.message2),
                    getResources().getString(R.string.ok));

        }

        GridView gv = (GridView) findViewById(R.id.gridview);

        sb = (SeekBar) findViewById(R.id.seekBar);
        if (sb != null) {
            sb.setMax(maxInt);
        }

        sbRecycled = new StringBuilder();

        tvSeekBar = (TextView) findViewById(R.id.textViewSeekBar);
        tvSeekBar.setText(concatTvSeekBar());
        tvPulseBar = (TextView) findViewById(R.id.textViewPulseBar);
        if (tvPulseBar != null) {
            tvPulseBar.setTextSize(8);
        }
        tvStopWatch = (TextView) findViewById(R.id.textViewStopWatch);

        if (tvStopWatch != null) {
            tvStopWatch.setText("00:00:00");
        }

        report = (TextView) findViewById(R.id.reportTV);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (report.getText().equals("") ||
                        report.getText().equals(getString(R.string.nothingToWrite)) ||
                        report.getText().equals(getString(R.string.externalStorageNotAccesed)))
                    return;

                requestPermissions();

            }
        });

        output = (TextView) findViewById(R.id.outputTV);
        output.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (output.getText().equals("") ||
                        output.getText().equals(getString(R.string.writeNotOk))) return;

                StorageChecks checks = new StorageChecks();

                if (checks.isExternalStorageReadable()) {

                    Uri myDirUri = Uri.parse(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() +
                            File.separator + APP_DIR);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(myDirUri, "resource/folder");

                    if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {

                        startActivity(Intent.createChooser(intent, getString(R.string.fileExplorerChoserTitleEXP)));

                        return;

                    } else {

                        toastThis(getString(R.string.noFileExp));

                        return;
                        // if you reach this place, it means there is no any file
                        // explorer app installed on your device
                    }
                }
            }
        });

        sgp = new StatGridPopulation(this, gv, trackpointDao);
        sgp.onTickReceived();

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                          @Override
                                          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                              currentInterval = progress + minInt;
                                              tvSeekBar.setText(concatTvSeekBar());

                                          }

                                          @Override
                                          public void onStartTrackingTouch(SeekBar seekBar) {
                                          }

                                          @Override
                                          public void onStopTrackingTouch(SeekBar seekBar) {

                                              if (getRunningStatus()) scheduleOrRescheduleJob();
                                              //Log.i("DEBUG", "ON SEEKBAR " + currentInterval);

                                          }
                                      }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbaroptions, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location_settings:
                // User chose the "Settings" item, show the app settings UI...

                Intent doSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(doSettings);

                return true;

            case R.id.action_settings:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...

                toastThis("implement me!");

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     *
     *
     *
     */
    @Override
    protected void onResume() {

        super.onResume();

        Log.i(TAG, "onResume");

        Log.e("DEBUG_JOBS","===============================================");
        listAllPendingJobs();
        Log.e("DEBUG_JOBS","===============================================");

        putReporterToWarn();
        reportPermStatus();

        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_alert");
        if (frag != null) manager.beginTransaction().remove(frag).commit();
        manager = null;

        currentInterval = getInterval();
        sb.setProgress(currentInterval - minInt);

        tvSeekBar.setText(concatTvSeekBar());

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        initLocStatus();
        setLocationTVs(null);

        mJobScheduler = (JobScheduler)
                getSystemService(Context.JOB_SCHEDULER_SERVICE);

        pb = new PulseBar(tvPulseBar, STATUS_BAR_CHAR, 16);
        disableOrEnableStatusBar();
        disableOrEnableButtonsAndIndicators();

        if (getRunningStatus()) {

            sws = new StopWatchSimple(tvStopWatch, getTimeOfStart(), this);//onTickListener
            vt = new VersatileTicks(sws, 1000);
            vt.startTicks();

            vtStat = new VersatileTicks(sgp, REFRESH_STATS);
            vtStat.startTicks();

        } else {

            StopWatchSimple watchToGetLastTimestamp = new StopWatchSimple(tvStopWatch, 1000, this);
            String restoredTimestamp = watchToGetLastTimestamp.getLastStoredTimestamp();
            tvStopWatch.setText(restoredTimestamp);

        }
    }

    /**
     *
     *
     *
     */
    @Override
    protected void onPause() {

        super.onPause();

        Log.i(TAG, "onPause");

        setInterval(currentInterval);

        try {
            lm.removeUpdates(this);
        } catch (SecurityException se) {
            //handleSE();
        }

        lm = null;

        pb.stopTicks();
        pb = null;

        if (vt != null) {

            vt.stopTicks();
            vt = null;

        }

        if (sws != null) {

            sws = null;

        }

        if (vtStat != null) {

            vtStat.stopTicks();
            vtStat = null;

        }

        currentLocation = null;

    }

    /**
     *
     *
     *
     */
    @Override
    protected void onStop() {

        super.onStop();

        Log.i(TAG, "onStop");

    }

    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################

    /**
     *
     *
     *
     */
    @Override
    protected void onDestroy() {

        Log.i(TAG, "onDestroy");

        super.onDestroy();

    }

    /**
     *
     *
     *
     */
    public void startJob(View v) {

        if (getRunningStatus()) return;

        reportPermStatus();
        clearOutputTv();

        Track track = new Track(trackpointDao, this);

        setSessionWhenStarted(OnRebootBroadcastListener.getSession(this.getApplication().getApplicationContext()));

        track.reset();
        logging.reset();

        toastThis(getString(R.string.statusRunning));

        if (currentLocation != null && currentLocation.getAccuracy() < ACCURACY_ABOVE_DO_NOT_ADD) {

            track.addTrackpoint(new Trackpoint(currentLocation, this));
            TripName name = new TripName();
            myEditor.putString("tripName", name.getTripName());
            myEditor.apply();

        } else {

            toastThis(getString(R.string.begNotAccur));

        }

        Log.i(TAG, "startJob");
        logging.addEntry(new LogEntry("__startJob from MA"));

        scheduleOrRescheduleJob();

        setRunningStatus(true);
        disableOrEnableStatusBar();
        Log.i(TAG, "__________LISTING ALL PENDING JOBS FROM START JOB");
        listAllPendingJobs();

        disableOrEnableButtonsAndIndicators();

        setTimeOfStart();

        sws = new StopWatchSimple(tvStopWatch, getTimeOfStart(), this);
        vt = new VersatileTicks(sws, 1000);
        vt.startTicks();

        vtStat = new VersatileTicks(sgp, REFRESH_STATS);
        vtStat.startTicks();

    }

    /**
     *
     */
    private void scheduleOrRescheduleJob() {

        Log.i(TAG, "=========================listing all pending jobs from scheduleOrReschedule before cancelAll: ");
        listAllPendingJobs();

        if (mJobScheduler != null) { mJobScheduler.cancelAll(); }
            else {
            mJobScheduler = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE );
            mJobScheduler.cancelAll();
            doWarningAlert("scheduleOrRescheduleJob","mJobScheduler == null","OK");
            }

        Log.i(TAG, "=========================listing all pending jobs from scheduleOrReschedule after cancelAll: ");
        listAllPendingJobs();

        int currId = myPreferences.getInt("id", 0);
        myEditor.putInt("id", currId + 1);
        myEditor.commit();


        JobInfo.Builder builder = new JobInfo.Builder(myPreferences.getInt("id", 0),
                new ComponentName(getPackageName(),
                        MyJobService.class.getName()));

        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
        builder.setPersisted(false);                      //PERSISTED
        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
        builder.setPeriodic( (long) ((currentInterval * 60 * 1000)*1) ); //MINUTES
        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

        if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {

            //If something goes wrong
            doWarningAlert("ScheduleOrRescheduleJob", "problem return value <= 0", "OK");

        }

        Log.i(TAG, "=========================listing all pending jobs from scheduleOrReschedule after schedule/reschedule: ");
        listAllPendingJobs();
    }

    /**
     *
     *
     *
     */
    private void disableOrEnableButtonsAndIndicators() {

        if (getRunningStatus()) {
            startB.setEnabled(false);
            stopB.setEnabled(true);
        } else {
            startB.setEnabled(true);
            stopB.setEnabled(false);
        }

        TextView gps = (TextView) findViewById(R.id.textViewGPSProv);
        TextView net = (TextView) findViewById(R.id.textViewNETProv);

        if (gps != null && net != null) {
            if (!enabledNET && !enabledGPS) {

                startB.setEnabled(false);

                gps.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                net.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                setProvidersIndicators(enabledGPS, enabledNET);
            } else {

                gps.setTextColor(ContextCompat.getColor(this, R.color.gray1));
                net.setTextColor(ContextCompat.getColor(this, R.color.gray1));
                setProvidersIndicators(enabledGPS, enabledNET);
            }
        }

    }

    /**
     *
     *
     *
     */
    private void disableOrEnableStatusBar() {

        if (pb == null) return;

        if (getRunningStatus()) {
            setIntervalTicks();
            pb.startTicks();
        } else {
            pb.stopTicks();
        }

    }

    /**
     *
     *
     *
     */
    private void setIntervalTicks() {

        if (pb == null) return;

        float accuracy = 700;
        if (currentLocation != null && currentLocation.hasAccuracy())
            accuracy = currentLocation.getAccuracy();
        int accuracyToSetAsInterval = (int) (100 + (Math.floor(accuracy)));
        pb.setInterval(accuracyToSetAsInterval);

    }

    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################
    //######################################################################################

    /**
     *
     *
     *
     */
    private void listAllPendingJobs() {

        if (mJobScheduler == null) {
            mJobScheduler = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE );
        }

        List<JobInfo> pendingJobs = mJobScheduler.getAllPendingJobs();

        Log.i(TAG, "____________________listing all pending jobs");
        for (JobInfo info : pendingJobs) {

            Log.i(TAG, "____________________JobInfo__________" + info.toString());
            Log.i(TAG, "____________________JobInfo_period_________" + info.getIntervalMillis());
            Log.i(TAG, "____________________JobInfo_periodic_________" + info.isPeriodic());
            Log.i(TAG, "____________________JobInfo_persisted_________" + info.isPersisted());

        }
    }

    /**
     *
     *
     *
     */
    public void stopJob(View v) {

        if (!getRunningStatus()) return;

        Track track = new Track(trackpointDao, this);

        if (currentLocation != null && currentLocation.getAccuracy() < ACCURACY_ABOVE_DO_NOT_ADD) {
            track.addTrackpoint(new Trackpoint(currentLocation, this));

            if (myPreferences.getString("tripName", "").equals(getString(R.string.defTripName))) {

                TripName tn = new TripName();
                myEditor.putString("tripName", tn.getTripName());
                myEditor.apply();

            }

            sgp.onTickReceived();

        } else {

            toastThis(getString(R.string.endNotAccur));

        }

        Log.i(TAG, "stopJob");
        logging.addEntry(new LogEntry("__stopJob from MA"));

        Log.i(TAG, "__________LISTING ALL PENDING JOBS FROM STOP JOB BEFORE cancelAll");
        listAllPendingJobs();

        if (mJobScheduler != null) { mJobScheduler.cancelAll(); }
            else {
                mJobScheduler = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE );
                mJobScheduler.cancelAll();
                doWarningAlert("stopJob","mJobScheduler == null","OK");
            }// null == case called stopJob after reboot when running

        setRunningStatus(false);
        disableOrEnableStatusBar();

        Log.i(TAG, "__________LISTING ALL PENDING JOBS FROM START JOB AFTER cancelAll");
        listAllPendingJobs();

        Button b = (Button) v;

        if (b != stopAfterRebootWhileRunning) toastThis(getString(R.string.statusStopped));

        disableOrEnableButtonsAndIndicators();

        if (vt != null) vt.stopTicks();//null == case called stopJob after reboot when running

        if (vtStat != null)
            vtStat.stopTicks();//null == case called stopJob after reboot when running

        //###############################################################
        //###############################################################
        //###############################################################

        //export

        if (!permWriteIsGranted() || !permLocIsGranted()) {
            return;
        }

        StorageChecks sch = new StorageChecks();
        boolean possible = sch.isExternalStorageWritable();

        if (!possible) {
            //putReporterToWarn();
            report.setText(getString(R.string.externalStorageNotAccesed));
            return;
        }

        if (track.getTrack().size() == 0) {
            //putReporterToWarn();
            report.setText(getString(R.string.nothingToWrite));
            return;
        }

        List<Trackpoint> data = track.getTrack();
        List<LogEntry> log = logging.getLog();

        e = new Export(data, log, myPreferences.getString("tripName",
                getString(R.string.defTripName)), this, progress);

        startB.setEnabled(false);

        e.doWriteExportFile();

        //export

        //###############################################################
        //###############################################################
        //###############################################################

    }

    void finalizeOutputTv(boolean finishedOK) {

        if (finishedOK) {
            putOutputToNormal();
            output.setText(e.getPathToFilenameWritten());
            startB.setEnabled(true);
        } else {
            putOutputToWarn();
            output.setText(getString(R.string.writeNotOk));
            startB.setEnabled(true);
        }

    }

    /**
     *
     *
     */
    private boolean getRunningStatus() {
        return myPreferences.getBoolean("status", false);
    }

    /**
     *
     *
     */
    private void setRunningStatus(boolean status) {
        myEditor.putBoolean("status", status);
        myEditor.commit();
    }

    /**
     *
     */
    private int getInterval() {
        return myPreferences.getInt("interval", 10);
    }

    /**
     *
     */
    private void setInterval(int interval) {

        if (interval < minInt) interval = minInt;
        if (interval > maxInt) interval = maxInt;
        myEditor.putInt("interval", interval);
        myEditor.commit();

    }

    /**
     *
     *
     */
    private void setTimeOfStart() {

        if (cal != null) cal.clear();
        cal = Calendar.getInstance();
        long milis = cal.getTimeInMillis();

        myEditor.putLong("timeOfStart", milis);
        myEditor.commit();

    }

    private void setDefAlt(double def) {

        myEditor.putFloat("def_alt", (float) def);
        myEditor.commit();

    }

    private void setLastKnownAlt(double def) {

        myEditor.putFloat("last_known_alt", (float) def);
        myEditor.commit();

    }

    /**
     *
     *
     *
     */
    private String getSessionWhenStarted() {
        return myPreferences.getString("sessionWhenStarted", OnRebootBroadcastListener.UNDEF);
    }

    /**
     *
     *
     */
    private void setSessionWhenStarted(String session) {

        myEditor.putString("sessionWhenStarted", session);
        myEditor.commit();

    }

    /**
     *
     *
     */
    private long getTimeOfStart() {

        return myPreferences.getLong("timeOfStart", System.nanoTime());

    }

    /**
     *
     *
     */
    private String concatTvSeekBar() {

        sbRecycled.delete(0, sbRecycled.length());
        sbRecycled.append(getString(R.string.interval))
                .append(getString(R.string.whitespace))
                .append(currentInterval)
                .append(getString(R.string.whitespace))
                .append(getString(R.string.min));
        return sbRecycled.toString();

    }

    /**
     *
     *
     */
    private void toastThis(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //LOCATION LISTENER
    private void initLocStatus() {

        enabledGPS = false;
        enabledNET = false;

        if (lm != null) enabledGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (lm != null) enabledNET = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        try {
            if (enabledGPS)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATES_REQ_GPS, 2, this);
            if (enabledNET)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_REQ_NET, 2, this);
        } catch (SecurityException se) {
            //handleSE();
        }
        setProvidersIndicators(enabledGPS, enabledNET);

    }

    private void setProvidersIndicators(boolean gpsFlag, boolean netFlag) {

        TextView gpsTv = (TextView) findViewById(R.id.textViewGPSProv);
        TextView netTv = (TextView) findViewById(R.id.textViewNETProv);

        if (gpsTv != null) {
            if (gpsFlag) gpsTv.setText(getString(R.string.gps) +
                    getString(R.string.whitespace) +
                    getString(R.string.enabled));
            else gpsTv.setText(getString(R.string.gps) +
                    getString(R.string.whitespace) +
                    getString(R.string.disabled));
        }
        if (netTv != null) {
            if (netFlag) netTv.setText(getString(R.string.net) +
                    getString(R.string.whitespace) +
                    getString(R.string.enabled));
            else netTv.setText(getString(R.string.net) +
                    getString(R.string.whitespace) +
                    getString(R.string.disabled));
        }
    }

    //
    //
    @Override
    public void onLocationChanged(Location location) {

        currentLocation = new Location(location);

        setLocationTVs(currentLocation);
        setIntervalTicks();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "------------------------ON STATUS CHANGED provider: " + provider + " STATUS " + status);
        initLocStatus();
        disableOrEnableButtonsAndIndicators();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "------------------------PROVIDER ENABLED " + provider);
        initLocStatus();
        disableOrEnableButtonsAndIndicators();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "------------------------PROVIDER DISABLED " + provider);
        initLocStatus();
        disableOrEnableButtonsAndIndicators();
    }

    /**
     *
     */
    private void setLocationTVs(Location location) {

        TextView lat = (TextView) findViewById(R.id.textViewLat);
        TextView lon = (TextView) findViewById(R.id.textViewLon);
        TextView provUsed = (TextView) findViewById(R.id.textViewProvUsed);
        TextView accur = (TextView) findViewById(R.id.textViewAccur);

        String latS = getString(R.string.lat);
        String lonS = getString(R.string.lon);
        String w = getString(R.string.whitespace);

        if (location == null && lat != null && lon != null && provUsed != null && accur != null) {
            lat.setText(latS + w + getString(R.string.noFix));
            lon.setText(lonS + w + getString(R.string.noFix));
            provUsed.setText(getString(R.string.provUsed) + w + getString(R.string.noFix));
            accur.setTextColor(Color.RED);
            accur.setText(getString(R.string.accur) + w + getString(R.string.noFix));
            tvPulseBar.setTextColor(AccuracyFieldColor.findColorBasedOnAccuracy(LIMIT_THAT_MAKES_ACCURACY_FIELD_RED, LIMIT_THAT_MAKES_ACCURACY_FIELD_RED));
            return;
        }

        if (lat != null && lon != null && provUsed != null) {
            if (location != null) {
                lat.setText(latS + w + String.format(Locale.ENGLISH, "%.6f", location.getLatitude()));
            }
            if (location != null) {
                lon.setText(lonS + w + String.format(Locale.ENGLISH, "%.6f", location.getLongitude()));
            }
            if (location != null) {
                provUsed.setText(getString(R.string.provUsed) + w +
                        location.getProvider());
            }
        }
        if (location != null && location.hasAccuracy() && accur != null) {
            int color = AccuracyFieldColor.findColorBasedOnAccuracy((int) location.getAccuracy(), LIMIT_THAT_MAKES_ACCURACY_FIELD_RED);
            tvPulseBar.setTextColor(color);
            accur.setTextColor(color);
            accur.setText(getString(R.string.accur) + w +
                    String.format(Locale.ENGLISH, "%.2f", location.getAccuracy()) + w + getString(R.string.metres));
        } else if (accur != null) {
            tvPulseBar.setTextColor(AccuracyFieldColor.findColorBasedOnAccuracy(LIMIT_THAT_MAKES_ACCURACY_FIELD_RED, LIMIT_THAT_MAKES_ACCURACY_FIELD_RED));
            accur.setTextColor(Color.MAGENTA);
            accur.setText(getString(R.string.accur) + w + getString(R.string.accurUnknown));
        }

    }

    /**
     *
     */
    private void doWarningAlert(
            String title,
            String message,
            String ok
    ) {

        Bundle handedIn = new Bundle();

        handedIn.putString("title", title);
        handedIn.putString("message", message);
        handedIn.putString("ok", ok);

        FragmentManager manager = getFragmentManager();

        FragmentAlertDialog fad = new FragmentAlertDialog();
        fad.setArguments(handedIn);
        fad.show(manager, "fragment_warning");

        manager = null;

    }

    private boolean permLocIsGranted() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_BOTH);
    }

    private boolean permWriteIsGranted() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void clearOutputTv() {
        output.setText("");
    }

    private void reportPermStatus() {

        String reporter = "";

        if (!permLocIsGranted()) {
            //putReporterToWarn();
            reporter = getString(R.string.permLocNotGranted);
        }

        if (!permWriteIsGranted()) {
            //putReporterToWarn();
            if (reporter.length() > 0) reporter += "\n";
            reporter += getString(R.string.permWriteNotGranted);
        }

        report.setText(reporter);

    }

    //REPORTER

    private void putReporterToWarn() {
        report.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    //OUTPUT

    private void putOutputToWarn() {
        output.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    private void putOutputToNormal() {
        output.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

}
