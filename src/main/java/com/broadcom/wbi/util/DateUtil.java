package com.broadcom.wbi.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateUtil {

    public static String toString(Date d) {
        DateTimeFormatter dfmt = DateTimeFormat.forPattern("MM/dd/yy");
        try {
            DateTime dt = new DateTime(d);
            if (dt.getYear() == 1980 || dt.getYear() == 1979)
                return "&nbsp;";
            else if (dt.getYear() == 1950 || dt.getYear() == 1949)
                return "NA";
            else if (dt.getYear() == 1960 || dt.getYear() == 1959)
                return "TBD";
            else if (dt.getYear() == 1970 || dt.getYear() == 1969)
                return "DONE";
            else {
                return dt.toString(dfmt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "&nbsp;";
    }

    public static String toString(String ds) {
        if (ds.contains("80") || ds.contains("79")) {
            return "";
        } else if (ds.contains("70") || ds.contains("69")) {
            return "Done";
        } else if (ds.contains("60") || ds.contains("59")) {
            return "TBD";
        } else if (ds.contains("50") || ds.contains("49")) {
            return "NA";
        }
        return ds;
    }

    public static DateTime toDate(String ds) {
        DateTimeFormatter dfmt = DateTimeFormat.forPattern("MM/dd/yy");
        if (ds.trim().isEmpty()) {
            return new DateTime().withYear(1980).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
        } else if (ds.toLowerCase().indexOf("done") != -1) {
            return new DateTime().withYear(1970).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
        } else if (ds.toLowerCase().indexOf("tbd") != -1) {
            return new DateTime().withYear(1960).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
        } else if (ds.toLowerCase().indexOf("na") != -1) {
            return new DateTime().withYear(1950).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
        }
        return dfmt.parseDateTime(ds);
    }

}
