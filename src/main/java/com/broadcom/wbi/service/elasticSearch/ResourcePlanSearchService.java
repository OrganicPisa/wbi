package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.ResourcePlanSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

public interface ResourcePlanSearchService {

    DateTimeFormatter dfmt = DateTimeFormat.forPattern("yyyy-MM-dd");

    ResourcePlanSearch save(ResourcePlanSearch resource);

    void saveBulk(List<ResourcePlanSearch> resources);

    void emptyData();

    ResourcePlanSearch findOne(String id);

    Iterator<ResourcePlanSearch> findAll();

    HashMap<String, TreeMap<String, Double>> groupByMonthSkill(RevisionSearch rs, String ptype);

    TreeMap<String, Double> groupByMonth(RevisionSearch rs, String ptype);

    Set<String> getDistinctValue(RevisionSearch rs, String ptype, String colName);

    void deleteByTime(DateTime dt);

    void deleteByPlanType(RevisionSearch rs, String ptype);

    Set<String> getDistinctValue(RevisionSearch rs, String colName);

}
