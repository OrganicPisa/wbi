package com.broadcom.wbi.controller;

import com.broadcom.wbi.exception.CustomGenericException;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.ResourcePlan;
import com.broadcom.wbi.model.mysql.ResourceProgramClassification;
import com.broadcom.wbi.model.mysql.SkillMapping;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.*;
import com.broadcom.wbi.service.resource.parser.DataResourceParseService;
import com.broadcom.wbi.service.resource.program.ResourceProjectService;
import com.broadcom.wbi.service.resource.report.ResourceReportService;
import com.broadcom.wbi.util.ProjectConstant;
import com.broadcom.wbi.util.TextUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressWarnings({"rawtypes", "unchecked"})
@RestController
@RequestMapping({"/api/resource"})
public class ResourceController {
    final static DateTimeFormatter dfmt1 = org.joda.time.format.DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
    final static DateTimeFormatter dfmt2 = org.joda.time.format.DateTimeFormat.forPattern("MM/dd/yy");
    final static DateTimeFormatter dfmt3 = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM");
    final static DateTimeFormatter dfmt4 = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
    static final ObjectMapper mapper = new ObjectMapper();
    private final RevisionSearchService revisionSearchService;
    private final IndicatorService indicatorService;
    private final ResourceProjectService resourceProjectService;
    private final ResourceReportService resourceReportService;
    private final ResourcePlanService resourcePlanService;
    private final RedisCacheRepository redisCacheRepository;
    private final ResourceProgramClassificationService resourceProgramClassificationService;
    private final ProgramService programService;
    private final SkillMappingService skillMappingService;

    private final DataResourceParseService dataResourceParseService;

    @Autowired
    public ResourceController(RevisionSearchService revisionSearchService, IndicatorService indicatorService,
                              ResourceProjectService resourceProjectService, ResourceReportService resourceReportService,
                              ResourcePlanService resourcePlanService, RedisCacheRepository redisCacheRepository,
                              ResourceProgramClassificationService resourceProgramClassificationService, ProgramService programService, SkillMappingService skillMappingService,
                              DataResourceParseService dataResourceParseService) {
        this.revisionSearchService = revisionSearchService;
        this.indicatorService = indicatorService;
        this.resourceProjectService = resourceProjectService;
        this.resourceReportService = resourceReportService;
        this.resourcePlanService = resourcePlanService;
        this.redisCacheRepository = redisCacheRepository;
        this.resourceProgramClassificationService = resourceProgramClassificationService;
        this.programService = programService;
        this.skillMappingService = skillMappingService;
        this.dataResourceParseService = dataResourceParseService;
    }

    @RequestMapping(value = {"/index", "/", "/home"}, method = {RequestMethod.GET})
    public ModelAndView index(HttpServletRequest req) {
        return new ModelAndView("/resource/index");
    }

    @RequestMapping(value = {"/{type}"}, method = {RequestMethod.GET})
    public ModelAndView type(HttpServletRequest req, @PathVariable String type) {
        return new ModelAndView("/resource/" + type);
    }

    @RequestMapping(value = {"/program/getResourceSummaryTable"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> getResourceSummaryTable(HttpServletRequest req,
                                                     @RequestParam(value = "rid", defaultValue = "0") final int rid,
                                                     @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<Map> callable = new Callable<Map>() {
            public Map call() {
                if (rid < 1)
                    return null;
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                if (rs == null)
                    throw new CustomGenericException("Revision Not found in database");
                String redisk = rs.getId() + "_resourceSummaryTable";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map ret = null;
                    Map<String, DateTime> tm = indicatorService.getKeyProjectDate(Integer.parseInt(rs.getId()));
                    if (tm != null && tm.keySet().size() > 0) {
                        if (!tm.containsKey("pc") || tm.get("pc") == null) {
                            if (rs.getRev_name().equalsIgnoreCase("a0"))
                                return null;
                        } else {
                            ret = resourceProjectService.getResourceSummaryTable(rs, tm);

                            if (ret != null && ret.size() > 0) {
                                try {
                                    redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                                    return ret;
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (TreeMap) mapper.readValue(value, new TypeReference<TreeMap>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(120000, callable);
    }

    @RequestMapping(value = {"/program/getResourceSummaryChart"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> getResourceMonthlyChart(final HttpServletRequest req,
                                                     @RequestParam(value = "rid", defaultValue = "0") final int rid,
                                                     @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<Map> callable = new Callable<Map>() {
            public Map call() {
                if (rid < 1)
                    return null;
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                if (rs == null)
                    throw new CustomGenericException("Revision Not found in database");
                String redisk = rs.getId() + "_resourceSummaryChart";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map ret = null;
                    Map<String, DateTime> tm = indicatorService.getKeyProjectDate(Integer.parseInt(rs.getId()));
                    if (tm != null && tm.keySet().size() > 0) {
                        if (!tm.containsKey("pc") || tm.get("pc") == null) {
                            if (rs.getRev_name().equalsIgnoreCase("a0"))
                                return null;
                        } else {
                            ret = resourceProjectService.getResourceGroupByMonthChart(rs, tm);
                            if (ret != null && ret.size() > 0) {
                                try {
                                    redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                                    return ret;
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (TreeMap) mapper.readValue(value, new TypeReference<TreeMap>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;

            }
        };

        return new WebAsyncTask<Map>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @RequestMapping(value = {"/program/delete"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> clearProgramResource(final HttpServletRequest req,
                                                  @RequestParam(value = "pid", defaultValue = "0") final int pid) {
        Callable<Map> callable = new Callable<Map>() {
            public Map call() {
                if (pid < 1)
                    return null;
                Program program = programService.findById(pid);
                if (program == null)
                    return null;
                List<ResourcePlan> resourcePlanList = resourcePlanService.findByProgram(program);
                if (resourcePlanList != null && !resourcePlanList.isEmpty()) {
                    for (ResourcePlan resourcePlan : resourcePlanList)
                        resourcePlanService.delete(resourcePlan.getId());
                }
                List<SkillMapping> skillMappingList = skillMappingService.findByProgram(program);
                if (skillMappingList != null && !skillMappingList.isEmpty()) {
                    for (SkillMapping skillMapping : skillMappingList)
                        skillMappingService.delete(skillMapping.getId());
                }
                return null;

            }
        };

        return new WebAsyncTask<Map>(120000, callable);
    }

    @RequestMapping(value = {"/program/getResourceSummarySkill"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> getResourceSkillSummary(HttpServletRequest req,
                                                     @RequestParam(value = "rid", defaultValue = "0") final int rid,
                                                     @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<Map> callable = new Callable<Map>() {
            public Map call() {
                if (rid < 1)
                    return null;
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                if (rs == null)
                    throw new CustomGenericException("Revision Not found in database");

                String redisk = rs.getProgram_id() + "_resourceSkillSummaryTable";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map ret = null;
                    Map<String, DateTime> tm = indicatorService.getKeyProjectDate(Integer.parseInt(rs.getId()));
                    if (tm != null && tm.keySet().size() > 0) {
                        if (!tm.containsKey("pc") || tm.get("pc") == null) {
                            if (rs.getRev_name().equalsIgnoreCase("a0"))
                                return null;
                        } else {
                            ret = resourceProjectService.getResourceSkillSummaryTable(rs, tm);
                            if (ret != null && ret.size() > 0) {
                                try {
                                    redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                                    return ret;
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(120000, callable);
    }

    @RequestMapping(value = {"/program/getActualResourceTimes"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<List> getProgramActualResourceTime(
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            public List call() {
                if (rid == 0)
                    return null;
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                if (rs == null)
                    throw new CustomGenericException("Revision Not found in database");

                String currentdt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
                        .toString(dfmt4);
                String redisk = rs.getProgram_id() + "_resourceProgramTimeList";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Set<String> data = resourceProjectService.getActualTimeList(rs);
                    if (data == null || data.isEmpty())
                        return null;
                    List ret = new ArrayList();
                    for (String d : data) {
                        ret.add(d);
                        if (d.equals(currentdt))
                            break;
                    }
                    if (ret != null && ret.size() > 0) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    List ret = (ArrayList) mapper.readValue(value, new TypeReference<ArrayList>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(120000, callable);
    }

    @RequestMapping(value = {"/program/generateActualEmployeeResource"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> generateProgramActualEmployeeResource(
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "date", defaultValue = "2001-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") final Date da,
            @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<Map> callable = new Callable<Map>() {
            public Map call() {
                if (rid == 0)
                    return null;
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                if (rs == null)
                    throw new CustomGenericException("Revision Not found in database");
                Map ret = new HashMap();
                DateTime dt = new DateTime(da).dayOfMonth().withMinimumValue().minusDays(1).withTimeAtStartOfDay();
                if (dt.getYear() < 2002) {
                    dt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                }
                List<Map> data = resourceProjectService.getActualEmployeeData(rs, dt);
                if (data != null && data.size() > 0) {
                    List dataList = new ArrayList();
                    for (Map map : data) {
                        LinkedHashMap hm = new LinkedHashMap();
                        hm.put("Employee", WordUtils.capitalizeFully(map.get("employee").toString()));
                        hm.put("Skill", map.get("skill").toString().toUpperCase());
                        hm.put("Project", map.get("project").toString().toUpperCase());
                        hm.put("Manager", WordUtils.capitalizeFully(map.get("manager").toString()));
                        hm.put("Man Month", map.get("count"));
                        dataList.add(hm);
                    }
                    ret.put("data", dataList);
                    return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(120000, callable);
    }


    /*******************************************************************************************************************
     * Resource Report
     *****************************************************************************************************************/
    @RequestMapping(value = {"/report/generateCurrentMonthStatus"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<List> generateCurrentMonthStatus(HttpServletRequest req,
                                                         @RequestParam(value = "filter", defaultValue = "none") final String filterBy,
                                                         @RequestParam(value = "employeeType", defaultValue = "all") final String employeeType,
                                                         @RequestParam(value = "groupBy", defaultValue = "") final String groupBy,
                                                         @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                List ret = new ArrayList();
                DateTime dt = new DateTime().dayOfMonth().withMinimumValue().minusDays(1).withTimeAtStartOfDay();
                boolean isFilter = false;
                if (!filterBy.equalsIgnoreCase("none"))
                    isFilter = true;
                String redisk = "resource_" + isFilter + "_" + employeeType + "_" + groupBy + "_" + dt.toDate() + "_currentResourceReportStatus";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    ret = resourceReportService.getCurrentReportGroupStatus(groupBy, isFilter, employeeType);
                    if (ret != null && ret.size() > 0) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;

                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    ret = mapper.readValue(value, new TypeReference<ArrayList>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        };
        return new WebAsyncTask<List>(120000, callable);
    }

    @RequestMapping(value = {"/report/generateProjectTrendStatus"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<LinkedHashMap> getProjectReportGroupStatus(HttpServletRequest req,
                                                                   @RequestParam(value = "intervalGroup", defaultValue = "month") final String intervalGroup,
                                                                   @RequestParam(value = "filter", defaultValue = "none") final String filterBy,
                                                                   @RequestParam(value = "type", defaultValue = "project") final String type,
                                                                   @RequestParam(value = "employeeType", defaultValue = "all") final String employeeType,
                                                                   @RequestParam(value = "from", defaultValue = "01/01/2001") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date startda,
                                                                   @RequestParam(value = "to", defaultValue = "01/01/2001") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date stopda,
                                                                   @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<LinkedHashMap> callable = new Callable<LinkedHashMap>() {
            @Override
            public LinkedHashMap call() {
                DateTime fromdt = new DateTime(startda).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                DateTime todt = new DateTime(stopda).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                if (todt.getYear() < 2002) {
                    todt = new DateTime().withMonthOfYear(10).dayOfMonth().withMaximumValue().withTimeAtStartOfDay();
                } else {
                    todt = new DateTime(stopda).plusDays(1).withTimeAtStartOfDay();
                }
                if (fromdt.getYear() < 2002) {
                    fromdt = new DateTime().withMonthOfYear(11).dayOfMonth().withMinimumValue().minusYears(1).minusDays(1).withTimeAtStartOfDay();
                }
                boolean isFilter = false;
                if (!filterBy.equalsIgnoreCase("none"))
                    isFilter = true;
                String redisk = "resource_" + isFilter + "_" + employeeType + "_" + type + "_" + intervalGroup + "_" + startda + "_" + stopda + "_trendResourceProjectGroup";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                LinkedHashMap ret = new LinkedHashMap();
                if (!redisCacheRepository.hasKey(redisk)) {
                    List typeArr = Arrays.asList(type.split(","));
                    if (typeArr.size() == 1)
                        ret = resourceReportService.getProjectReportGroupStatus(intervalGroup, fromdt, todt, isFilter, employeeType);
                    else if (typeArr.size() == 2)
                        ret = resourceReportService.getProjectSkillReportGroupStatus(intervalGroup, fromdt, todt, isFilter, employeeType);
                    if (ret != null && ret.keySet().size() > 0) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    ret = mapper.readValue(value, new TypeReference<LinkedHashMap>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        };
        return new WebAsyncTask<LinkedHashMap>(120000, callable);
    }

    @RequestMapping(value = {"/report/generateTrendStatus"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<TreeMap> generateTrendStatus(HttpServletRequest req,
                                                     @RequestParam(value = "filter", defaultValue = "none") final String filterBy,
                                                     @RequestParam(value = "employeeType", defaultValue = "all") final String employeeType,
                                                     @RequestParam(value = "returnType", defaultValue = "table") final String return_type,
                                                     @RequestParam(value = "groupBy", defaultValue = "") final String group_by,
                                                     @RequestParam(value = "intervalGroup", defaultValue = "month") final String intervalGroup,
                                                     @RequestParam(value = "from", defaultValue = "01/01/2001") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date startda,
                                                     @RequestParam(value = "to", defaultValue = "01/01/2001") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date stopda,
                                                     @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<TreeMap> callable = new Callable<TreeMap>() {
            @Override
            public TreeMap call() {
                List<String> charged_froms = new ArrayList<String>();

                List<String> group_bys = new ArrayList<String>();
                if (!group_by.trim().isEmpty()) {
                    group_bys = Arrays.asList(group_by.split("\\,"));
                }
                DateTime fromdt = new DateTime(startda).dayOfMonth().withMinimumValue().minusDays(1).withTimeAtStartOfDay();
                DateTime todt = new DateTime(stopda).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                if (todt.getYear() < 2002) {
                    todt = new DateTime().withMonthOfYear(10).dayOfMonth().withMaximumValue().withTimeAtStartOfDay();
                } else {
                    todt = new DateTime(stopda).plusDays(1).withTimeAtStartOfDay();
                }
                if (fromdt.getYear() < 2002) {
                    fromdt = new DateTime().withMonthOfYear(11).dayOfMonth().withMinimumValue().minusYears(1).minusDays(1).withTimeAtStartOfDay();
                }
                boolean isFilter = false;
                if (!filterBy.equalsIgnoreCase("none"))
                    isFilter = true;
                String redisk = "resource_" + isFilter + "_" + employeeType + "_"
                        + group_by.toString() + "_" + intervalGroup + "_" + return_type + "_" + startda + "_" + stopda
                        + "_trendResourceReportStatus";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                TreeMap ret = new TreeMap();
                if (!redisCacheRepository.hasKey(redisk)) {
                    if (group_bys.size() == 1) {
                        if (return_type.equalsIgnoreCase("chart"))
                            ret = resourceReportService.generateTrendGroupChart(group_bys.get(0), intervalGroup, fromdt, todt, isFilter, employeeType);
                        else
                            ret = resourceReportService.getTrend1GroupStatus(group_bys.get(0), intervalGroup, fromdt, todt, isFilter, employeeType);

                    } else if (group_bys.size() == 2) {
                        ret = resourceReportService.getTrend2GroupStatus(group_bys.get(0), group_bys.get(1), intervalGroup, fromdt, todt, isFilter, employeeType);
                    } else if (group_bys.size() == 3) {
                        ret = resourceReportService.getTrend3GroupStatus(group_bys.get(0), group_bys.get(1), group_bys.get(2), intervalGroup, fromdt, todt, isFilter, employeeType);
                    }
                    if (ret != null && ret.keySet().size() > 0) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    ret = mapper.readValue(value, new TypeReference<TreeMap>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        };
        return new WebAsyncTask<TreeMap>(120000, callable);
    }

    @RequestMapping(value = {"/report/getAllProjectGroup"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<List> getAllProjectGroup(HttpServletRequest req,
                                                 @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                List<ResourceProgramClassification> gl = resourceProgramClassificationService.listAll();
                List ret = new ArrayList();
                if (gl != null && !gl.isEmpty()) {
                    for (ResourceProgramClassification g : gl) {
                        if (g.getProgramList() == null) continue;
                        HashMap map = new HashMap();
                        map.put("name", TextUtil.formatName(g.getName()));
                        map.put("inReport", g.getStatus());
                        map.put("type", g.getType().toLowerCase());
                        map.put("id", g.getId());
                        List<String> list = new ArrayList<String>();
                        list = Arrays.asList(g.getProgramList().trim().replaceAll("^\\,", "").split(","));
                        map.put("projects", list);
                        ret.add(map);
                    }
                    if (!ret.isEmpty())
                        return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(120000, callable);
    }

    @RequestMapping(value = {"/report/getDistinctProjectByType"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<HashMap> getDistinctProjectByType(HttpServletRequest req,
                                                          @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                                          @RequestParam(value = "id", defaultValue = "0") final int id) {
        Callable<HashMap> callable = new Callable<HashMap>() {
            @Override
            public HashMap call() {
                if (id < 1)
                    return null;
                ResourceProgramClassification g = resourceProgramClassificationService.findById(id);
                if (g != null) {
                    HashMap ret = new HashMap();
                    List<String> list = new ArrayList<String>();
                    list = Arrays.asList(g.getProgramList().trim().replaceAll("^\\,", "").split(","));
                    ret.put("projects", list);
                    ret.put("type", g.getType());
                    ret.put("inReport", g.getStatus());
                    return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<HashMap>(120000, callable);
    }

    @RequestMapping(value = {"/report/getDistinctValue"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<List> getDistinctValue(HttpServletRequest req,
                                               @RequestParam(value = "filter", defaultValue = "none") final String filterBy,
                                               @RequestParam(value = "type", defaultValue = "") final String type,
                                               @RequestParam(value = "from", defaultValue = "01/01/2001") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date startda,
                                               @RequestParam(value = "to", defaultValue = "01/01/2001") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date stopda,
                                               @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                if (type.isEmpty())
                    return null;
                DateTime fromdt = new DateTime(startda).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                DateTime todt = new DateTime(stopda).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                if (todt.getYear() < 2002) {
                    todt = new DateTime().dayOfYear().withMaximumValue().plusYears(1).withTimeAtStartOfDay();
                } else {
                    todt = new DateTime(stopda).plusDays(1).withTimeAtStartOfDay();
                }
                if (fromdt.getYear() < 2002) {
                    fromdt = new DateTime().minusYears(10).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
                }
                boolean isFilter = false;
                if (!filterBy.equalsIgnoreCase("none"))
                    isFilter = true;
                List ret = new ArrayList();
                Set<String> distinctList = resourceReportService.getDistinctValue(type, fromdt, todt, isFilter);
                if (distinctList != null && !distinctList.isEmpty()) {
                    for (String value : distinctList) {
                        ret.add(TextUtil.formatName(value));

                    }
                    if (ret != null && ret.size() > 0) {
                        return ret;
                    }
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
    @RequestMapping(value = {"/report/saveProjectClassificationList"}, method = {RequestMethod.POST})
    @ResponseStatus(value = HttpStatus.CREATED)
    public Callable<String> saveprojectGroup(HttpServletRequest req, @RequestBody final HashMap hm) {
        return new Callable<String>() {
            public String call() {
                if (!hm.containsKey("name") || hm.get("name").toString().isEmpty())
                    return null;
                boolean status = true;
                final String username = SecurityContextHolder.getContext().getAuthentication().getName();
                ProjectConstant.EnumResourceProgramClassificationType type = ProjectConstant.EnumResourceProgramClassificationType.REPORT;
                if (hm.containsKey("includeInReport")) {
                    status = Boolean.parseBoolean(hm.get("includeInReport").toString());
                }
                final String name = hm.get("name").toString().toLowerCase();
                StringBuilder sb = new StringBuilder();
                if (hm.containsKey("list")) {
                    if (hm.get("list") instanceof List)
                        sb.append(String.join(",", (ArrayList) hm.get("list")));
                }
                int id = 0;

                if (hm.containsKey("type")) {
                    if (hm.get("type").toString().toLowerCase().indexOf("project") != -1) {
                        type = ProjectConstant.EnumResourceProgramClassificationType.PROJECT;
                    }
                }
                if (hm.containsKey("id")) {
                    try {
                        id = Integer.parseInt(hm.get("id").toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                ResourceProgramClassification g = null;
                if (id > 0) {
                    g = resourceProgramClassificationService.findById(id);
                    g.setStatus(status);
                    g.setName(name.toLowerCase());
                    g.setType(type.toString().toLowerCase());
                    g.setProgramList(sb.toString());
                    resourceProgramClassificationService.saveOrUpdate(g);
                } else {
                    g = new ResourceProgramClassification();
                    g.setName(name.toLowerCase());
                    g.setType(type.toString().toLowerCase());
                    g.setStatus(status);
                    if (sb.length() == 0) {
                        if (name.equalsIgnoreCase("active")) {
                            List<RevisionSearch> rsl = revisionSearchService.findByProgramType(ProjectConstant.EnumProgramType.CHIP, true);
                            Set<String> pname = new HashSet<String>();
                            if (rsl != null && !rsl.isEmpty()) {
                                for (RevisionSearch rs : rsl) {
                                    if (rs.getProgram_name().toLowerCase().indexOf("hidden") == -1) {
                                        if (rs.getSegment().toLowerCase().indexOf("software") == -1 &&
                                                rs.getSegment().toLowerCase().indexOf("sw") == -1 &&
                                                rs.getSegment().toLowerCase().indexOf("test") == -1 &&
                                                rs.getSegment().toLowerCase().indexOf("ip") != 0)
                                            pname.add(TextUtil.formatName(rs.getProgram_name()));
                                    }
                                }
                            }
                            sb.append(String.join(",", pname));
                        }
                        g.setProgramList(sb.toString());
                    }
                    g = resourceProgramClassificationService.saveOrUpdate(g);

                }

                return null;
            }
        };
    }

    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
    @RequestMapping(value = {"/report/deleteProjectClassificationList"}, method = {RequestMethod.POST})
    @ResponseStatus(value = HttpStatus.OK)
    public Callable<String> deleteprojectGroup(HttpServletRequest req, @RequestBody final HashMap hm) {
        return new Callable<String>() {
            public String call() {
                int id = 0;
                if (hm.containsKey("id")) {
                    try {
                        id = Integer.parseInt(hm.get("id").toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (id > 0) {
                    ResourceProgramClassification g = resourceProgramClassificationService.findById(id);
                    if (g != null)
                        resourceProgramClassificationService.delete(id);
                }

                return null;
            }
        };
    }

    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
    @RequestMapping(value = {"/report/clearCache"}, method = {RequestMethod.POST})
    @ResponseStatus(value = HttpStatus.OK)
    public Callable<String> clearCache(HttpServletRequest req) {
        return new Callable<String>() {
            public String call() {
                redisCacheRepository.deleteWildCard("resource_*");
                return null;
            }
        };
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @RequestMapping(value = {"/sync"}, method = {RequestMethod.POST})
    @ResponseStatus(value = HttpStatus.OK)
    public Callable<String> syncData(HttpServletRequest req) {
        return new Callable<String>() {
            public String call() {
                DateTime lastmonth = new DateTime().minusMonths(2).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                DateTime stopdt = new DateTime().dayOfYear().withMaximumValue().withTimeAtStartOfDay();
                DateTime current = lastmonth;
                while (current.getMillis() < stopdt.getMillis()) {
                    dataResourceParseService.cleanup(current);
                    dataResourceParseService.doCollectAndInsertData(current);
                    current = current.plusMonths(1);
                }
                redisCacheRepository.deleteWildCard("resource_*");
                return null;
            }
        };
    }

}
