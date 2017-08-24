package com.broadcom.wbi.util;

public class ProjectConstant {
    public static enum EnumHeadlineStage {
        PLANNING, DESIGN, FABRICATION, VER_QUAL, PRE_PRODUCTION, PRA,
        SUSTAINING, EXECUTION, SOFTWARE, SERDES, CANCELLED, INACTIVE, CUSTOMER, NON_ACTIVE
    }

    ;

    public static enum EnumIndicatorEndingDateType {
        END, START
    }

    ;

    public static enum EnumIndicatorTrackingDateType {
        ACTUAL, CURRENT, PLAN
    }

    ;

    public static enum EnumLinkCategory {
        LINK, MEETING
    }

    ;

    public static enum EnumPermissionType {
        ADMIN, VIEW, IPM, CPM, PM, SWPM, IPPM;
    }

    ;

    public static enum EnumProgramStatus {
        ACTIVE, NON_ACTIVE, CANCELLED
    }

    ;

    public static enum EnumProgramType {
        CHIP, CUSTOMER, IP, SOFTWARE
    }

    ;

    public static enum EnumCSPQueryConnector {
        AND, OR
    }

    ;

    public static enum EnumCSPQueryType {
        INTERNAL, EXTERNAL
    }

    ;

    public static enum EnumIndicatorStatus {
        GREEN, ORANGE, RED, BLACK, GREY, YELLOW
    }

    ;

    public static enum EnumResourceProgramClassificationType {
        PROJECT, REPORT
    }

    ;

    public static enum CacheTimeout {
        MONTH(2592000), WEEK(604800), DAY(86400), HALF_DAY(43200),
        SIX_HOUR(21600), THREE_HOUR(10800), HOUR(3600),
        MINUTE(60), NO_CACHE(0);
        private int second;

        private CacheTimeout(int second) {
            this.second = second;
        }

        public int getSecond() {
            return second;
        }
    }


}
