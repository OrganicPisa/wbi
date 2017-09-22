package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.ResourcePlanSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public interface ResourcePlanSearchService extends CRUDService<ResourcePlanSearch> {

    DateTimeFormatter dfmt = DateTimeFormat.forPattern("yyyy-MM-dd");

    HashMap<String, TreeMap<String, Double>> groupByMonthSkill(RevisionSearch rs, String ptype);

    TreeMap<String, Double> groupByMonth(RevisionSearch rs, String ptype);

    Set<String> getDistinctValue(RevisionSearch rs, String ptype, String colName);

    void deleteByTime(DateTime dt);

    void deleteByPlanType(RevisionSearch rs, String ptype);

    Set<String> getDistinctValue(RevisionSearch rs, String colName);

}
