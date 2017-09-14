package com.broadcom.wbi.service.resource.program;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.SkillMapping;
import com.broadcom.wbi.service.elasticSearch.ResourceActualSearchService;
import com.broadcom.wbi.service.elasticSearch.ResourcePlanSearchService;
import com.broadcom.wbi.service.jpa.ProgramService;
import com.broadcom.wbi.service.jpa.ResourcePlanService;
import com.broadcom.wbi.service.jpa.SkillMappingService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
public class ResourceProjectServiceImpl implements ResourceProjectService {
    private final ResourcePlanService resourcePlanService;
    private final ProgramService programService;
    private final ResourcePlanSearchService resourcePlanSearchService;
    private final ResourceActualSearchService resourceActualSearchService;
    private final SkillMappingService skillMappingService;

    @Autowired
    public ResourceProjectServiceImpl(ResourcePlanService resourcePlanService, ProgramService programService, ResourcePlanSearchService resourcePlanSearchService,
                                      ResourceActualSearchService resourceActualSearchService, SkillMappingService skillMappingService) {
        this.resourcePlanService = resourcePlanService;
        this.programService = programService;
        this.resourcePlanSearchService = resourcePlanSearchService;
        this.resourceActualSearchService = resourceActualSearchService;
        this.skillMappingService = skillMappingService;
    }

    @Override
    public Set<String> getAllResourceSkill(RevisionSearch rs, String type) {
        Set<String> ret = new LinkedHashSet<String>();

        if (type.toLowerCase().indexOf("actual") == -1) {
            ret = resourcePlanSearchService.getDistinctValue(rs, type, "skill");
        } else {
            ret = resourceActualSearchService.getProgramDistinctValue(rs, "skill");
        }
        if (ret != null && ret.size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public Map getResourceSummaryTable(final RevisionSearch rs, final Map<String, DateTime> datehm) {
        final Map ret = Collections.synchronizedMap(new TreeMap());
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Set<String> types = resourcePlanSearchService.getDistinctValue(rs, "plan_type");
        if (types == null)
            types = new LinkedHashSet<String>();
        types.add("ACTUAL");
        DateTime pcdt = null;
        DateTime todt = null;
        DateTime pradt = null;
        if (datehm != null) {
            if (datehm.containsKey("pc") && datehm.get("pc") != null) {
                pcdt = datehm.get("pc").dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            }
            if (datehm.containsKey("t/o") && datehm.get("t/o") != null) {
                todt = datehm.get("t/o").dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            }
            if (datehm.containsKey("pra") && datehm.get("pra") != null) {
                pradt = datehm.get("pra").dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            } else {
                if (datehm.containsKey("eng sample") && datehm.get("eng sample") != null) {
                    pradt = datehm.get("eng sample").plusMonths(12).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                }
            }
        }
        Program program = programService.findById(rs.getProgram_id());
        if (pcdt == null) {
            Date minDate = resourcePlanService.findMinResourceDate(program);
            if (minDate == null)
                return null;
            pcdt = new DateTime(minDate);
        }
        final DateTime pc = pcdt;
        final DateTime to = todt;
        final DateTime pra = pradt;
        final DateTime currentMonthdt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        final DateTime currentdt = new DateTime();

        DateTime lastUpdatedDate = resourceActualSearchService.getLastUpdateTime(rs);
        if (lastUpdatedDate != null) {
            long diff = (currentdt.getMillis() - lastUpdatedDate.getMillis()) / (1000 * 60 * 60 * 24);
            if (diff > 13)
                ret.put("last_updated_date", "<i class='text-danger'>Updated on " + lastUpdatedDate.toString(dfmt1) + "<i>");
            else
                ret.put("last_updated_date", "<i>Updated on " + lastUpdatedDate.toString(dfmt1) + "<i>");
        }

        final HashMap datamap = new HashMap();
        for (final String type : types) {
            executor.submit(new Runnable() {
                public void run() {
                    List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> tmp_map = new HashMap<String, String>();
                    tmp_map.put("key", "name");
                    tmp_map.put("value", "<span style='font-weight:bold;' >" + type.toUpperCase() + "</span>");
                    tmp_map.put("order", "1");
                    list.add(tmp_map);
                    Double total = getTotalResourceTime(rs, null, null, type);
                    if (total != 0.0) {
                        tmp_map = new HashMap<String, String>();
                        tmp_map.put("key", "total");
                        tmp_map.put("value", "<span style='text-align:center;'>" + Double.toString(total) + "</span>");
                        tmp_map.put("order", "6");
                        list.add(tmp_map);
                        if (pc != null) {
                            Double prePC = getTotalResourceTime(rs, null, pc, type);
                            tmp_map = new HashMap<String, String>();
                            tmp_map.put("key", "prepc");
                            tmp_map.put("value",
                                    "<span style='text-align:center;'>" + Double.toString(prePC) + "</span>");
                            tmp_map.put("order", "2");
                            list.add(tmp_map);
                            if (to != null) {
                                Double pcTO = getTotalResourceTime(rs, pc, to, type);
                                tmp_map = new HashMap<String, String>();
                                tmp_map.put("key", "pcto");
                                tmp_map.put("value",
                                        "<span style='text-align:center;'>" + Double.toString(pcTO) + "</span>");
                                tmp_map.put("order", "3");
                                list.add(tmp_map);

                                tmp_map = new HashMap<String, String>();
                                if (pra != null) {
                                    Double toPRA = getTotalResourceTime(rs, to, pra.plusMonths(1), type);
                                    tmp_map = new HashMap<String, String>();
                                    tmp_map.put("key", "topra");
                                    tmp_map.put("value",
                                            "<span style='text-align:center;'>" + Double.toString(toPRA) + "</span>");
                                    tmp_map.put("order", "4");
                                    list.add(tmp_map);

                                    if (pra.isBeforeNow()) {
                                        Double postPRA = getTotalResourceTime(rs, pra.plusMonths(1), currentMonthdt,
                                                type);
                                        tmp_map = new HashMap<String, String>();
                                        tmp_map.put("key", "postpra");
                                        tmp_map.put("value", "<span style='text-align:center;'>"
                                                + Double.toString(postPRA) + "</span>");
                                        tmp_map.put("order", "5");
                                        list.add(tmp_map);
                                    } else {
                                        tmp_map = new HashMap<String, String>();
                                        tmp_map.put("key", "postpra");
                                        tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                                        tmp_map.put("order", "5");
                                        list.add(tmp_map);
                                    }
                                } // if PRA date found
                                else {
                                    tmp_map.put("key", "topra");
                                    tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                                    tmp_map.put("order", "4");
                                    list.add(tmp_map);

                                    tmp_map = new HashMap<String, String>();
                                    tmp_map.put("key", "topra");
                                    tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                                    tmp_map.put("order", "5");
                                    list.add(tmp_map);
                                }
                            } // if there is T/O
                            else {
                                tmp_map = new HashMap<String, String>();
                                tmp_map.put("key", "pcto");
                                tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                                tmp_map.put("order", "3");
                                list.add(tmp_map);

                                tmp_map = new HashMap<String, String>();
                                tmp_map.put("key", "topra");
                                tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                                tmp_map.put("order", "4");
                                list.add(tmp_map);

                                tmp_map = new HashMap<String, String>();

                                tmp_map.put("key", "topra");
                                tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                                tmp_map.put("order", "5");
                                list.add(tmp_map);
                            } // no T/O Date found
                        } // if there is PC
                        else {
                            tmp_map = new HashMap<String, String>();
                            tmp_map.put("key", "prepc");
                            tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                            tmp_map.put("order", "2");
                            list.add(tmp_map);

                            tmp_map = new HashMap<String, String>();
                            tmp_map.put("key", "pcto");
                            tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                            tmp_map.put("order", "3");
                            list.add(tmp_map);

                            tmp_map = new HashMap<String, String>();
                            tmp_map.put("key", "topra");
                            tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                            tmp_map.put("order", "4");
                            list.add(tmp_map);

                            tmp_map = new HashMap<String, String>();
                            tmp_map.put("key", "topra");
                            tmp_map.put("value", "<span style='text-align:center;'>TBD</span>");
                            tmp_map.put("order", "5");
                            list.add(tmp_map);
                        } // no pc
                        datamap.put(type, list);
                    }
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (datamap != null && datamap.size() > 0) {
            ret.put("data", datamap);
            return ret;
        }
        return null;
    }

    @Override
    public Map getResourceGroupByMonthChart(final RevisionSearch rs, final Map<String, DateTime> datehm) {
        DateTime startdt = null;
        DateTime stopdt = null;
        if (datehm != null) {
            if (datehm.containsKey("pc") && datehm.get("pc") != null) {
                startdt = datehm.get("pc").dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            }
            if (datehm.containsKey("pra") && datehm.get("pra") != null) {
                stopdt = datehm.get("pra").dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            } else {
                if (datehm.containsKey("eng sample") && datehm.get("eng sample") != null) {
                    stopdt = datehm.get("eng sample").plusMonths(12).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                }
            }
        }
        Program program = programService.findById(rs.getProgram_id());
        if (startdt == null) {
            Date minDate = resourcePlanService.findMinResourceDate(program);
            if (minDate == null)
                return null;
            startdt = new DateTime(minDate);
        }
        if (stopdt == null) {
            Date maxDate = resourcePlanService.findMaxResourceDate(program);
            if (maxDate == null)
                return null;
            else
                maxDate = new Date();
            stopdt = new DateTime(maxDate);
        }

        final DateTime currentdt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Set<String> types = resourcePlanSearchService.getDistinctValue(rs, "plan_type");
        if (types == null)
            types = new LinkedHashSet<String>();

        String foundpc = null;
        int pcnum = 1;
        String plantype = null;
        for (String t : types) {
            if (t.toLowerCase().indexOf("pc") != -1) {
                String n = t.replaceAll("[^0-9]+", "");
                if (n.length() > 0) {
                    if (pcnum == -1) {
                        foundpc = t;
                    } else {
                        if (Integer.parseInt(n) > pcnum) {
                            foundpc = t;
                        }
                    }
                }
            } else {
                plantype = t;
            }
        }
        if (foundpc == null) {
            foundpc = "PC";
        }
        if (plantype == null) {
            plantype = foundpc;
        }
        final String plan_type = plantype;
        types.add("ACTUAL");
        DateTime trackdt = startdt;
        Set<String> tlist = new TreeSet<String>();
        while (trackdt.getMillis() <= stopdt.getMillis()) {
            tlist.add(trackdt.toString(dfmt6));
            trackdt = trackdt.plusMonths(1);
        }
        if (tlist == null || tlist.isEmpty()) {
            tlist = resourceActualSearchService.getProgramDistinctValue(rs, "month");
        }
        final Set<String> timelist = tlist;
        if (types != null && !types.isEmpty()) {
            Map ret = new HashMap();
            final List series = Collections.synchronizedList(new ArrayList());
            for (final String type : types) {
                executor.submit(new Runnable() {
                    public void run() {
                        List datalist = new ArrayList();
                        HashMap hm = new HashMap();
                        hm.put("name", type.toUpperCase());
                        if (type.toLowerCase().indexOf("actual") != -1) {
                            hm.put("color", "#0000FF");
                        } else if (type.toLowerCase().indexOf("pc") != -1) {
                            hm.put("color", "#4B721D");
                            if (plan_type.toLowerCase().indexOf("pc") == -1) {
                                hm.put("visible", false);
                            }
                        } else if (type.toLowerCase().indexOf("ecr") != -1) {
                            hm.put("color", "#781D7E");
                            if (plan_type.toLowerCase().indexOf("pc") == -1
                                    && plan_type.toLowerCase().indexOf("ecr") == -1) {
                                hm.put("visible", false);
                            }
                        } else if (type.toLowerCase().indexOf("por") != -1) {
                            hm.put("color", "#7CB5EC");
                        }
                        TreeMap<String, Double> typeDBmap = getResourceGroupByProgramMonth(rs, type);
                        if (typeDBmap != null && typeDBmap.keySet().size() > 0) {
                            timeloop:
                            for (String t : timelist) {
                                if (type.toLowerCase().indexOf("actual") != -1) {
                                    DateTime dt = dfmt6.parseDateTime(t).dayOfMonth().withMinimumValue()
                                            .withTimeAtStartOfDay();
                                    if (dt.getMillis() > currentdt.getMillis()) {
                                        break timeloop;
                                    }
                                }
                                Double count = 0.0;
                                if (typeDBmap.containsKey(t)) {
                                    count = typeDBmap.get(t);
                                }
                                datalist.add(count);
                            }
                            hm.put("data", datalist);
                        }
                        series.add(hm);
                    }
                });
            }
            executor.shutdown();

            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (series != null && !series.isEmpty()) {
                ret.put("types", types);
                ret.put("time", timelist);
                ret.put("series", series);
                ret.put("title", rs.getProgram_name());
                ret.put("subtitle", "");
                return ret;
            }
        }
        return null;
    }

    @Override
    public TreeMap<String, Double> getResourceGroupByProgramMonth(RevisionSearch rs, String type) {
        TreeMap<String, Double> ret = new TreeMap<String, Double>();

        if (type.toLowerCase().indexOf("actual") == -1) {
            ret = resourcePlanSearchService.groupByMonth(rs, type);
        } else {
            ret = resourceActualSearchService.groupByMonth(rs);
        }
        if (ret != null && ret.keySet().size() > 0)
            return ret;
        return null;
    }

    @Override
    public HashMap getResourceSkillSummaryTable(final RevisionSearch rs, final Map<String, DateTime> datehm) {
        DateTime startdt = null;
        DateTime stopdt = new DateTime().dayOfYear().withMaximumValue().withTimeAtStartOfDay();
        if (datehm != null) {
            if (datehm.containsKey("pc") && datehm.get("pc") != null) {
                startdt = datehm.get("pc").dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            }
        }
        Program program = programService.findById(rs.getProgram_id());
        if (startdt == null) {
            Date minDate = resourcePlanService.findMinResourceDate(program);
            if (minDate == null)
                return null;
            startdt = new DateTime(minDate);
        }

        Set<String> types = resourcePlanSearchService.getDistinctValue(rs, "plan_type");
        if (types == null)
            types = new LinkedHashSet<String>();
        types.add("ACTUAL");
        final HashMap ret = new HashMap();
        final HashMap dataret = new HashMap();
        String foundpc = null;
        boolean checkECRFlag = types.contains("ECR");
        if (checkECRFlag) {
            int pcnum = -1;
            for (String t : types) {
                if (t.toLowerCase().indexOf("ecr") != -1) {
                    String n = t.replaceAll("[^0-9]+", "");
                    if (n.length() > 0) {
                        if (pcnum == -1) {
                            foundpc = t;
                        } else {
                            if (Integer.parseInt(n) > pcnum) {
                                foundpc = t;
                            }
                        }
                    }
                }
            }
        } else {
            int pcnum = -1;
            for (String t : types) {
                if (t.toLowerCase().indexOf("pc") != -1) {
                    String n = t.replaceAll("[^0-9]+", "");
                    if (n.length() > 0) {
                        if (pcnum == -1) {
                            foundpc = t;
                        } else {
                            if (Integer.parseInt(n) > pcnum) {
                                foundpc = t;
                            }
                        }
                    }
                }
            }
        }
        if (foundpc == null) {
            foundpc = "PC";
        }
        final DateTime currentdt = new DateTime();

        DateTime lastUpdatedDate = resourceActualSearchService.getLastUpdateTime(rs);
        if (lastUpdatedDate != null) {
            long diff = (currentdt.getMillis() - lastUpdatedDate.getMillis()) / (1000 * 60 * 60 * 24);
            if (diff > 13)
                ret.put("last_updated_date", "<i class='text-danger'>Updated on " + lastUpdatedDate.toString(dfmt1) + "<i>");
            else
                ret.put("last_updated_date", "<i>Updated on " + lastUpdatedDate.toString(dfmt1) + "<i>");
        }

        final boolean checkPORFlag = types.contains("POR");
        final String latestPC = foundpc;
        Set<String> skillList = new TreeSet<String>();
        Map cleanskillret = getResourceCleanSkillData(rs, types);
        Map tmpret = (Map) cleanskillret.get("skillData");
        skillList = (TreeSet<String>) cleanskillret.get("skillList");

        DateTime trackdt = startdt;
        Set<String> tlist = new TreeSet<String>();
        while (trackdt.getMillis() <= stopdt.getMillis()) {
            tlist.add(trackdt.toString(dfmt6));
            trackdt = trackdt.plusMonths(1);
        }
        if (tlist == null || tlist.isEmpty()) {
            tlist = (TreeSet<String>) cleanskillret.get("timeList");
        }
        final Set<String> timeList = tlist;

        HashMap porMap = new HashMap();
        HashMap actualMap = (HashMap) tmpret.get("ACTUAL");

        if (checkPORFlag) {
            porMap = (HashMap) tmpret.get("POR");
        } else {
            porMap = (HashMap) tmpret.get(latestPC.toUpperCase());
        }
        List currentActualSkillChart = new ArrayList();
        List currentPorSkillChart = new ArrayList();

        List actualSkillTrendDataList = new ArrayList();
        List porSkillTrendDataList = new ArrayList();

        TreeMap skillMap = new TreeMap();
        Double acurrenttotal = 0.0;
        Double pcurrenttotal = 0.0;
        Double aacctotal = 0.0;
        Double pacctotal = 0.0;
        List displaySkillList = new ArrayList();
        int order = 0;
        for (String skill : skillList) {
            List list = new ArrayList();
            Double acurrent = 0.0;
            Double aacc = 0.0;
            Double pcurrent = 0.0;
            Double pacc = 0.0;
            List<Double> actualSkillTrendList = new ArrayList<Double>();
            List<Double> porSkillTrendList = new ArrayList<Double>();
            if (actualMap != null && actualMap.keySet().size() > 0) {
                Map<String, Double> currentMap = null;
                Map<String, Double> totalMap = null;
                if (actualMap.containsKey("current")) {
                    if (actualMap.get("current") instanceof TreeMap) {
                        currentMap = (TreeMap<String, Double>) actualMap.get("current");
                    }
                }
                if (actualMap.containsKey("total")) {
                    if (actualMap.get("total") instanceof TreeMap) {
                        totalMap = (TreeMap<String, Double>) actualMap.get("total");
                    }
                }
                if (actualMap.containsKey("trend")) {
                    Map skillTrend = null;
                    if (actualMap.get("trend") instanceof TreeMap) {
                        skillTrend = (TreeMap) actualMap.get("trend");
                    }
                    Map<String, Double> trend = null;
                    if (skillTrend.containsKey(skill)) {
                        if (skillTrend.get(skill) instanceof TreeMap) {
                            trend = (TreeMap) skillTrend.get(skill);
                        }
                    }
                    if (trend != null && trend.keySet().size() > 0) {
                        for (String dt : timeList) {
                            if (trend.containsKey(dt)) {
                                actualSkillTrendList.add(trend.get(dt));
                            } else {
                                actualSkillTrendList.add(0.0);
                            }
                        }
                    }
                }
                if (currentMap.containsKey(skill.toUpperCase())) {
                    acurrent = currentMap.get(skill.toUpperCase());
                }
                if (totalMap.containsKey(skill.toUpperCase())) {
                    aacc = totalMap.get(skill.toUpperCase());
                }
            }
            if (porMap != null && porMap.keySet().size() > 0) {
                Map<String, Double> currentMap = null;
                Map<String, Double> totalMap = null;
                if (porMap.containsKey("current")) {
                    if (porMap.get("current") instanceof TreeMap) {
                        currentMap = (TreeMap<String, Double>) porMap.get("current");
                    }
                }
                if (porMap.containsKey("total")) {
                    if (porMap.get("total") instanceof TreeMap) {
                        totalMap = (TreeMap<String, Double>) porMap.get("total");
                    }
                }
                if (porMap.containsKey("trend")) {
                    Map skillTrend = null;
                    if (porMap.get("trend") instanceof TreeMap) {
                        skillTrend = (TreeMap<String, TreeMap<String, Double>>) porMap.get("trend");
                    }
                    Map<String, Double> trend = null;
                    if (skillTrend.containsKey(skill)) {
                        if (skillTrend.get(skill) instanceof TreeMap) {
                            trend = (TreeMap) skillTrend.get(skill);
                        }
                    }
                    if (trend != null && trend.keySet().size() > 0) {
                        for (String dt : timeList) {
                            if (trend.containsKey(dt)) {
                                porSkillTrendList.add(trend.get(dt));
                            } else {
                                porSkillTrendList.add(0.0);
                            }
                        }
                    }
                }
                if (currentMap.containsKey(skill.toUpperCase())) {
                    pcurrent = currentMap.get(skill.toUpperCase());
                }
                if (totalMap.containsKey(skill.toUpperCase())) {
                    pacc = totalMap.get(skill.toUpperCase());
                }
            }

            // current
            acurrenttotal += acurrent;
            pcurrenttotal += pcurrent;
            list.add("<span>" + acurrent + "</span>");
            list.add("<span>" + pcurrent + "</span>");
            Double diff = acurrent - pcurrent;
            diff = Math.round(diff * 100.00) / 100.00;
            Double percentage = (pcurrent / acurrent) * 100;
            if (diff > 1 && percentage < 90)
                list.add("<span style='color: rgb(255, 0, 0) !important'>" + diff + "</span>");
            else if (diff > 1 && percentage > 90 && percentage < 95)
                list.add("<span style='color: rgb(255, 165, 0) !important'>" + diff + "</span>");
            else
                list.add("<span>" + diff + "</span>");

            // total
            list.add("<span>" + aacc + "</span>");
            list.add("<span>" + pacc + "</span>");
            aacctotal += aacc;
            pacctotal += pacc;
            diff = aacc - pacc;
            diff = Math.round(diff * 100.00) / 100.00;
            percentage = (pacc / aacc) * 100;
            if (diff > 1 && percentage < 90)
                list.add("<span style='color: rgb(255, 0, 0) !important'>" + diff + "</span>");
            else if (diff > 1 && percentage > 90 && percentage < 95)
                list.add("<span style='color: rgb(255, 165, 0) !important'>" + diff + "</span>");
            else
                list.add("<span>" + diff + "</span>");

            String displaySkill = skill.toUpperCase();
            if (!displaySkill.isEmpty()) {
                displaySkillList.add(displaySkill);
                HashMap tmpmap = new HashMap();
                tmpmap.put("data", list);
                if (displaySkill.toLowerCase().indexOf("misc") != -1) {
                    tmpmap.put("order", 999);
                } else {
                    tmpmap.put("order", order);
                }

                skillMap.put(displaySkill, tmpmap);
                order++;
                currentActualSkillChart.add(Math.round(aacc * 100.00) / 100.00);
                currentPorSkillChart.add(Math.round(pacc * 100.00) / 100.00);

                HashMap actualSkillTrendMap = new HashMap();
                actualSkillTrendMap.put("name", displaySkill.replaceAll("(?i)^z", ""));
                actualSkillTrendMap.put("data", actualSkillTrendList);
                actualSkillTrendDataList.add(actualSkillTrendMap);

                HashMap porSkillTrendMap = new HashMap();
                porSkillTrendMap.put("name", displaySkill.replaceAll("(?i)^z", ""));
                porSkillTrendMap.put("data", porSkillTrendList);
                porSkillTrendDataList.add(porSkillTrendMap);
            }
        }

        List list = new ArrayList();
        list.add("<span>" + Math.round(acurrenttotal * 100.00) / 100.00 + "</span>");
        list.add("<span>" + Math.round(pcurrenttotal * 100.00) / 100.00 + "</span>");
        Double diff = acurrenttotal - pcurrenttotal;
        diff = Math.round(diff * 100.00) / 100.00;
        Double percentage = (pcurrenttotal / acurrenttotal) * 100;
        if (diff > 1 && percentage < 90)
            list.add("<span style='color: rgb(255, 0, 0) !important'>" + diff + "</span>");
        else if (diff > 1 && percentage > 90 && percentage < 95)
            list.add("<span style='color: rgb(255, 165, 0) !important'>" + diff + "</span>");
        else
            list.add("<span>" + diff + "</span>");

        list.add("<span>" + Math.round(aacctotal * 100.00) / 100.00 + "</span>");
        list.add("<span>" + Math.round(pacctotal * 100.00) / 100.00 + "</span>");
        diff = aacctotal - pacctotal;
        diff = Math.round(diff * 100.00) / 100.00;
        percentage = (pacctotal / aacctotal) * 100;
        if (diff > 1 && percentage < 90)
            list.add("<span style='color: rgb(255, 0, 0) !important'>" + diff + "</span>");
        else if (diff > 1 && percentage > 90 && percentage < 95)
            list.add("<span style='color: rgb(255, 165, 0) !important'>" + diff + "</span>");
        else
            list.add("<span>" + diff + "</span>");

        HashMap tmpmap = new HashMap();
        tmpmap.put("data", list);
        tmpmap.put("order", 1000);

        skillMap.put("ZTOTAL", tmpmap);

        List skillCurrentTrendChart = new ArrayList();
        HashMap skillCurrentActualTrendMap = new HashMap();
        skillCurrentActualTrendMap.put("name", "Actual");
        skillCurrentActualTrendMap.put("data", currentActualSkillChart);
        skillCurrentTrendChart.add(skillCurrentActualTrendMap);

        HashMap skillCurrentPorTrendMap = new HashMap();
        skillCurrentPorTrendMap.put("name", "POR");
        skillCurrentPorTrendMap.put("data", currentPorSkillChart);
        skillCurrentTrendChart.add(skillCurrentPorTrendMap);

        HashMap skillCurrentTrend = new HashMap();
        skillCurrentTrend.put("data", skillCurrentTrendChart);
        skillCurrentTrend.put("time", displaySkillList);

        dataret.put("currentSkillChart", skillCurrentTrend);

        HashMap actualSkillTrend = new HashMap();
        actualSkillTrend.put("time", timeList);
        actualSkillTrend.put("data", actualSkillTrendDataList);
        dataret.put("actualSkillTrend", actualSkillTrend);

        HashMap porSkillTrend = new HashMap();
        porSkillTrend.put("time", timeList);
        porSkillTrend.put("data", porSkillTrendDataList);
        dataret.put("porSkillTrend", porSkillTrend);

        if (skillMap != null && skillMap.keySet().size() > 0) {
            dataret.put("table", skillMap);
        }
        dataret.put("skillList", skillList);

        if (dataret != null && dataret.keySet().size() > 0) {
            ret.put("data", dataret);
            return ret;
        }
        return null;
    }

    @Override
    public List<Map> getActualEmployeeData(RevisionSearch rs) {
        DateTime stopDate = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        return resourceActualSearchService.findByProgram(rs, stopDate);
    }

    @Override
    public List<Map> getActualEmployeeData(RevisionSearch rs, DateTime dt) {
        DateTime stopDate = dt.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        return resourceActualSearchService.findByProgram(rs, stopDate);
    }

    @Override
    public Set<String> getActualTimeList(RevisionSearch rs) {
        Set<String> set = resourceActualSearchService.getProgramDistinctValue(rs, "month");
        return set;
    }

//	@Override
//	public HashMap parseFile(RevisionSearch rs, String username, File file) {
//		HashMap ret = new HashMap();
//		try {
//			List<ResourcePlan> resourceInsertList = new ArrayList();
//			List<SkillMapping> skillInsertList = new ArrayList();
//			List<ResourcePlanSearch> spsl = new ArrayList();
//			FileInputStream is = new FileInputStream(file);
//			Workbook wb = WorkbookFactory.create(is);
//			Sheet sheet = wb.getSheetAt(0);
//			Row row = null;
//			Row dateRow = null;
//			int skillCount = 0;
//			final Timestamp ts = new Timestamp(new Date().getTime());
//
//			List<String> projectRegion = new ArrayList();
//			CellRangeAddress range;
//			for (int m = 0; m < sheet.getNumMergedRegions(); m++) {
//				range = sheet.getMergedRegion(m);
//				String[] addr = range.formatAsString().split(":");
//				if ((addr[0].contains("A")) && (addr[1].contains("A"))) {
//					projectRegion.add(range.formatAsString());
//				}
//			}
//			Collections.sort(projectRegion);
//			Collections.reverse(projectRegion);
//			Program p = progServ.findById(rs.getProgram_id());
//
//			ExecutorService executor = Executors.newFixedThreadPool(10);
//			rangeLoop:
//			for (String projectRange : projectRegion) {
//				String[] xpRange = projectRange.replaceAll("[a-zA-Z]", "").split(":");
//				int start = Integer.parseInt(xpRange[0]);
//				int stop = Integer.parseInt(xpRange[1]);
//				if(start==stop)
//					continue rangeLoop;
//				int titleStart = start - 1;
//				String title = sheet.getRow(titleStart).getCell(0).getStringCellValue();
//				if (title.toLowerCase().indexOf("mapping") != -1) {
//					executor = Executors.newFixedThreadPool(10);
//					List<SkillMapping> skillList = skillMappingService.findByProgram(rs);
//					if (skillList != null && skillList.size() > 0) {
//						for (final SkillMapping skill : skillList) {
//							executor.submit(new Runnable() {
//								public void run() {
//									skillMappingService.delete(skill.getId());
//								}
//							});
//						}
//						executor.shutdown();
//						try {
//							executor.awaitTermination(5, TimeUnit.MINUTES);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//					for (int i = start; i < stop; i++) {
//						if ((sheet.getRow(i) != null) && (sheet.getRow(i).getCell(1).getCellType() != 3)) {
//							String pskill = sheet.getRow(i).getCell(1).getStringCellValue().trim();
//							String askill = sheet.getRow(i).getCell(2).getStringCellValue().trim();
//							if(pskill.trim().isEmpty())
//								return null;
//							if(askill.trim().isEmpty())
//								askill = pskill;
//							SkillMapping skill = new SkillMapping();
//							skill.setPlan_skill(pskill);
//							skill.setActual_skill(askill);
//							skill.setProgram(p);
//							skill.setOrderNum(Integer.valueOf(skillCount));
//							skill.setCreatedBy(username);
//							skill.setCreatedDate(ts);
//							skill.setExclude(sheet.getRow(i).getCell(3).getStringCellValue().trim());
//							skillCount++;
//							skillInsertList.add(skill);
//						}
//					}
//				} else {
//					executor = Executors.newFixedThreadPool(100);
//					List<Integer> rplanlist = new JPAQuery(em).from(qresourceplan)
//							.where(qresourceplan.program.eq(p).and(qresourceplan.type.toLowerCase().like(title)))
//							.list(qresourceplan.id);
//
//
//					if (rplanlist != null && !rplanlist.isEmpty()) {
//						// can not edit pc
//						if (title.trim().equalsIgnoreCase("pc")) {
//							ret.put("msg", "PC data exists in the system. The current PC data will not be udpated");
//							continue rangeLoop;
//						}
//						for (final Integer resourcePlan_id : rplanlist) {
//							executor.submit(new Runnable() {
//								public void run() {
//									resourcePlanServ.delete(resourcePlan_id);
//								}
//							});
//						}
//						executor.shutdown();
//						try {
//							executor.awaitTermination(5, TimeUnit.MINUTES);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//					resourcePlanSearchService.deleteByPlanType(rs, title);
//					dateRow = sheet.getRow(titleStart);
//
//					int colCount = dateRow.getLastCellNum();
//					for (int i = start; i < stop; i++) {
//						row = sheet.getRow(i);
//						if (row.getCell(1).getCellType() != 3) {
//							for (int j = 2; j < colCount; j++) {
//								if (dateRow.getCell(j).getCellType() != 3) {
//									ResourcePlanSearch sps = new ResourcePlanSearch();
//									sps.setInclude_contractor(true);
//									sps.setMonth(dateRow.getCell(j).getDateCellValue());
//									sps.setPlan_type(title.toUpperCase().trim());
//									sps.setProgram(p.getId());
//									sps.setSkill(row.getCell(1).getStringCellValue().trim());
//
//									ResourcePlan rp = new ResourcePlan();
//									rp.setCreatedBy(username);
//									rp.setInclude_contractor(true);
//									rp.setMonth(dateRow.getCell(j).getDateCellValue());
//									rp.setProgram(p);
//									rp.setPlan_skill(row.getCell(1).getStringCellValue().trim());
//									rp.setType(title.toUpperCase().trim());
//									rp.setCreatedDate(ts);
//									Double count = 0.0;
//									if(row.getCell(j)!= null){
//										switch (row.getCell(j).getCellType()) {
//											case Cell.CELL_TYPE_BLANK:
//												count = 0.0;
//												break;
//											case Cell.CELL_TYPE_NUMERIC:
//												count = row.getCell(j).getNumericCellValue();
//												break;
//											case Cell.CELL_TYPE_STRING:
//												String cs = row.getCell(j).getRichStringCellValue().getString().replaceAll("\\s", "").trim();
//												if (cs.indexOf("0.5") != -1) {
//													count = 0.5D;
//												} else {
//													count = Double.parseDouble(cs.trim());
//												}
//												break;
//											case Cell.CELL_TYPE_FORMULA:
//												switch(row.getCell(j).getCachedFormulaResultType()){
//													case Cell.CELL_TYPE_NUMERIC:
//														count = row.getCell(j).getNumericCellValue();
//														break;
//													case Cell.CELL_TYPE_STRING:
//														cs = row.getCell(j).getRichStringCellValue().getString().replaceAll("\\s", "").trim();
//														if (cs.indexOf("0.5") != -1) {
//															count = 0.5D;
//														} else {
//															count = Double.parseDouble(cs.trim());
//														}
//														break;
//												}
//												break;
//										}
//
//									}
//									rp.setCount(count);
//									sps.setCount(count);
//									spsl.add(sps);
//									resourceInsertList.add(rp);
//								}
//							}
//						}
//					}
//				}
//			}
//			if (resourceInsertList.size() > 0) {
//				resourcePlanServ.createBulk(resourceInsertList);
//				resourcePlanSearchService.saveBulk(spsl);
//			}
//			if (skillInsertList.size() > 0) {
//				skillMappingService.createBulk(skillInsertList);
//			}
//
//			ret.put("msg", "Update Succesful");
//		} catch (FileNotFoundException e) {
//			ret.put("err", "Upload File Fail");
//			e.printStackTrace();
//		} catch (InvalidFormatException e) {
//			ret.put("err", "Invalid Format");
//			e.printStackTrace();
//		} catch (IOException e) {
//			ret.put("err", "IO Failed");
//			e.printStackTrace();
//		}
//		return ret;
//	}

    private Double getTotalResourceTime(RevisionSearch rs, DateTime dt1, DateTime dt2, String type) {
        Double ret = 0.0;
        TreeMap<String, Double> hm = new TreeMap<String, Double>();
        if (type.toLowerCase().indexOf("actual") == -1) {
            hm = resourcePlanSearchService.groupByMonth(rs, type);
        } else {
            hm = resourceActualSearchService.groupByMonth(rs);
        }
        DateTime currentdt = new DateTime().plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        if (dt1 != null && dt2 != null && dt1.getMillis() > dt2.getMillis())
            return 0.0;

        if (type.toLowerCase().indexOf("actual") != -1) {
            if (dt1 != null && dt1.getMillis() > currentdt.getMillis())
                dt1 = currentdt;
            if (dt2 != null) {
                if (dt2.getMillis() > currentdt.getMillis()) {
                    dt2 = currentdt;
                }
            } else {
                dt2 = currentdt;
            }
        }
        if (hm != null && hm.keySet().size() > 0) {
            for (String d : hm.keySet()) {
                DateTime dt = dfmt6.parseDateTime(d);
                if (dt1 != null && dt2 != null) {
                    if (dt.getMillis() >= dt1.getMillis() && dt.getMillis() < dt2.getMillis()) {
                        ret += hm.get(d);
                    }
                } else {
                    if (dt1 != null) {
                        if (dt.getMillis() >= dt1.getMillis()) {
                            ret += hm.get(d);
                        }
                    } else if (dt2 != null) {
                        if (dt.getMillis() < dt2.getMillis()) {
                            ret += hm.get(d);
                        }
                    } // if 2 date is null, get the sum of all
                    else {
                        if (type.toLowerCase().indexOf("actual") == -1) {
                            ret += hm.get(d);
                        } else {
                            if (dt.getMillis() < currentdt.getMillis()) {
                                ret += hm.get(d);
                            }
                        }
                    }
                }
            }
        }
        if (ret != null && ret != 0.0)
            return Math.round(ret * 100.00) / 100.00;
        return 0.0;
    }

    private Map getResourceCleanSkillData(final RevisionSearch rs, Set<String> types) {
        final Map tmpret = Collections.synchronizedMap(new HashMap());
        final String currentdt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay().toString(dfmt6);
        final Set<String> skillList = new TreeSet<String>();
        skillList.add("ZMISC");
        final Set<String> timeList = new TreeSet<String>();
        Map ret = new HashMap();
        Program program = programService.findById(rs.getProgram_id());
        if (program == null) return null;
        final List<SkillMapping> mappingTable = skillMappingService.findByProgram(program);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (final String type : types) {
            executor.submit(new Runnable() {
                public void run() {
                    HashMap skillMap = new HashMap();
                    HashMap<String, TreeMap<String, Double>> hm = new HashMap<String, TreeMap<String, Double>>();
                    TreeMap<String, Double> currentMap = new TreeMap<String, Double>();
                    TreeMap<String, Double> totalMap = new TreeMap<String, Double>();
                    TreeMap<String, TreeMap<String, Double>> skillTrendMap = new TreeMap<String, TreeMap<String, Double>>();
                    if (type.equalsIgnoreCase("actual")) {
                        hm = resourceActualSearchService.groupByMonthSkill(rs);
                    } else {
                        hm = resourcePlanSearchService.groupByMonthSkill(rs, type);
                    }
                    if (hm != null && hm.keySet().size() > 0) {
                        for (String skill : hm.keySet()) {
                            Double currentCount = 0.0;
                            Double totalCount = 0.0;
                            TreeMap<String, Double> skillMonthMap = new TreeMap<String, Double>();
                            TreeMap<String, Double> tm = hm.get(skill);
                            String aSkill = skill.toLowerCase().trim();
                            if (mappingTable != null && mappingTable.size() > 0) {
                                String skill_name = "ZMISC";
                                skillmappingLoop:
                                for (SkillMapping skillMapping : mappingTable) {
                                    if (type.equalsIgnoreCase("actual")) {
                                        String[] pkArr = skillMapping.getActual_skill().toLowerCase().split(",", -1);
                                        List<String> pklist = new ArrayList<String>();
                                        for (int i = 0; i < pkArr.length; i++)
                                            pklist.add(pkArr[i].trim());
                                        if (pklist.contains(aSkill)) {
                                            skill_name = skillMapping.getPlan_skill().trim().toUpperCase();
                                            break skillmappingLoop;
                                        }
                                    } else {
                                        String pSkill = skillMapping.getPlan_skill().trim().toLowerCase();
                                        if (pSkill.indexOf(aSkill) != -1) {
                                            skill_name = skillMapping.getPlan_skill().trim().toUpperCase();
                                            break skillmappingLoop;
                                        }
                                    }
                                }
                                skillList.add(skill_name.trim().toUpperCase());
                                if (currentMap.containsKey(skill_name.toUpperCase())) {
                                    currentCount = currentMap.get(skill_name.toUpperCase());
                                }
                                if (totalMap.containsKey(skill_name.toUpperCase())) {
                                    totalCount = totalMap.get(skill_name.toUpperCase());
                                }
                                if (skillTrendMap.containsKey(skill_name.toUpperCase())) {
                                    skillMonthMap = skillTrendMap.get(skill_name.toUpperCase());
                                }

                                dtloop:
                                for (String dt : tm.keySet()) {
                                    if (type.equalsIgnoreCase("actual")) {
                                        timeList.add(dt);
                                    }
                                    Double count = Math.round(tm.get(dt) * 100.0) / 100.0;
                                    totalCount += count;
                                    if (skillMonthMap.containsKey(dt)) {
                                        count += skillMonthMap.get(dt);
                                    }
                                    skillMonthMap.put(dt, count);
                                    if (dt.equals(currentdt)) {
                                        currentCount = count;
                                        break dtloop;
                                    }
                                }
                                currentMap.put(skill_name.toUpperCase(), Math.round(currentCount * 100.00) / 100.00);
                                totalMap.put(skill_name.toUpperCase(), Math.round(totalCount * 100.00) / 100.00);
                                skillTrendMap.put(skill_name.toUpperCase(), skillMonthMap);
                            }
                        }
                    }

                    // System.out.println(type+"---"+totalMap+"---"+hm);
                    if (currentMap != null && currentMap.keySet().size() > 0) {
                        skillMap.put("current", currentMap);
                    }
                    if (totalMap != null && totalMap.keySet().size() > 0) {
                        skillMap.put("total", totalMap);
                    }
                    if (skillTrendMap != null && skillTrendMap.keySet().size() > 0) {
                        skillMap.put("trend", skillTrendMap);
                    }
                    tmpret.put(type.toUpperCase(), skillMap);
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ret.put("skillData", tmpret);
        ret.put("skillList", skillList);
        ret.put("timeList", timeList);
        return ret;
    }
}
