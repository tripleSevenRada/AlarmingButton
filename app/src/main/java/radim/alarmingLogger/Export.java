package radim.alarmingLogger;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import radim.alarmingLogger.position.Trackpoint;

import static radim.alarmingLogger.IConstants.APP_DIR;

class Export implements IPermConstants {

    private static final String TAG = "EXPORT";
    private final List<Trackpoint> data;
    private final String name;
    private final MainActivity activity;
    private volatile ProgressBar progress;

    //--------------------------------------------

    Export(List<Trackpoint> data, String name, MainActivity activity, ProgressBar progress) {
        this.data = data;
        this.name = name;
        this.activity = activity;
        this.progress = progress;
    }

    //--------------------------------------------

    void doWriteExportFile() {

        //_______________________________________________________________________________________

//        Log.i(TAG,"################################# EXPORT ################################");
//        Log.i(TAG,"#########################################################################");
//        Log.i(TAG,"#########################################################################");
//        Log.i(TAG,"#########################################################################");
//
//        for(Trackpoint t : data) Log.i(TAG,t.toString());
//
//        Log.i(TAG,"#########################################################################");
//        Log.i(TAG,"#########################################################################");
//        Log.i(TAG,"#########################################################################");
//        Log.i(TAG,"#########################################################################");

        //_______________________________________________________________________________________

//        AsyncWriter aw = new AsyncWriter();
//        aw.execute();

        executeInPar();

    }

    /**
     *
     *
     *
     *
     */
    String getPathToFilenameWritten() {
        if (trip == null) return "WTF??";
        try {
            return trip.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "IO ERROR";
        }
    }

    private volatile File trip;

    //_____________________________________________
    private final Handler mHandler = new Handler();
    private volatile int count = 1;
    //_____________________________________________

    /**
     *
     *
     *
     *
     */
    private void executeInPar() {

        //onPreExecute
        progress.setIndeterminate(false);
        progress.setMax(data.size());
        progress.setVisibility(View.VISIBLE);

        //doInBackground

        Runnable paralel = new Runnable() {
            @Override
            public void run() {

                try {

                    File dir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS), APP_DIR);
                    if (dir.mkdirs()) Log.i(TAG, dir.toString() + " created");

                    if (!dir.exists()) postExecute(-1);

                    trip = new File(dir, name + ".gpx");

                    FileOutputStream fos = new FileOutputStream(trip);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                    bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                    bw.newLine();

                    bw.write("<gpx creator=\"gps_logger\" version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\" " +
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                            "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
                    bw.newLine();

                    bw.write("<trk>");
                    bw.newLine();

                    bw.write("<name>" + name + "</name>");
                    bw.newLine();

                    bw.write("<trkseg>");
                    bw.newLine();

                    for (Trackpoint t : data) {
                        bw.write(t.getAsXmlLine());
                        bw.newLine();

                        /* For every 10 th increment, sent a message to UI thread to increment the progressbar */
                        if (count % 10 == 0)
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progress.setProgress(count);
                                    count++;
                                }
                            });

                    }

                    bw.write("</trkseg>");
                    bw.newLine();

                    bw.write("</trk>");
                    bw.newLine();

                    bw.write("</gpx>");
                    bw.newLine();

                    bw.flush();
                    bw.close();

                } catch (Exception e) {
                    postExecute(-1);
                }
                postExecute(10);
            }// void run
        };

        Thread t = new Thread(paralel);
        t.start();

        //onPostExecute

    }

    /**
     * @param result result value called once from worker thread
     */
    private void postExecute(final int result) {

        /* send to UI thread */
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(0);
                progress.setVisibility(View.INVISIBLE);
                activity.finalizeOutputTv(result == 10);
            }
        });

    }

//================================= ALTERNATIVE ==========================================
//========================================================================================
//========================================================================================
//========================================================================================
//========================================================================================
//========================================================================================
//========================================================================================
//========================================================================================

    /**
     *
     *
     *
     *
     */
    private class AsyncWriter extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progress.setIndeterminate(false);
            progress.setMax(data.size());
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... p) {
            progress.setProgress(p[0]);
        }

        @Override
        protected Integer doInBackground(Void... params) {

            Log.i(TAG, "################################# EXPORT ################################");
            Log.i(TAG, "#########################################################################");
            Log.i(TAG, "#########################################################################");
            Log.i(TAG, "#########################################################################");

            for (Trackpoint t : data) Log.i(TAG, t.toString());

            Log.i(TAG, "#########################################################################");
            Log.i(TAG, "#########################################################################");
            Log.i(TAG, "#########################################################################");
            Log.i(TAG, "#########################################################################");

            try {

                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), APP_DIR);
                if (dir.mkdirs()) Log.i(TAG, dir.toString() + " created");

                if (!dir.exists()) return -1;

                trip = new File(dir, name + ".gpx");

                FileOutputStream fos = new FileOutputStream(trip);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                bw.newLine();

                bw.write("<gpx creator=\"gps_logger\" version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
                bw.newLine();

                bw.write("<trk>");
                bw.newLine();

                bw.write("<name>" + name + "</name>");
                bw.newLine();

                bw.write("<trkseg>");
                bw.newLine();

                int count = 1;

                for (Trackpoint t : data) {
                    bw.write(t.getAsXmlLine());
                    bw.newLine();
                    publishProgress(count);
                    count++;
                }

                bw.write("</trkseg>");
                bw.newLine();

                bw.write("</trk>");
                bw.newLine();

                bw.write("</gpx>");
                bw.newLine();

                bw.flush();
                bw.close();

            } catch (Exception e) {
                return -1;
            }
            return 10;
        }

        @Override
        protected void onPostExecute(Integer param) {
            progress.setProgress(0);
            progress.setVisibility(View.INVISIBLE);
            activity.finalizeOutputTv(param == 10);
        }
    }
}
