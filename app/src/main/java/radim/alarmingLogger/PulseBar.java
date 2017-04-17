package radim.alarmingLogger;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class PulseBar implements IOnTickListener{

    private TextView tv;
    private char ch = 0x25A0;//full square
    private int length = 10;
    private int interval = 600;
    private VersatileTicks vt;

    private int roller = 0;

    public PulseBar(TextView tv, char ch, int length){

        this.tv = tv;
        this.ch = ch;
        this.length = length;
        this.vt = new VersatileTicks(this, (long) interval);

    }

    public void setInterval(int interval){

        if(interval > 100 && interval < 1400){
            this.interval = interval;
            vt.setInterval((long)interval);
        }

    }

    public void startTicks(){

        roller = 0;
        vt.startTicks();

    }

    public void stopTicks(){

        vt.stopTicks();
        tv.setText("");

    }
    @Override
    public void onTickReceived(){

        updateStatus(roller);
        roller++;
        if(roller == length) roller = 0;

    }

    private StringBuilder sb = new StringBuilder();

    public void updateStatus(int roller){

        if(roller == 0) sb.delete(0,sb.length());else sb.append(ch);

        tv.setText(sb.toString());

    }

}

