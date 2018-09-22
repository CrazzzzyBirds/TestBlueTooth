package com.heima.testbluetooth.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {


    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = simpleDateFormat.format(new Date());
        return dateString;
    }
}
