package com.broadcom.wbi.service.indicator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IndicatorService {
    DateTimeFormatter dfmt = DateTimeFormat.forPattern("MM/dd/yy");

    DateTimeFormatter dfmt2 = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");

    DateTime emptydt = new DateTime().withYear(1980).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
    DateTime tbddt = new DateTime().withYear(1960).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
    DateTime nadt = new DateTime().withYear(1950).dayOfYear().withMinimumValue().withTimeAtStartOfDay();

    DateTime checkEmptydt = new DateTime().withYear(2003).withMonthOfYear(3).withDayOfMonth(3).withTimeAtStartOfDay();
    DateTime checkTBDdt = new DateTime().withYear(2002).withMonthOfYear(2).withDayOfMonth(2).withTimeAtStartOfDay();
    DateTime checkNAdt = new DateTime().withYear(2001).dayOfYear().withMinimumValue().withTimeAtStartOfDay();

    String[] chipKeyMilestones = {"pc", "t/o", "eng sample", "qual complete", "pra"};

    HashMap<String, String> internalProjectFixedMilestone = new HashMap<String, String>() {
        {
            put("ca", null);
            put("pc", null);
            put("first rtl", null);
            put("rtl freeze", null);
            put("t/o", null);
            put("eng sample", null);
            put("lead customer sample", null);
            put("split sample", null);
            put("sdk bringup", null);
            put("sv nominal complete", null);
            put("respin t/o target", null);
            put("respin decision", null);
            put("dvt complete", null);
            put("qual complete", null);
            put("sdk bringup", null);
            put("sdk ga", null);
            put("lead customer fcs", null);
            put("pra", null);
        }
    };

    ObjectMapper mapper = new ObjectMapper();

    /***************************************************************************
     *
     * Get Indicator Milestone
     *
     ***************************************************************************/

    List getIndicatorByCategory(int gid, DateTime dt);

    Map<String, String> getKeyProjectDate(int rid);

    /***************************************************************************
     *
     * Get Front page Indicator Milestone
     *
     ***************************************************************************/

    List getFrontPageMilestone(int rid);

    List getFrontPageMilestone(List hm);

    HashMap getFrontPageRevisionInfo(int rid);

    /***************************************************************************
     *
     * Get SOFTWARE indicator
     *
     ***************************************************************************/

    List getSWHeadlineList(int pid);

}
