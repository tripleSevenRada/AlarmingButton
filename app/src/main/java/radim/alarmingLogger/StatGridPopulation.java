package radim.alarmingLogger;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import radim.alarmingLogger.position.Track;
import radim.alarmingLogger.position.TrackpointDao;

/**
 * Created by radim on 10.9.16.
 */
public class StatGridPopulation implements IOnTickListener {

    private GridView v;
    private String[] myStringArray;
    private String w;
    private Context mContext;
    //private static final String TAG="Logger:MA";

    private Track track;

    private final NumberFormat formatter;

    public StatGridPopulation(Context mContext, GridView v, TrackpointDao t){

        this.mContext = mContext;
        this.v = v;
        this.w = mContext.getResources().getString(R.string.whitespace);
        this.track = new Track(t,mContext);

        myStringArray = new String[4];

        formatter = new DecimalFormat("#0.00");

    }

    @Override
    public void onTickReceived() {//this method does get called from main thread

        myStringArray[0] = mContext.getResources().getString(R.string.stat1)+w+String.valueOf(track.getTrackSize());
        myStringArray[1] = mContext.getResources().getString(R.string.stat2)+w+formatter.format(track.getLength())+w+mContext.getResources().getString(R.string.km);
        myStringArray[2] = mContext.getResources().getString(R.string.stat3)+w+formatter.format(track.getAvgSpeed())+w+mContext.getResources().getString(R.string.kmh);
        myStringArray[3] = mContext.getResources().getString(R.string.stat4)+w+formatter.format(track.getMaxAltitude())+w+mContext.getResources().getString(R.string.mnm);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                R.layout.custom_text_view_cell, myStringArray);

        v.setAdapter(adapter);

    }
}
