package com.broadcom.wbi.service.resource.program;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

@SuppressWarnings("rawtypes")
public interface ResourceProjectService {

    DateTimeFormatter dfmt1 = DateTimeFormat.forPattern("MM/dd/yy");
    DateTimeFormatter dfmt2 = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
    DateTimeFormatter dfmt3 = DateTimeFormat.forPattern("MMM-yy");
    DateTimeFormatter dfmt4 = DateTimeFormat.forPattern("MMM dd, yyyy HH:mm:ss");
    DateTimeFormatter dfmt5 = DateTimeFormat.forPattern("E MMM dd HH:mm:ss Z yyyy");
    DateTimeFormatter dfmt6 = DateTimeFormat.forPattern("yyyy-MM-dd");

    ObjectMapper mapper = new ObjectMapper();

    String[] tArr = {"Actual", "Current", "Plan"};
    String[] eArr = {"End", "Start"};


    Map getResourceSummaryTable(RevisionSearch rs, Map<String, DateTime> map);

    Map getResourceGroupByMonthChart(RevisionSearch rs, Map<String, DateTime> datehm);

    Set<String> getAllResourceSkill(RevisionSearch rs, String type);

    TreeMap<String, Double> getResourceGroupByProgramMonth(RevisionSearch rs, String type);

    HashMap getResourceSkillSummaryTable(RevisionSearch rs, Map<String, DateTime> datehm);

    Set<String> getActualTimeList(RevisionSearch rs);

    List<Map> getActualEmployeeData(RevisionSearch rs, DateTime dt);

    List<Map> getActualEmployeeData(RevisionSearch rs);

//	HashMap parseFile(RevisionSearch rs, String username, File file);


}
