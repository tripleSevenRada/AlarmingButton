package radim.alarmingLogger.position;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import radim.alarmingLogger.IConstants;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Trackpoint extends Location implements IConstants {

    //Interface that must be implemented and provided as a public CREATOR field
    //that generates instances of your Parcelable class from a Parcel.

    @Transient
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Trackpoint createFromParcel(Parcel in) {
            return new Trackpoint(in);
        }

        public Trackpoint[] newArray(int size) {
            return new Trackpoint[size];
        }
    };

    @Transient
    private static final String NAME_OF_PROVIDER = "GPSLoggerApp";


    //##############################################################
    //##############################################################
    //##############################################################

    @Property(nameInDb = "TIME")
    private String timestamp;
    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "LAT")
    private double latitude;
    @Property(nameInDb = "LON")
    private double longitude;
    @Property(nameInDb = "ALT")
    private volatile double altitude;//possibly 0.0 as no altitude
    @Property(nameInDb = "ACCUR")
    private float accuracy;

    //##############################################################
    //##############################################################
    //##############################################################
    @Transient
    private StringBuilder sb = new StringBuilder();

    //private double lat;//all locations created by LocationManager include valid lat
    //private double lon;//all locations created by LocationManager include valid lon
    //private float accur;//all locations created by LocationManager include accuracy

    @Transient
    private Context c = null;

    /**
     *C
     * @return
     */
    public Trackpoint(Location loc, Context c) {
        super(loc);
            Log.i("DEBUG TRCKPT", "constr2");
            double alt = loc.getAltitude();
            this.c = c;
            keepTrackOfAltitude(alt);
            Timestamp tmstp = new Timestamp(System.currentTimeMillis());
            String stamp = tmstp.toString();
            stamp = stamp.replace(" ", "T");
            stamp = stamp+"Z";
            this.timestamp = stamp;
    }

    /**
     *C
     * @return
     */
    public Trackpoint(Parcel in) {

        super(new Location(NAME_OF_PROVIDER));
        Log.i("DEBUG TRCKPT", "constr3");

        initSuper(15.0, 50.0, 500.0, (float) 123.0, 1234000);
        keepTrackOfAltitude(500);

    }

    /**
     *C
     * @return
     */
    @Keep
    public Trackpoint(String timestamp, Long id, double latitude, double longitude, double altitude,
            float accuracy) {

        super(new Location(NAME_OF_PROVIDER));
        initSuper(latitude,longitude,altitude,accuracy,System.currentTimeMillis());

        this.timestamp = timestamp;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        
    }

    /**
     *C
     * @return
     */
    @Keep
    public Trackpoint() {
        super(new Location(NAME_OF_PROVIDER));
    }

    /**
     *
     * @return
     */
    public String getTimeStamp() {
        return String.valueOf(this.timestamp);
    }

    /**
     *
     * @return
     */
    private void initSuper(double lat, double lon, double altitude, float accur, long elapsedRealTimeNanos) {

        super.setLatitude(lat);
        super.setLongitude(lon);
        super.setAccuracy(accur);
        super.setElapsedRealtimeNanos(elapsedRealTimeNanos);

    }



    //@@@@@@@if(!myPreferences.contains("default_alt")){setDefAlt(DEF_ALT);}
    //@@@@@@@if(!myPreferences.contains("last_known_alt")){setLastKnownAlt(DEF_ALT);}

    /**
     *
     * @return
     */
    private synchronized void keepTrackOfAltitude(double alt) {

        if (c == null) {
            Log.i("DEBUG TRCKPT", "context == null");
            return;
        }

        SharedPreferences pref = c.getSharedPreferences("myPreferences", Context.MODE_PRIVATE);

        if (alt != 0.0) {
            SharedPreferences.Editor edit = pref.edit();
            edit.putFloat("last_known_alt", (float) alt);
            edit.commit();
            Log.i("DEBUG TRCKPT", "inserting last known alt " + (float) alt + " / " + alt);
            this.altitude = alt;
        } else {
            this.altitude = pref.getFloat("last_known_alt", (float) DEF_ALT);
            super.setAltitude(this.altitude);
            Log.i("DEBUG TRCKPT", "setting altitude " + this.altitude);
        }

    }

    /**
     *
     * @return
     */
    public String toString() {
        sb.delete(0, sb.length());
        sb.append(String.valueOf(getLatitude())).append("\n")
                .append(String.valueOf(getLongitude())).append("\n")
                .append(String.valueOf(getAltitude())).append("\n")
                .append(String.valueOf(getAccuracy())).append("\n")
                .append(getTimeStamp()).append("\n")
                .append(String.valueOf(getId()));
        return sb.toString();
    }

    /**
     *
     * @return
     */
    public double getLatitude() {
        return super.getLatitude();
    }
    /**
     *
     * @return
     */
    public void setLatitude(double latitude) {
        super.setLatitude(latitude);
    }
    /**
     *
     * @return
     */
    public double getLongitude() {
        return super.getLongitude();
    }
    /**
     *
     * @return
     */
    public void setLongitude(double longitude) {
        super.setLongitude(longitude);
    }
    /**
     *
     * @return
     */
    public double getAltitude() {
        return this.altitude;
    }
    /**
     *
     * @return
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    /**
     *
     * @return
     */
    public float getAccuracy() {
        return super.getAccuracy();
    }
    /**
     *
     * @return
     */
    public void setAccuracy(float accuracy) {
        super.setAccuracy(accuracy);
    }
    /**
     *
     * @return
     */
    public Long getId() {
        return id;
    }
    /**
     *
     * @return
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     *
     * @return
     */
    public String getTimestamp() {
        return this.timestamp;
    }
    /**
     *
     * @return
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    /**
     *
     *
     *
     *
     *
     *
     * @return String representing trackpoint as xml entity
     */
    public String getAsXmlLine() {

        return "<trkpt lat=\"" + f6.format(getLatitude()) + "\" lon=\"" + f6.format(getLongitude()) +
                "\">\n<ele>" + f2.format(getAltitude()) + "</ele>\n<time>"+getTimeStamp()+"</time>\n</trkpt>";

    }

    @Transient
    private NumberFormat f6 = new DecimalFormat("#0.00000000");
    @Transient
    private NumberFormat f2 = new DecimalFormat("#0.00");

}