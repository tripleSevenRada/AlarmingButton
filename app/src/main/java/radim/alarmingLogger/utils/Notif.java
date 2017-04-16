package radim.alarmingLogger.utils;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import radim.alarmingLogger.MainActivity;
import radim.alarmingLogger.R;
import radim.alarmingLogger.position.Track;
import radim.alarmingLogger.position.TrackpointDao;


/**
 * Created by radim on 14.9.16.
 */


public class Notif {

    private Context myContext;
    private String title;
    private String message;
    private String ticker;
    private TrackpointDao trackpointDao;

    public Notif(Context myContext, String title, String message, String ticker, TrackpointDao t){

        this.myContext = myContext;
        this.title = title;
        this.message = message;
        this.ticker = ticker;
        this.trackpointDao = t;

    }
    /**
     *
     *
     *
     */
    public void raiseNotif(){

        Track track = new Track(trackpointDao, myContext);

        Intent intent = new Intent(myContext, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(myContext,1010, intent, 0);

        Notification n = new NotificationCompat.Builder(myContext)
        .setContentTitle(title)
        .setContentText(message+myContext.getResources().getString(R.string.whitespace)+String.valueOf(track.getTrackSize()))
        .setTicker(ticker)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_room_white_24dp)
        .setOngoing(false)
        .setContentIntent(pIntent)
                .build();

        NotificationManager mNotifyMgr =
                (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //n.flags |= Notification.FLAG_AUTO_CANCEL;

        // Builds the notification and issues it.
        mNotifyMgr.notify(0, n);
    }
}
