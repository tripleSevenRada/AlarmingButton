package radim.alarmingLogger;

/**
 * Created by radim on 1.9.16.
 */
public interface IgpsConstants {
    int LOCATION_UPDATES_REQ_GPS = 512;
    int LOCATION_UPDATES_REQ_NET = 1024;

    int LOCATION_UPDATES_REQ_GPS_JOBSER = 256;
    int LOCATION_UPDATES_REQ_NET_JOBSER = 512;

    int HOW_LONG_TO_TRY = 60;
    int HOW_LONG_TO_RECORD = 26;

    long MIN_INTERVAL_BETWEEN_ADDING_TRACKPOINT = 6000;

    float ACCURACY_TO_BREAK_RECORDING = 26.0f;
    float ACCURACY_ABOVE_DO_NOT_ADD = 600.0f;
}
