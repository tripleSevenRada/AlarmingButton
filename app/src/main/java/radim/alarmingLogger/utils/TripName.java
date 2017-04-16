package radim.alarmingLogger.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by radim on 20.1.17.
 */

public class TripName {

    public String getTripName(){

        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        return date.format(currentLocalTime);

    }

}
