package com.broadcom.wbi.util;

import org.joda.time.DateTime;

public class DateResetUtil {

    public static DateTime getResetDate(DateTime dt) {
        int dayOfWeek = 2;
        if (dt != null) {
            DateTime temp_dtts = dt.withDayOfWeek(dayOfWeek).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
            if (temp_dtts.getMillis() > dt.getMillis()) {
                dt = temp_dtts.minusWeeks(1);
            } else {
                dt = temp_dtts;
            }
        } else {
            dt = new DateTime().withDayOfWeek(dayOfWeek).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
            DateTime currentdt = new DateTime();
            if (dt.getMillis() > currentdt.getMillis()) {
                dt = dt.minusWeeks(1);
            }
        }
        return dt;
    }

    public static boolean checkSameDateReset() {
        DateTime lastTuesdaydt = getResetDate(null);
        DateTime currentdt = new DateTime();
        long diff = (currentdt.getMillis() - lastTuesdaydt.getMillis()) / (1000 * 60 * 60);
        if (diff < 24) {
            return true;
        }
        return false;
    }

    public static boolean isResetProgramStatus(DateTime dt, DateTime lastWeek) {
        DateTime resetDate = null;
        if (lastWeek != null)
            resetDate = getResetDate(lastWeek);
        else
            resetDate = getResetDate(null);
        if (dt.getMillis() < resetDate.getMillis())
            return true;
        return false;
    }
}
