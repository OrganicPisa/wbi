package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.ResourceActualSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;

import java.util.*;

@SuppressWarnings("rawtypes")
public interface ResourceActualSearchService {

    DateTimeFormatter dfmt = DateTimeFormat.forPattern("yyyy-MM-dd");

    void emptyData();

    ResourceActualSearch findOne(String id);

    Iterator<ResourceActualSearch> findAll();

    DeleteQuery deleteByTime(DateTime dt);

    TreeMap<String, Double> groupByMonth(RevisionSearch rs);

    HashMap<String, TreeMap<String, Double>> groupByMonthSkill(RevisionSearch rs);

    Set<String> getProgramDistinctValue(RevisionSearch rs, String colName);

    List<Map> findByProgram(RevisionSearch rs, DateTime dt);

    DateTime getLastUpdateTime(RevisionSearch rs);

    /********************************************************************************
     *
     * Reporting
     *
     ********************************************************************************/

    TreeMap getCurrentReportGroup(String group_by, Boolean isCSGOnly, String employeeType);

    TreeMap getTrendReport1Group(String groupBy, String interval, DateTime startDate, DateTime stopDate, Boolean isCSGOnly, String employeeType);

    TreeMap getTrendReport2Group(String groupBy1, String groupBy2, String interval, DateTime startdt, DateTime stopdt, Boolean isCSGOnly, String employeeType);

    TreeMap getTrendReport3Group(String groupBy1, String groupBy2, String groupBy3, String interval, DateTime startdt, DateTime stopdt, Boolean isCSGOnly, String employeeType);

    TreeMap getProjectReportGroup(List<String> list, String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    TreeMap getProjectSkillReportGroup(List<String> list, String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType);

    List<Map> getActualEmployeeData(DateTime startdt, DateTime stopdt, Boolean isCSGOnly);

    Set<String> getDistinctValue(String colName, DateTime startDate, DateTime stopDate, Boolean isCSGOnly);

}
