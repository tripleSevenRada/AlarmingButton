package radim.alarmingLogger.utils;

import android.graphics.Color;

public final class AccuracyFieldColor {

    private AccuracyFieldColor(){}

    public static int findColorBasedOnAccuracy(int accuracy, int limit){
        int myAccuracy = accuracy;
        if (myAccuracy == 0) myAccuracy = 1;
        if (myAccuracy>limit) myAccuracy = limit-1;
        int half = limit/2;
        double coef = (double)255/half;
        int red; int green;
        if(myAccuracy>=half) red = 255; else{
            red = (int)(myAccuracy*coef);
        }
        if(myAccuracy<=half) green = 255;else{
            green = (int)(255-((myAccuracy-half)*coef));
        }
        return Color.rgb(red,green,0);
    }

}
