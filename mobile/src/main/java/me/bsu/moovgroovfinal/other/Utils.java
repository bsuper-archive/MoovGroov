package me.bsu.moovgroovfinal.other;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class Utils {
    public static String convertUnixTimestampToLocalTimestampString(long unixTS) {
        Date date = new Date(unixTS);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(getUserTimezone());
        return sdf.format(date);
    }

    public static TimeZone getUserTimezone() {
        return SimpleTimeZone.getDefault();
    }
}
