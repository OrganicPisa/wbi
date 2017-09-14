package com.broadcom.wbi.service.resource.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public interface ResourceReportService {

    DateTimeFormatter dfmt1 = DateTimeFormat.forPattern("MM/dd/yy");
    DateTimeFormatter dfmt2 = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
    DateTimeFormatter dfmt3 = DateTimeFormat.forPattern("MMM-yy");
    DateTimeFormatter dfmt4 = DateTimeFormat.forPattern("MMM dd, yyyy HH:mm:ss");
    DateTimeFormatter dfmt5 = DateTimeFormat.forPattern("E MMM dd HH:mm:ss Z yyyy");
    DateTimeFormatter dfmt6 = DateTimeFormat.forPattern("yyyy-MM-dd");

    ObjectMapper mapper = new ObjectMapper();
    String[] tArr = {"Actual", "Current", "Plan"};
    String[] eArr = {"End", "Start"};

    List getCurrentReportGroupStatus(String group_by, Boolean isCSGOnly, String employeeType);

    TreeMap generateTrendGroupChart(String groupBy, String intervalGroup, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    TreeMap getTrend1GroupStatus(String groupBy, String intervalGroup, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    TreeMap getTrend2GroupStatus(String groupBy1, String groupBy2, String intervalGroup, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    TreeMap getTrend3GroupStatus(String groupBy1, String groupBy2, String groupBy3, String intervalGroup, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    Set<String> getDistinctValue(String colName, DateTime startDate, DateTime stopDate, Boolean isCSGOnly);

    LinkedHashMap getProjectReportGroupStatus(String intervalGroup, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    LinkedHashMap getProjectSkillReportGroupStatus(String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    TreeMap getActualEmployeeData(DateTime fromdt, DateTime todt, Boolean isCSGOnly);
}
