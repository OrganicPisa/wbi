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

    TreeMap getCurrentReportGroup(List<String> charged_from, List<String> charged_to, String group_by);

    TreeMap getTrendReport1Group(List<String> charged_from, List<String> charged_to, String groupBy, String interval, DateTime startDate, DateTime stopDate);

    TreeMap getTrendReport2Group(List<String> charged_from, List<String> charged_to, String groupBy1, String groupBy2, String interval, DateTime startdt, DateTime stopdt);

    TreeMap getTrendReport3Group(List<String> charged_from, List<String> charged_to, String groupBy1, String groupBy2, String groupBy3, String interval, DateTime startdt, DateTime stopdt);

    TreeMap getProjectReportGroup(List<String> list, String interval, DateTime fromdt, DateTime todt);

    List<Map> getActualEmployeeData(List<String> charged_from, List<String> charged_to, DateTime startdt, DateTime stopdt);

    Set<String> getDistinctValue(List<String> charged_from, String colName, DateTime startDate, DateTime stopDate);

}
