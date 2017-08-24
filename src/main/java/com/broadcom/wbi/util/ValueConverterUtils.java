package com.broadcom.wbi.util;

public class ValueConverterUtils {

    public static boolean isPositiveInteger(String s) {
        try {
            int id = Integer.parseInt(s);
            if (id > 0)
                return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int convertStringToInt(String strVal, int defaultVal) {
        try {
            return Integer.parseInt(strVal);
        } catch (Exception ex) {
            return defaultVal;
        }
    }

    public static boolean convertStringToBoolean(String strVal, boolean defaultVal) {
        try {
            if (strVal.equalsIgnoreCase("true") || strVal.equals("1")) {
                return true;
            } else if (strVal.equalsIgnoreCase("false") || strVal.equals("0")) {
                return false;
            }
        } catch (Exception ex) {
        }

        return defaultVal;
    }

    public static double convertStringToDouble(String strVal, double defaultVal) {
        try {
            return Double.parseDouble(strVal);
        } catch (Exception ex) {
            return defaultVal;
        }
    }
}
