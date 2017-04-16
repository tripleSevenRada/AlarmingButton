package radim.alarmingLogger.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import java.util.Calendar;
import radim.alarmingLogger.IOnTickListener;

/**
 * Created by radim on 9.9.16.
 */
public class StopWatchSimple implements IOnTickListener{

    private long timeItStarted;
    private Calendar cal;
    private long now;
    private TextView tv;
    private SharedPreferences myPreferences;
    private SharedPreferences.Editor myEditor;

    public StopWatchSimple(TextView tv, long timeItStarted, Context myContext){

        this.timeItStarted = timeItStarted;
        this.tv = tv;
        this.cal = Calendar.getInstance();
        this.now = cal.getTimeInMillis();

        myPreferences = myContext.getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        myEditor = myPreferences.edit();

        if(!myPreferences.contains("lastTimestamp"))storeTimestamp("00:00:00");

    }
    /**
     *
     *
     *
     */
    public String getCurrent(){

        String hoursS;
        String minsS;
        String secsS;

        long elapsed = now-timeItStarted;
        if(elapsed<0)elapsed = 0;
        long elapsedSec = elapsed/1000;

        //int days = (int) elapsedSec/(60*60*24);
        //if( days>=1 ) elapsedSec = elapsedSec - (days*(60*60*24));

        int hours = (int) elapsedSec/(60*60);

        if(hours<10) hoursS = "0"+String.valueOf(hours);else hoursS = String.valueOf(hours);

        int remn = (int) elapsedSec%(60*60);

        int min = remn/60;

        if(min<10) minsS = "0"+String.valueOf(min);else minsS = String.valueOf(min);

        int sec = remn%60;

        if(sec<10) secsS = "0"+String.valueOf(sec);else secsS = String.valueOf(sec);

        //return String.valueOf(elapsedSec);
        return hoursS+":"+minsS+":"+secsS;
    }
    /**
     *
     *
     *
     */
    @Override
    public void onTickReceived(){

        cal.clear();
        cal = Calendar.getInstance();
        now = cal.getTimeInMillis();
        String timestamp = getCurrent();
        tv.setText(timestamp);
        storeTimestamp(timestamp);

    }
    /**
     *
     *
     *
     */
    public String getLastStoredTimestamp(){return myPreferences.getString("lastTimestamp","00:00:00");}
    private void storeTimestamp(String timestamp){

        myEditor.putString("lastTimestamp",timestamp);
        myEditor.commit();

    }

}
