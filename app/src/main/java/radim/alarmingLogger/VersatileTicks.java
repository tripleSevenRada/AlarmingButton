package radim.alarmingLogger;

import android.os.Handler;
import android.util.Log;

/**
 * Created by radim on 9.9.16.
 */

public class VersatileTicks{

    private IOnTickListener otl;
    private long intervalInMilis;
    private Handler handler = new Handler();
    private boolean killer = false;

    public VersatileTicks(IOnTickListener otl, long intervalInMilis ){
        this.otl = otl;
        this.intervalInMilis = intervalInMilis;
    }

    private Runnable ticks = new Runnable() {

        @Override
        public void run() {
            try {
                otl.onTickReceived();
            } finally {
                // 100% guarantee that this always happens
                if(!killer) {
                    handler.postDelayed(ticks, intervalInMilis);
                }
            }
        }

    };

    public void startTicks(){
        Log.i("HANDLER","Start");
        killer = false;
        ticks.run();
    }

    public void stopTicks(){
        Log.i("HANDLER","Remove");
        killer = true;
        handler.removeCallbacks(ticks);
    }

    public void setInterval(long newInterval){
        this.intervalInMilis = newInterval;
    }

}
