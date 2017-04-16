package radim.alarmingLogger.position;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import radim.alarmingLogger.R;

public class Track extends AppCompatActivity {

    private static final String TAG = "Logger:TRACK";
    private final TrackpointDao trackpointDao;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor myEditor;
    private final Context c;

    /**
     *
     *
     */
    public Track(TrackpointDao tDao, Context c) {

        this.trackpointDao = tDao;
        this.c = c;

        prefs = c.getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        myEditor = prefs.edit();

    }

    /**
     *
     */
    public void addTrackpoint(Trackpoint t) {//called from worker thread

        synchronized (trackpointDao) {

            Log.i(TAG, "___________________________________adding trackpoint");
            Log.i(TAG, t.toString());

            float currAlt = (float) t.getAltitude();
            float maxAlt = prefs.getFloat("max_alt", 0);
            if (currAlt > maxAlt) {
                myEditor.putFloat("max_alt", currAlt);
            }

            if (trackpointDao.count() > 0) {

                long lastId = prefs.getLong("last_id", 0);

                Trackpoint tLast = trackpointDao.loadByRowId(lastId);

                float dist = t.distanceTo(tLast);
                float distSoFar = prefs.getFloat("length", 0);
                myEditor.putFloat("length", dist + distSoFar);

            } else {
                myEditor.putLong("first_known_time", t.getTime());
            }

            myEditor.putLong("last_known_time", t.getTime());

            //-----------------------------------------------------
            trackpointDao.insert(t);
            //-----------------------------------------------------

            myEditor.putLong("last_id", t.getId());

            myEditor.commit();

        }
    }

    public List<Trackpoint> getTrack() {

        synchronized (trackpointDao) {
            return trackpointDao.loadAll();
        }
    }

    public void reset() {
        synchronized (trackpointDao) {
            trackpointDao.deleteAll();
        }
        myEditor.putFloat("length", 0f);
        myEditor.putLong("last_known_time", 0l);
        myEditor.putLong("first_known_time", 0l);
        myEditor.putFloat("max_alt", 0f);
        myEditor.putLong("last_id", 0l);
        myEditor.putString("tripName", this.c.getString(R.string.defTripName));
        myEditor.commit();

    }

    public int getTrackSize() {
        synchronized (trackpointDao) {
            return (int) trackpointDao.count();
        }
    }

    public double getAvgSpeed() {

        long start = prefs.getLong("first_known_time", 0l);
        //Log.e("DBG_AVG","start "+start);
        long last = prefs.getLong("last_known_time", 0l);
        //Log.e("DBG_AVG","last "+last);
        long delta = last - start;
        //Log.e("DBG_AVG","delta "+delta);
        if (delta == 0l) return 0l;
        double hours = ((double) delta) / 1000 / 60 / 60;
        //Log.e("DBG_AVG","hours "+hours);

        return getLength() / hours;

    }

    public double getLength() {

        float length = prefs.getFloat("length", 0f);

        return (double) length / 1000;
    }

    public double getMaxAltitude() {

        float max = prefs.getFloat("max_alt", 0f);

        return (double) max;

    }

}
