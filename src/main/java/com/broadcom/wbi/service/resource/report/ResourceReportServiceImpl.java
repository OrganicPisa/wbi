package com.broadcom.wbi.service.resource.report;

import com.broadcom.wbi.model.mysql.ResourceProgramClassification;
import com.broadcom.wbi.service.elasticSearch.ResourceActualSearchService;
import com.broadcom.wbi.service.elasticSearch.ResourcePlanSearchService;
import com.broadcom.wbi.service.jpa.*;
import com.broadcom.wbi.util.TextUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ResourceReportServiceImpl implements ResourceReportService {

    private final ResourcePlanService resourcePlanService;
    private final ProgramService programService;
    private final ResourcePlanSearchService resourcePlanSearchService;
    private final ResourceActualSearchService resourceActualSearchService;
    private final SkillMappingService skillMappingService;
    private final RedisCacheRepository redisCacheRepository;

    private final ResourceProgramClassificationService resourceProgramClassificationService;

    @Autowired
    public ResourceReportServiceImpl(ResourcePlanService resourcePlanService, ProgramService programService, ResourcePlanSearchService resourcePlanSearchService,
                                     ResourceActualSearchService resourceActualSearchService, SkillMappingService skillMappingService,
                                     RedisCacheRepository redisCacheRepository, ResourceProgramClassificationService resourceProgramClassificationService) {
        this.resourcePlanService = resourcePlanService;
        this.programService = programService;
        this.resourcePlanSearchService = resourcePlanSearchService;
        this.resourceActualSearchService = resourceActualSearchService;
        this.skillMappingService = skillMappingService;
        this.redisCacheRepository = redisCacheRepository;
        this.resourceProgramClassificationService = resourceProgramClassificationService;
    }

    @Override
    public List getCurrentReportGroupStatus(String group_by, Boolean isCSGOnly, String employeeType) {
        List ret = new ArrayList();
        TreeMap tm = resourceActualSearchService.getCurrentReportGroup(group_by, isCSGOnly, employeeType);
        if (tm != null && tm.keySet().size() > 0) {
            for (Object term : tm.keySet()) {
                Double d = (Double) tm.get(term);
                if (d > 5) {
                    HashMap hm = new HashMap();
                    hm.put("name", term.toString());
                    hm.put("y", d);
                    ret.add(hm);
                }
            }
            return ret;
        }
        return null;
    }

    @Override
    public TreeMap generateTrendGroupChart(String groupBy, String intervalGroup, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType) {
        TreeMap datamap = resourceActualSearchService.getTrendReport1Group(groupBy, intervalGroup, fromdt, todt, isCSGOnly, employeeType);
        if (datamap != null && datamap.keySet().size() > 0) {
            TreeMap ret = new TreeMap();
            List<String> timelist = new ArrayList<String>();
            List dataList = new ArrayList();

            DateTime dt = fromdt.plusDays(1);
            while (dt.getMillis() < todt.getMillis()) {
                if (intervalGroup.equalsIgnoreCase("year")) {
                    timelist.add(Integer.toString(convertFiscalYear(dt)));
                } else if (intervalGroup.equalsIgnoreCase("quarter")) {
                    timelist.add(convertFiscalQuarter(dt));
                } else {
                    timelist.add(dt.toString(dfmt6));
                }
                if (intervalGroup.equalsIgnoreCase("year")) {
                    dt = dt.plusYears(1);
                } else if (intervalGroup.equalsIgnoreCase("quarter")) {
                    dt = dt.plusMonths(3);
                } else {
                    dt = dt.plusMonths(1);
                }
            }
            ret.put("category", timelist);
            for (Object term : datamap.keySet()) {
                TreeMap subtm = (TreeMap) datamap.get(term);
                if (subtm != null && subtm.keySet().size() > 0) {
                    List list = new ArrayList();
                    Double count = 0.0;
                    LinkedHashMap<String, Double> datasubtm = new LinkedHashMap<String, Double>();
                    for (Object d : subtm.keySet()) {
                        DateTime subdatadt = dfmt6.parseDateTime(d.toString());
                        String keydt = "";
                        if (intervalGroup.equalsIgnoreCase("year")) {
                            keydt = Integer.toString(convertFiscalYear(subdatadt));
                        } else if (intervalGroup.equalsIgnoreCase("quarter")) {
                            keydt = convertFiscalQuarter(subdatadt);
                        } else {
                            keydt = subdatadt.toString(dfmt6);
                        }
                        count = 0.0;
                        if (datasubtm.containsKey(keydt)) {
                            count = datasubtm.get(keydt);
                        }
                        count += (Double) subtm.get(d);
                        datasubtm.put(keydt, count);
                    }

                    if (datasubtm != null && datasubtm.keySet().size() > 0) {
                        for (String tl : timelist) {
                            if (datasubtm.containsKey(tl)) {
                                Double c = datasubtm.get(tl);
                                if (intervalGroup.equalsIgnoreCase("year")) {
                                    c = c / 12;
                                } else if (intervalGroup.equalsIgnoreCase("quarter")) {
                                    c = c / 3;
                                }
                                list.add(Math.round(c * 100.00) / 100.00);
                            } else {
                                list.add(0.00);
                            }
                        }
                    }
                    if (list != null && !list.isEmpty()) {
                        HashMap hm = new HashMap();
                        hm.put("name", term.toString());
                        hm.put("data", list);
                        hm.put("showInLegend", true);
                        if (datamap.keySet().size() > 30) {
                            hm.put("showInLegend", false);
                        }
                        dataList.add(hm);
                    }
                }
            }
            ret.put("series", dataList);
            return ret;
        }
        return null;

    }

    @Override
    public TreeMap getTrend1GroupStatus(String groupBy, String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType) {
        TreeMap datamap = resourceActualSearchService.getTrendReport1Group(groupBy, interval, fromdt, todt, isCSGOnly, employeeType);
        if (datamap != null && datamap.keySet().size() > 0) {
            TreeMap ret = new TreeMap();
            List collist = new ArrayList();
            Map colmap = new HashMap();
            colmap.put("name", groupBy.toUpperCase());
            collist.add(colmap);

            DateTime dt = fromdt.plusDays(1);
            List<String> timelist = new ArrayList();
            while (dt.getMillis() < todt.getMillis()) {
                colmap = new HashMap();
                if (interval.equalsIgnoreCase("year")) {
                    colmap.put("name", convertFiscalYear(dt));
                    timelist.add(Integer.toString(convertFiscalYear(dt)));
                } else if (interval.equalsIgnoreCase("quarter")) {
                    colmap.put("name", convertFiscalQuarter(dt));
                    timelist.add(convertFiscalQuarter(dt));
                } else {
                    colmap.put("name", dt.toString(dfmt6));
                    timelist.add(dt.toString(dfmt6));
                }
                colmap.put("enableFiltering", false);
                collist.add(colmap);

                if (interval.equalsIgnoreCase("year")) {
                    dt = dt.plusYears(1);
                } else if (interval.equalsIgnoreCase("quarter")) {
                    dt = dt.plusMonths(3);
                } else {
                    dt = dt.plusMonths(1);
                }
            }

            colmap = new HashMap();
            colmap.put("name", "TOTAL");
            collist.add(colmap);
            List datalist = new ArrayList();
            for (Object dc : datamap.keySet()) {
                Map dcmap = new HashMap();
                if (datamap.get(dc) instanceof TreeMap) {
                    dcmap = (TreeMap) datamap.get(dc);
                } else {
                    dcmap = (HashMap) datamap.get(dc);
                }
                if (dcmap != null && dcmap.keySet().size() > 0) {
                    LinkedHashMap projectskillmap = new LinkedHashMap();
                    projectskillmap.put(groupBy.toUpperCase(), dc.toString());
                    if (timelist.size() > 0) {
                        Double total = 0.0;
                        Double count = 0.0;
                        LinkedHashMap<String, Double> datasubtm = new LinkedHashMap<String, Double>();
                        for (Object d : dcmap.keySet()) {
                            DateTime subdatadt = dfmt6.parseDateTime(d.toString());
                            String keydt = "";
                            if (interval.equalsIgnoreCase("year")) {
                                keydt = Integer.toString(convertFiscalYear(subdatadt));
                            } else if (interval.equalsIgnoreCase("quarter")) {
                                keydt = convertFiscalQuarter(subdatadt);
                            } else {
                                keydt = subdatadt.toString(dfmt6);
                            }
                            count = 0.0;
                            if (datasubtm.containsKey(keydt)) {
                                count = datasubtm.get(keydt);
                            }
                            count += (Double) dcmap.get(d);
                            datasubtm.put(keydt, count);
                        }
                        for (Object time : timelist) {
                            Double c = 0.0;
                            if (datasubtm.containsKey(time)) {
                                c = Double.valueOf(datasubtm.get(time).toString());
                            }
                            if (interval.equalsIgnoreCase("year")) {
                                c = c / 12;
                            } else if (interval.equalsIgnoreCase("quarter")) {
                                c = c / 3;
                            }
                            c = Math.round(c * 100.00) / 100.00;
                            total += c;
                            projectskillmap.put(time, c);
                        }
                        projectskillmap.put("TOTAL", Math.round(total * 100.00) / 100.00);
                        datalist.add(projectskillmap);
                    }
                }
            }
            if (datalist.size() > 0) {
                ret.put("data", datalist);
                ret.put("col", collist);
            }

            if (ret != null && ret.keySet().size() > 0) {
                return ret;
            }
        }
        return null;
    }

    @Override
    public TreeMap getTrend2GroupStatus(String groupBy1, String groupBy2, String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType) {
        TreeMap datamap = resourceActualSearchService.getTrendReport2Group(groupBy1, groupBy2, interval, fromdt, todt, isCSGOnly, employeeType);
        if (datamap != null && datamap.keySet().size() > 0) {
            TreeMap ret = new TreeMap();
            List collist = new ArrayList();
            Map colmap = new HashMap();
            colmap.put("name", groupBy1.toUpperCase());
            colmap.put("grouping", new HashMap() {{
                put("groupPriority", 0);
            }});
            colmap.put("sort", new HashMap() {{
                put("priority", 0);
                put("direction", "\'desc\'");
            }});
            colmap.put("cellTemplate", "<div><div ng-if='!col.grouping || col.grouping.groupPriority === undefined || col.grouping.groupPriority === null || ( row.groupHeader && col.grouping.groupPriority === row.treeLevel )' class='ui-grid-cell-contents' title='TOOLTIP'>{{COL_FIELD CUSTOM_FILTERS}}</div></div>");

            collist.add(colmap);
            colmap = new HashMap();
            colmap.put("name", groupBy2.toUpperCase());
            collist.add(colmap);

            DateTime dt = fromdt.plusDays(1);
            List timelist = new ArrayList();
            while (dt.getMillis() < todt.getMillis()) {
                colmap = new HashMap();
                if (interval.equalsIgnoreCase("year")) {
                    colmap.put("name", convertFiscalYear(dt));
                    timelist.add(Integer.toString(convertFiscalYear(dt)));
                } else if (interval.equalsIgnoreCase("quarter")) {
                    colmap.put("name", convertFiscalQuarter(dt));
                    timelist.add(convertFiscalQuarter(dt));
                } else {
                    colmap.put("name", dt.toString(dfmt6));
                    timelist.add(dt.toString(dfmt6));
                }
                colmap.put("enableFiltering", false);
                collist.add(colmap);

                if (interval.equalsIgnoreCase("year")) {
                    dt = dt.plusYears(1);
                } else if (interval.equalsIgnoreCase("quarter")) {
                    dt = dt.plusMonths(3);
                } else {
                    dt = dt.plusMonths(1);
                }
            }

            colmap = new HashMap();
            colmap.put("name", "TOTAL");
            collist.add(colmap);
            List datalist = new ArrayList();

            for (Object dc : datamap.keySet()) {
                Map dctm = null;
                if (datamap.get(dc) instanceof TreeMap) {
                    dctm = (TreeMap) datamap.get(dc);
                } else {
                    dctm = (HashMap) datamap.get(dc);
                }
                if (dctm != null && dctm.keySet().size() > 0) {
                    for (Object proj : dctm.keySet()) {
                        Map projtm = null;
                        if (dctm.get(proj) instanceof TreeMap) {
                            projtm = (TreeMap) dctm.get(proj);
                        } else {
                            projtm = (HashMap) dctm.get(proj);
                        }
                        if (projtm != null && projtm.keySet().size() > 0) {
                            LinkedHashMap projectskillmap = new LinkedHashMap();
                            projectskillmap.put(groupBy1.toUpperCase(), dc.toString());
                            projectskillmap.put(groupBy2.toUpperCase(), proj.toString());
                            if (timelist.size() > 0) {
                                Double total = 0.0;
                                Double count = 0.0;
                                LinkedHashMap<String, Double> datasubtm = new LinkedHashMap<String, Double>();
                                for (Object d : projtm.keySet()) {
                                    DateTime subdatadt = dfmt6.parseDateTime(d.toString());
                                    String keydt = "";
                                    if (interval.equalsIgnoreCase("year")) {
                                        keydt = Integer.toString(convertFiscalYear(subdatadt));
                                    } else if (interval.equalsIgnoreCase("quarter")) {
                                        keydt = convertFiscalQuarter(subdatadt);
                                    } else {
                                        keydt = subdatadt.toString(dfmt6);
                                    }
                                    count = 0.0;
                                    if (datasubtm.containsKey(keydt)) {
                                        count = datasubtm.get(keydt);
                                    }
                                    count += (Double) projtm.get(d);
                                    datasubtm.put(keydt, count);
                                }
                                for (Object time : timelist) {
                                    Double c = 0.0;
                                    if (datasubtm.containsKey(time)) {
                                        c = Double.valueOf(datasubtm.get(time).toString());
                                    }
                                    if (interval.equalsIgnoreCase("year")) {
                                        c = c / 12;
                                    } else if (interval.equalsIgnoreCase("quarter")) {
                                        c = c / 3;
                                    }
                                    c = Math.round(c * 100.00) / 100.00;
                                    total += c;
                                    projectskillmap.put(time, c);
                                }
                                projectskillmap.put("TOTAL", Math.round(total * 100.00) / 100.00);
                                datalist.add(projectskillmap);
                            }
                        }
                    }
                }
            }
            if (datalist.size() > 0) {
                ret.put("data", datalist);
                ret.put("col", collist);
            }
            if (ret != null && ret.keySet().size() > 0) {
                return ret;
            }
        }
        return null;

    }

    @Override
    public TreeMap getTrend3GroupStatus(String groupBy1, String groupBy2, String groupBy3, String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType) {
        TreeMap datamap = resourceActualSearchService.getTrendReport3Group(groupBy1, groupBy2, groupBy3, interval, fromdt, todt, isCSGOnly, employeeType);
        if (datamap != null && datamap.keySet().size() > 0) {
            TreeMap ret = new TreeMap();
            List collist = new ArrayList();
            Map colmap = new HashMap();
            colmap.put("name", groupBy1.toUpperCase());
            colmap.put("grouping", new HashMap() {{
                put("groupPriority", 0);
            }});
            colmap.put("sort", new HashMap() {{
                put("priority", 0);
                put("direction", "\'desc\'");
            }});
            colmap.put("cellTemplate", "<div><div ng-if='!col.grouping || col.grouping.groupPriority === undefined || col.grouping.groupPriority === null || ( row.groupHeader && col.grouping.groupPriority === row.treeLevel )' class='ui-grid-cell-contents' title='TOOLTIP'>{{COL_FIELD CUSTOM_FILTERS}}</div></div>");

            collist.add(colmap);
            colmap = new HashMap();
            colmap.put("name", groupBy2.toUpperCase());
            colmap.put("grouping", new HashMap() {{
                put("groupPriority", 1);
            }});
            colmap.put("sort", new HashMap() {{
                put("priority", 1);
                put("direction", "\'desc\'");
            }});
            colmap.put("cellTemplate", "<div><div ng-if='!col.grouping || col.grouping.groupPriority === undefined || col.grouping.groupPriority === null || ( row.groupHeader && col.grouping.groupPriority === row.treeLevel )' class='ui-grid-cell-contents' title='TOOLTIP'>{{COL_FIELD CUSTOM_FILTERS}}</div></div>");

            collist.add(colmap);
            colmap = new HashMap();
            colmap.put("name", groupBy3.toUpperCase());
            collist.add(colmap);

            DateTime dt = fromdt.plusDays(1);
            List timelist = new ArrayList();
            while (dt.getMillis() < todt.getMillis()) {
                colmap = new HashMap();
                if (interval.equalsIgnoreCase("year")) {
                    colmap.put("name", convertFiscalYear(dt));
                    timelist.add(Integer.toString(convertFiscalYear(dt)));
                } else if (interval.equalsIgnoreCase("quarter")) {
                    colmap.put("name", convertFiscalQuarter(dt));
                    timelist.add(convertFiscalQuarter(dt));
                } else {
                    colmap.put("name", dt.toString(dfmt6));
                    timelist.add(dt.toString(dfmt6));
                }
                colmap.put("enableFiltering", false);
                collist.add(colmap);

                if (interval.equalsIgnoreCase("year")) {
                    dt = dt.plusYears(1);
                } else if (interval.equalsIgnoreCase("quarter")) {
                    dt = dt.plusMonths(3);
                } else {
                    dt = dt.plusMonths(1);
                }
            }
            colmap = new HashMap();
            colmap.put("name", "TOTAL");
            collist.add(colmap);
            List datalist = new ArrayList();
            for (Object dc : datamap.keySet()) {
                Map dctm = null;
                if (datamap.get(dc) instanceof TreeMap) {
                    dctm = (TreeMap) datamap.get(dc);
                } else {
                    dctm = (HashMap) datamap.get(dc);
                }
                if (dctm != null && dctm.keySet().size() > 0) {
                    for (Object proj : dctm.keySet()) {
                        Map projtm = null;
                        if (dctm.get(proj) instanceof TreeMap) {
                            projtm = (TreeMap) dctm.get(proj);
                        } else {
                            projtm = (HashMap) dctm.get(proj);
                        }
                        if (projtm != null && projtm.keySet().size() > 0) {
                            for (Object skill : projtm.keySet()) {
                                Map skilltm = null;
                                if (projtm.get(skill) instanceof TreeMap) {
                                    skilltm = (TreeMap) projtm.get(skill);
                                } else {
                                    skilltm = (HashMap) projtm.get(skill);
                                }
                                if (skilltm != null && skilltm.keySet().size() > 0) {
                                    LinkedHashMap projectskillmap = new LinkedHashMap();
                                    projectskillmap.put(groupBy1.toUpperCase(), dc.toString());
                                    projectskillmap.put(groupBy2.toUpperCase(), proj.toString());
                                    projectskillmap.put(groupBy3.toUpperCase(), skill.toString());
                                    if (timelist.size() > 0) {
                                        Double total = 0.0;
                                        Double count = 0.0;
                                        LinkedHashMap<String, Double> datasubtm = new LinkedHashMap<String, Double>();
                                        for (Object d : projtm.keySet()) {
                                            DateTime subdatadt = dfmt6.parseDateTime(d.toString());
                                            String keydt = "";
                                            if (interval.equalsIgnoreCase("year")) {
                                                keydt = Integer.toString(convertFiscalYear(subdatadt));
                                            } else if (interval.equalsIgnoreCase("quarter")) {
                                                keydt = convertFiscalQuarter(subdatadt);
                                            } else {
                                                keydt = subdatadt.toString(dfmt6);
                                            }
                                            count = 0.0;
                                            if (datasubtm.containsKey(keydt)) {
                                                count = datasubtm.get(keydt);
                                            }
                                            count += (Double) projtm.get(d);
                                            datasubtm.put(keydt, count);
                                        }
                                        for (Object time : timelist) {
                                            Double c = 0.0;
                                            if (datasubtm.containsKey(time)) {
                                                c = Double.valueOf(datasubtm.get(time).toString());
                                            }
                                            if (interval.equalsIgnoreCase("year")) {
                                                c = c / 12;
                                            } else if (interval.equalsIgnoreCase("quarter")) {
                                                c = c / 3;
                                            }
                                            c = Math.round(c * 100.00) / 100.00;
                                            total += c;
                                            projectskillmap.put(time, c);
                                        }
                                        projectskillmap.put("TOTAL", Math.round(total * 100.00) / 100.00);
                                        datalist.add(projectskillmap);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (datalist.size() > 0) {
                ret.put("data", datalist);
                ret.put("col", collist);
            }

            if (ret != null && ret.keySet().size() > 0) {
                return ret;
            }
        }
        return null;
    }

    @Override
    public LinkedHashMap getProjectReportGroupStatus(String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType) {
        LinkedHashMap ret = new LinkedHashMap();
        List datalist = new ArrayList();
        List collist = new ArrayList();
        Map colmap = new HashMap();
        colmap.put("name", "Project");
        colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
        collist.add(colmap);

        DateTime dt = fromdt.plusDays(1);
        List timelist = new ArrayList();
        while (dt.getMillis() < todt.getMillis()) {
            colmap = new HashMap();
            if (interval.equalsIgnoreCase("year")) {
                colmap.put("name", convertFiscalYear(dt));
                colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
                timelist.add(Integer.toString(convertFiscalYear(dt)));
            } else if (interval.equalsIgnoreCase("quarter")) {
                colmap.put("name", convertFiscalQuarter(dt));
                colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
                timelist.add(convertFiscalQuarter(dt));
            } else {
                colmap.put("name", dt.toString(dfmt6));
                colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
                timelist.add(dt.toString(dfmt6));
            }
            colmap.put("enableFiltering", false);
            collist.add(colmap);

            if (interval.equalsIgnoreCase("year")) {
                dt = dt.plusYears(1);
            } else if (interval.equalsIgnoreCase("quarter")) {
                dt = dt.plusMonths(3);
            } else {
                dt = dt.plusMonths(1);
            }
        }
        colmap = new HashMap();
        colmap.put("name", "Total");
        colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
        collist.add(colmap);
        ret.put("col", collist);
        LinkedHashMap allGroupMap = new LinkedHashMap();
        List<ResourceProgramClassification> types = resourceProgramClassificationService.findByType("project", true);
        if (types == null || types.isEmpty())
            return null;
        for (ResourceProgramClassification g : types) {
            List<String> list = Arrays.asList(g.getProgramList().toLowerCase().split(","));
            TreeMap datamap = resourceActualSearchService.getProjectReportGroup(list, interval, fromdt, todt, isCSGOnly, employeeType);
            LinkedHashMap totalColumnMap = new LinkedHashMap();
            if (datamap != null && datamap.keySet().size() > 0) {
                for (Object dc : datamap.keySet()) {
                    Map dcmap = new HashMap();
                    if (datamap.get(dc) instanceof TreeMap) {
                        dcmap = (TreeMap) datamap.get(dc);
                    } else {
                        dcmap = (HashMap) datamap.get(dc);
                    }
                    if (dcmap != null && dcmap.keySet().size() > 0) {
                        LinkedHashMap projectskillmap = new LinkedHashMap();
                        projectskillmap.put("Project", TextUtil.formatName(dc.toString()));
                        Double rowTotal = Double.valueOf(0.00);
                        Double count = Double.valueOf(0.00);
                        LinkedHashMap datasubtm = new LinkedHashMap();
                        for (Object d : dcmap.keySet()) {
                            DateTime subdatadt = dfmt6.parseDateTime(d.toString());
                            String keydt = "";
                            if (interval.equalsIgnoreCase("year")) {
                                keydt = Integer.toString(convertFiscalYear(subdatadt));
                            } else if (interval.equalsIgnoreCase("quarter")) {
                                keydt = convertFiscalQuarter(subdatadt);
                            } else {
                                keydt = subdatadt.toString(dfmt6);
                            }

                            count = Double.valueOf(0.00);
                            if (datasubtm.containsKey(keydt)) {
                                count = (Double) datasubtm.get(keydt);
                            }
                            count = Double.valueOf(count.doubleValue() + ((Double) dcmap.get(d)).doubleValue());
                            datasubtm.put(keydt, count);
                        }
                        for (Object time : timelist) {
                            Double c = Double.valueOf(0.00);
                            Double columnTotal = Double.valueOf(0.00);
                            if (totalColumnMap.containsKey(time)) {
                                columnTotal = (Double) totalColumnMap.get(time);
                            }
                            if (datasubtm.containsKey(time)) {
                                c = (Double) datasubtm.get(time);
                            }
                            if (interval.equalsIgnoreCase("year")) {
                                c = Double.valueOf(c.doubleValue() / 12.0);
                            } else if (interval.equalsIgnoreCase("quarter")) {
                                c = Double.valueOf(c.doubleValue() / 3.0);
                            }
                            c = Double.valueOf(Math.round(c.doubleValue() * 100.00) / 100.00);
                            columnTotal = Double.valueOf(columnTotal.doubleValue() + c.doubleValue());
                            totalColumnMap.put(time.toString(), Double.valueOf(Math.round(columnTotal.doubleValue() * 100.00) / 100.00));

                            rowTotal = Double.valueOf(rowTotal.doubleValue() + c.doubleValue());
                            projectskillmap.put(time, c);
                        }
                        projectskillmap.put("Total", Double.valueOf(Math.round(rowTotal.doubleValue() * 100.00) / 100.00));
                        datalist.add(projectskillmap);
                    }
                }

                LinkedHashMap projectskillmap = new LinkedHashMap();
                projectskillmap.put("Project", g.getName().toUpperCase());
                Double rowTotal = Double.valueOf(0.00);
                for (Object time : timelist) {
                    if (totalColumnMap.containsKey(time)) {
                        projectskillmap.put(time, totalColumnMap.get(time));
                        rowTotal = Double.valueOf(rowTotal.doubleValue() + ((Double) totalColumnMap.get(time)).doubleValue());
                    }
                    Double colTotal = Double.valueOf(0.00);
                    if (allGroupMap.containsKey(time)) {
                        colTotal = (Double) allGroupMap.get(time);
                    }
                    colTotal = Double.valueOf(colTotal.doubleValue() + ((Double) totalColumnMap.get(time)).doubleValue());
                    allGroupMap.put(time.toString(), Double.valueOf(Math.round(colTotal.doubleValue() * 100.00) / 100.00));
                }
                projectskillmap.put("Total", Double.valueOf(Math.round(rowTotal.doubleValue() * 100.00) / 100.00));
                datalist.add(projectskillmap);

                projectskillmap = new LinkedHashMap();
                projectskillmap.put("Project", "");
                for (Object time : timelist) {
                    projectskillmap.put(time, "");
                }
                projectskillmap.put("Total", "");
                datalist.add(projectskillmap);
            }
        }
        LinkedHashMap projectskillmap = new LinkedHashMap();
        Double rowTotal = Double.valueOf(0.00);
        projectskillmap.put("Project", "TOTAL");
        for (Object time : timelist) {
            if (allGroupMap.containsKey(time)) {
                projectskillmap.put(time, allGroupMap.get(time));
                rowTotal = Double.valueOf(rowTotal.doubleValue() + ((Double) allGroupMap.get(time)).doubleValue());
            }
        }
        projectskillmap.put("Total", Double.valueOf(Math.round(rowTotal.doubleValue() * 100.00) / 100.00));
        datalist.add(projectskillmap);

        projectskillmap = new LinkedHashMap();
        projectskillmap.put("Project", "");
        for (Object time : timelist) {
            projectskillmap.put(time, "");
        }
        projectskillmap.put("Total", "");
        datalist.add(projectskillmap);
        if (datalist.size() > 0) {
            ret.put("data", datalist);
        }
        return ret;
    }

    @Override
    public LinkedHashMap getProjectSkillReportGroupStatus(String interval, DateTime fromdt, DateTime todt, Boolean isCSGOnly, String employeeType) {
        LinkedHashMap ret = new LinkedHashMap();
        List datalist = new ArrayList();
        List collist = new ArrayList();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Map colmap = new HashMap();
        colmap.put("name", "Project");
        colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
        collist.add(colmap);


        colmap = new HashMap();
        colmap.put("name", "Skill");
        colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
        collist.add(colmap);

        DateTime dt = fromdt.plusDays(1);
        List timelist = new ArrayList();
        while (dt.getMillis() < todt.getMillis()) {
            colmap = new HashMap();
            if (interval.equalsIgnoreCase("year")) {
                colmap.put("name", convertFiscalYear(dt));
                colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
                timelist.add(Integer.toString(convertFiscalYear(dt)));
            } else if (interval.equalsIgnoreCase("quarter")) {
                colmap.put("name", convertFiscalQuarter(dt));
                colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
                timelist.add(convertFiscalQuarter(dt));
            } else {
                colmap.put("name", dt.toString(dfmt6));
                colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
                timelist.add(dt.toString(dfmt6));
            }
            colmap.put("enableFiltering", false);
            collist.add(colmap);

            if (interval.equalsIgnoreCase("year")) {
                dt = dt.plusYears(1);
            } else if (interval.equalsIgnoreCase("quarter")) {
                dt = dt.plusMonths(3);
            } else {
                dt = dt.plusMonths(1);
            }
        }
        colmap = new HashMap();
        colmap.put("name", "Total");
        colmap.put("cellTemplate", "<div ng-bind-html='row.entity[col.field]'></div>");
        collist.add(colmap);
        ret.put("col", collist);

        LinkedHashMap allGroupMap = new LinkedHashMap();
        List<ResourceProgramClassification> types = resourceProgramClassificationService.findByType("project", true);
        if (types == null || types.isEmpty())
            return null;
        for (ResourceProgramClassification g : types) {
            List<String> list = Arrays.asList(g.getProgramList().toLowerCase().split(","));
            TreeMap datamap = resourceActualSearchService.getProjectSkillReportGroup(list, interval, fromdt, todt, isCSGOnly, employeeType);
            LinkedHashMap projectGroupColumnMap = new LinkedHashMap();
            if (datamap != null && datamap.keySet().size() > 0) {
                for (Object projectname : datamap.keySet()) {
                    Map psmap = new HashMap();
                    if ((datamap.get(projectname) instanceof TreeMap)) {
                        psmap = (TreeMap) datamap.get(projectname);
                    } else {
                        psmap = (HashMap) datamap.get(projectname);
                    }
                    if (psmap != null && psmap.keySet().size() > 0) {
                        LinkedHashMap projectColumnMap = new LinkedHashMap();
                        for (Object skill : psmap.keySet()) {
                            Map projectskillmap = new LinkedHashMap();
                            projectskillmap.put("Project", TextUtil.formatName(projectname.toString()));
                            Double rowTotal = Double.valueOf(0.00);
                            Double count = Double.valueOf(0.00);
                            projectskillmap.put("Skill", TextUtil.formatName(skill.toString()));

                            Map skillmap = new HashMap();
                            if ((psmap.get(skill) instanceof TreeMap)) {
                                skillmap = (TreeMap) psmap.get(skill);
                            } else
                                skillmap = (HashMap) psmap.get(skill);
                            LinkedHashMap datasubtm;
                            Iterator localIterator4;
                            if (skillmap != null && skillmap.keySet().size() > 0) {
                                datasubtm = new LinkedHashMap();
                                for (Object d : skillmap.keySet()) {
                                    DateTime subdatadt = dfmt6.parseDateTime(d.toString());
                                    String keydt = "";
                                    if (interval.equalsIgnoreCase("year")) {
                                        keydt = Integer.toString(convertFiscalYear(subdatadt));
                                    } else if (interval.equalsIgnoreCase("quarter")) {
                                        keydt = convertFiscalQuarter(subdatadt);
                                    } else {
                                        keydt = subdatadt.toString(dfmt6);
                                    }

                                    count = Double.valueOf(0.00);
                                    if (datasubtm.containsKey(keydt)) {
                                        count = (Double) datasubtm.get(keydt);
                                    }
                                    count = Double.valueOf(count.doubleValue() + ((Double) skillmap.get(d)).doubleValue());
                                    datasubtm.put(keydt, count);
                                }
                                for (Object time : timelist) {
                                    Double c = Double.valueOf(0.00);
                                    Double projectColumnTotal = Double.valueOf(0.00);
                                    if (projectColumnMap.containsKey(time)) {
                                        projectColumnTotal = (Double) projectColumnMap.get(time);
                                    }

                                    if (datasubtm.containsKey(time)) {
                                        c = (Double) datasubtm.get(time);
                                    }
                                    if (interval.equalsIgnoreCase("year")) {
                                        c = Double.valueOf(c.doubleValue() / 12.0);
                                    } else if (interval.equalsIgnoreCase("quarter")) {
                                        c = Double.valueOf(c.doubleValue() / 3.0);
                                    }
                                    c = Double.valueOf(Math.round(c.doubleValue() * 100.00) / 100.00);
                                    rowTotal = Double.valueOf(rowTotal.doubleValue() + c.doubleValue());
                                    projectColumnTotal = Double.valueOf(projectColumnTotal.doubleValue() + c.doubleValue());
                                    projectColumnMap.put(time.toString(), Double.valueOf(Math.round(projectColumnTotal.doubleValue() * 100.00) / 100.00));

                                    projectskillmap.put(time, c);
                                }
                            }

                            projectskillmap.put("Total", Double.valueOf(Math.round(rowTotal.doubleValue() * 100.00) / 100.00));
                            datalist.add(projectskillmap);
                        }
                        LinkedHashMap projectskillmap = new LinkedHashMap();
                        projectskillmap.put("Project", TextUtil.formatName(projectname.toString()));
                        projectskillmap.put("Skill", "TOTAL");
                        Double rowTotal = Double.valueOf(0.00);
                        for (Object time : timelist) {
                            Double projectGroupColumnTotal = Double.valueOf(0.00);
                            if (projectGroupColumnMap.containsKey(time)) {
                                projectGroupColumnTotal = (Double) projectGroupColumnMap.get(time);
                            }
                            if (projectColumnMap.containsKey(time)) {
                                projectskillmap.put(time, projectColumnMap.get(time));
                                rowTotal = Double.valueOf(rowTotal.doubleValue() + ((Double) projectColumnMap.get(time)).doubleValue());
                                projectGroupColumnTotal = Double.valueOf(projectGroupColumnTotal.doubleValue() + ((Double) projectColumnMap.get(time)).doubleValue());
                            }
                            projectGroupColumnMap.put(time.toString(), Double.valueOf(Math.round(projectGroupColumnTotal.doubleValue() * 100.00) / 100.00));
                        }
                        projectskillmap.put("Total", Double.valueOf(Math.round(rowTotal.doubleValue() * 100.00) / 100.00));
                        datalist.add(projectskillmap);

                        //add new blank line for seperation
                        projectskillmap = new LinkedHashMap();
                        projectskillmap.put("Project", "");
                        projectskillmap.put("Skill", "");
                        for (Object time : timelist) {
                            projectskillmap.put(time, "");
                        }
                        projectskillmap.put("Total", "");
                        datalist.add(projectskillmap);
                    }
                }
                LinkedHashMap projectskillmap = new LinkedHashMap();
                projectskillmap.put("Project", g.getName().toUpperCase());
                Double rowTotal = Double.valueOf(0.00);
                projectskillmap.put("Skill", g.getName().toUpperCase());
                for (Object time : timelist) {
                    if (projectGroupColumnMap.containsKey(time)) {
                        projectskillmap.put(time, projectGroupColumnMap.get(time));
                        rowTotal = Double.valueOf(rowTotal.doubleValue() + ((Double) projectGroupColumnMap.get(time)).doubleValue());
                    }
                    Double colTotal = Double.valueOf(0.00);
                    if (allGroupMap.containsKey(time)) {
                        colTotal = (Double) allGroupMap.get(time);
                    }
                    colTotal = Double.valueOf(colTotal.doubleValue() + ((Double) projectGroupColumnMap.get(time)).doubleValue());
                    allGroupMap.put(time.toString(), Double.valueOf(Math.round(colTotal.doubleValue() * 100.00) / 100.00));
                }
                projectskillmap.put("Total", Double.valueOf(Math.round(rowTotal.doubleValue() * 100.00) / 100.00));
                datalist.add(projectskillmap);

                projectskillmap = new LinkedHashMap();
                projectskillmap.put("Project", "");
                projectskillmap.put("Skill", "");
                for (Object time : timelist) {
                    projectskillmap.put(time, "");
                }
                projectskillmap.put("Total", "");
                datalist.add(projectskillmap);
            }
        }
        LinkedHashMap projectskillmap = new LinkedHashMap();
        Double rowTotal = Double.valueOf(0.00);
        projectskillmap.put("Project", "TOTAL");
        for (Object time : timelist) {
            if (allGroupMap.containsKey(time)) {
                projectskillmap.put(time, allGroupMap.get(time));
                rowTotal = Double.valueOf(rowTotal.doubleValue() + ((Double) allGroupMap.get(time)).doubleValue());
            }
        }
        projectskillmap.put("Total", Double.valueOf(Math.round(rowTotal.doubleValue() * 100.00) / 100.00));
        datalist.add(projectskillmap);

        projectskillmap = new LinkedHashMap();
        projectskillmap.put("Project", "");
        for (Object time : timelist) {
            projectskillmap.put(time, "");
        }
        projectskillmap.put("Total", "");
        datalist.add(projectskillmap);
        if (datalist.size() > 0) {
            ret.put("data", datalist);
        }
        return ret;
    }

    @Override
    public Set<String> getDistinctValue(String colName, DateTime startDate, DateTime stopDate, Boolean isCSGOnly) {
        Set<String> ret = resourceActualSearchService.getDistinctValue(colName, startDate, stopDate, isCSGOnly);
        if (ret != null && ret.size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public TreeMap getActualEmployeeData(DateTime fromdt, DateTime todt, Boolean isCSGOnly) {
        //List<Map> datamap = resourceActualSearchServ.getActualEmployeeData(charged_from, charged_to, fromdt, todt);
        return null;
    }

    private String convertFiscalQuarter(DateTime dt) {
        String q = (dt.getYear() + 1) + " Q1";
        if (dt.getMonthOfYear() == 1)
            q = dt.getYear() + " Q1";
        else if (dt.getMonthOfYear() > 1 && dt.getMonthOfYear() < 5)
            q = dt.getYear() + " Q2";
        else if (dt.getMonthOfYear() > 4 && dt.getMonthOfYear() < 8)
            q = dt.getYear() + " Q3";
        else if (dt.getMonthOfYear() > 7 && dt.getMonthOfYear() < 11)
            q = dt.getYear() + " Q4";

        return q;
    }

    private int convertFiscalYear(DateTime dt) {
        if (dt.getMonthOfYear() > 10)
            return (dt.getYear() + 1);

        return dt.getYear();
    }


}
