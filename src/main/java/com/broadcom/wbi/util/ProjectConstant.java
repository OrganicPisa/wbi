package com.broadcom.wbi.util;

public class ProjectConstant {
    public enum EnumHeadlineStage {
        PLANNING, DESIGN, FABRICATION, VER_QUAL, PRE_PRODUCTION, PRA,
        SUSTAINING, EXECUTION, SOFTWARE, SERDES, CANCELLED, INACTIVE, CUSTOMER, NON_ACTIVE
    }

    public enum EnumIndicatorEndingDateType {
        END, START
    }

    public enum EnumIndicatorTrackingDateType {
        ACTUAL, CURRENT, PLAN
    }

    public enum EnumLinkCategory {
        LINK, MEETING
    }

    public enum EnumPermissionType {
        ADMIN, VIEW, IPM, CPM, PM, SWPM, IPPM
    }

    public enum EnumProgramStatus {
        ACTIVE, NON_ACTIVE, CANCELLED
    }

    public enum EnumProgramType {
        CHIP, CUSTOMER, IP, SOFTWARE
    }

    public enum EnumCSPQueryConnector {
        AND, OR
    }

    public enum EnumCSPQueryType {
        INTERNAL, EXTERNAL
    }

    public enum EnumIndicatorStatus {
        GREEN, ORANGE, RED, BLACK, GREY, YELLOW
    }

    public enum EnumResourceProgramClassificationType {
        PROJECT, REPORT
    }

    public enum CacheTimeout {
        MONTH(2592000), WEEK(604800), DAY(86400), HALF_DAY(43200),
        SIX_HOUR(21600), THREE_HOUR(10800), HOUR(3600),
        MINUTE(60), NO_CACHE(0);
        private int second;

        CacheTimeout(int second) {
            this.second = second;
        }

        public int getSecond() {
            return second;
        }
    }


}
