package com.broadcom.wbi.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class CSSColorUtil {

    public static String convertColorClassToCSSString(String cl) {
        if (cl.toLowerCase().indexOf("black") != -1) {
            return "color:rgb(0, 0, 0); font-weight:bold;";
        } else if (cl.toLowerCase().indexOf("green") != -1) {
            return "color:rgb(0, 128, 0); font-weight:bold;";
        } else if (cl.toLowerCase().indexOf("orange") != -1) {
            return "color:rgb(255, 165, 0); font-weight:bold;";
        } else if (cl.toLowerCase().indexOf("red") != -1) {
            return "color:rgb(255, 0, 0); font-weight:bold;";
        } else if (cl.toLowerCase().indexOf("grey") != -1) {
            return "color:rgb(128, 128, 128)";
        }
        return "";
    }

    public static String getColorClass(DateTime dt, String cl) {
        try {
            if (dt.isBeforeNow())
                cl = "grey";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cl;
    }

    public static String getColorClass(String ds, String cl) {
        DateTimeFormatter dfmt = DateTimeFormat.forPattern("MM/dd/yy");
        if (ds.trim().isEmpty())
            return "";
        else if (ds.trim().matches("[a-zA-Z]+")) {
            return "grey";
        }
        try {
            DateTime dt = dfmt.parseDateTime(ds);
            if (dt.isBeforeNow())
                return "grey";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cl;

    }
}
