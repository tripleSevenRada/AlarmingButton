package radim.alarmingLogger;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class OnRebootBroadcastListener extends BroadcastReceiver {

    private static final String TAG = "Logger:BL";
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    public static final String UNDEF = "undefined";

    @Override
    public void onReceive(Context context, Intent intent) {

        JobScheduler mJobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        List<JobInfo> info = mJobScheduler.getAllPendingJobs();

        for (JobInfo i : info) {

            int id = i.getId();
            mJobScheduler.cancel(id);
            Log.i(TAG, "canceling or at least trying to cancel" + i.toString());

        }

        mJobScheduler = null;

        if (!("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))) return;

        Log.i(TAG, "_______!!_______OnRebootListener onReceive context " + context.toString());

        Calendar cal = Calendar.getInstance();
        Date d = cal.getTime();
        String session = d.toString();

        sp = context.getSharedPreferences("myPreferencesOnReboot", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString("session", session);
        editor.commit();

    }

    public static String getSession(Context context) {

        //Log.i(TAG, "_______!!_______OnRebootListener getSession context "+context.toString());

        sp = context.getSharedPreferences("myPreferencesOnReboot", Context.MODE_PRIVATE);

        //Log.i(TAG, "_______!!_______OnRebootListener getSession sp "+sp.toString());

        String session = sp.getString("session", UNDEF);
        Log.i(TAG, "session returned from OnRebootBroadcastListener " + session);

        return session;
    }

}