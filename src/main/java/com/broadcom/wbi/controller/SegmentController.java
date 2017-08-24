package com.broadcom.wbi.controller;

import com.broadcom.wbi.exception.CustomGenericException;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.EmployeeService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.service.jpa.RevisionService;
import com.broadcom.wbi.service.jpa.SegmentService;
import com.broadcom.wbi.util.ProjectConstant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/segment")
public class SegmentController {
    final static DateTimeFormatter dfmt = DateTimeFormat.forPattern("MM/dd/yy");
    private static final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private SegmentService segmentService;
    @Autowired
    private RevisionService revisionService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private RedisCacheRepository redis;
    @Autowired
    private IndicatorService indicatorServ;

    @RequestMapping(value = {"/getActiveSegments"}, method = {RequestMethod.GET})
    public Callable<List> getSegment(HttpServletRequest req,
                                     @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        return new Callable<List>() {
            public List call() throws JsonProcessingException {
                String redisk = "activeSegment";
                List segments = new ArrayList();
                if (reload == 1 || !redis.hasKey(redisk)) {
                    segments = segmentService.findAllActive();
                    if (segments != null && segments.size() > 0) {
                        redis.put(redisk, mapper.writeValueAsString(segments));
                        return segments;
                    }
                    return null;
                }
                String value = (String) redis.get(redisk);
                try {
                    segments = (List) mapper.readValue(value, new TypeReference<ArrayList>() {
                    });
                    return segments;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    @RequestMapping(value = {"/getPrograms"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getSegmentProgram(HttpServletRequest req,
                                                @RequestParam(value = "segment", defaultValue = "") final String seg,
                                                @RequestParam(value = "status", defaultValue = "true") final boolean status,
                                                @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() throws IOException {
                if (seg.trim().isEmpty())
                    throw new CustomGenericException("Segment Name can not be empty");
                final List<HashMap<String, Object>> ret = Collections.synchronizedList(new ArrayList());
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Employee user = employeeService.findByAccountName(username);
                if (user == null && username.matches(".*\\d+.*")) {
                    username = username.substring(3);
                    try {
                        user = employeeService.findById(Integer.parseInt(username));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                if (user == null)
                    throw new CustomGenericException("User not found");
                final Employee employee = user;
                final List<Integer> bookmarkRevivions = revisionService.findByEmployee(employee);
                String redisk1 = seg.toLowerCase() + "_" + status + "_frontPageSegment";
                String redisk2 = username + "_" + seg.toLowerCase() + "_" + status + "_frontPageSegment";
                if (reload == 1 || !redis.hasKey(redisk2)) {
                    if (reload == 1 || !redis.hasKey(redisk1)) {
                        ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.valueOf("CHIP");
                        Segment segment = segmentService.findByName(seg);
                        if (segment == null)
                            throw new CustomGenericException(seg + " can not be found in database");

                        if (seg.toLowerCase().indexOf("software") == 0) {
                            ptype = ProjectConstant.EnumProgramType.SOFTWARE;
                        } else if (seg.toLowerCase().indexOf("customer") == 0) {
                            ptype = ProjectConstant.EnumProgramType.CUSTOMER;
                        } else if (seg.toLowerCase().indexOf("ip") == 0) {
                            ptype = ProjectConstant.EnumProgramType.IP;
                        }
                        List<RevisionSearch> rsl = new ArrayList<RevisionSearch>();
                        if (status)
                            rsl = revisionSearchService.findBySegment(segment.getName().toLowerCase(),
                                    ptype.toString().toLowerCase(), true);
                        else
                            rsl = revisionSearchService.findBySegment(segment.getName().toLowerCase(),
                                    ptype.toString().toLowerCase(), false);
                        if (rsl != null) {
                            ExecutorService executor = Executors.newFixedThreadPool(20);
                            for (final RevisionSearch rs : rsl) {
                                executor.submit(new Runnable() {
                                    public void run() {
                                        if (rs.getType().equalsIgnoreCase("software")
                                                && !rs.getRev_name().toLowerCase().startsWith("program")) {
                                            return;
                                        }
                                        if (rs.getType().equalsIgnoreCase("ip")
                                                && rs.getRev_name().toLowerCase().startsWith("head")) {
                                            return;
                                        }
                                        HashMap hm = indicatorServ.getFrontPageRevisionInfo(Integer.parseInt(rs.getId()));
                                        ret.add(hm);
                                    }
                                });
                            }
                            executor.shutdown();
                            try {
                                executor.awaitTermination(10, TimeUnit.MINUTES);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                            if (ret.size() > 0) {
                                try {
                                    redis.put(redisk1, mapper.writeValueAsString(ret));
                                    redis.setExpire(redisk1, ProjectConstant.CacheTimeout.HOUR.getSecond());
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        String value = (String) redis.get(redisk1);
                        try {
                            List list = (List) mapper.readValue(value, new TypeReference<ArrayList>() {
                            });
                            ret.addAll(list);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    List list = new ArrayList();
                    if (ret.size() > 0) {
                        for (Object obj : ret) {
                            if (obj != null) {
                                HashMap hm = (HashMap) obj;
                                hm.put("bookmark", false);
                                int rid = Integer.parseInt(hm.get("rid").toString());
                                if (hm != null && hm.keySet().size() > 0) {
                                    if (bookmarkRevivions.contains(rid)) {
                                        hm.put("bookmark", true);
                                    }
                                }
                                list.add(hm);
                            }
                        }
                        if (list.size() > 0) {
                            try {
                                redis.put(redisk2, mapper.writeValueAsString(list));
                                redis.setExpire(redisk2, ProjectConstant.CacheTimeout.HOUR.getSecond());
                                return list;
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                } else {
                    String value = (String) redis.get(redisk2);
                    try {
                        List list = (List) mapper.readValue(value, new TypeReference<ArrayList>() {
                        });
                        ret.addAll(list);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return ret;
            }
        };
        return new WebAsyncTask<List>(1800000, callable);
    }
}
