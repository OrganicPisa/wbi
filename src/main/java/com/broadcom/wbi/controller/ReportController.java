package com.broadcom.wbi.controller;


import com.broadcom.wbi.service.indicator.IndicatorReportService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.util.ProjectConstant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(value = "/api/report")
public class ReportController {

    private static final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private IndicatorReportService indicatorReportService;
    @Autowired
    private RedisCacheRepository redis;

    @RequestMapping(value = {"/milestone/{type}/collect"}, method = {RequestMethod.GET})
    public WebAsyncTask<Map> getMilestoneReport(HttpServletRequest req, @PathVariable final String type,
                                                @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                                @RequestParam(value = "status", defaultValue = "true") final String statusString) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                if (!type.equalsIgnoreCase("internal") && !type.equalsIgnoreCase("customer")
                        && !type.equalsIgnoreCase("email") && !type.equalsIgnoreCase("ip"))
                    return null;
                ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
                if (type.toLowerCase().indexOf("customer") == 0) {
                    ptype = ProjectConstant.EnumProgramType.CUSTOMER;
                } else if (type.toLowerCase().indexOf("ip") == 0) {
                    ptype = ProjectConstant.EnumProgramType.IP;
                }
                String redisk = ptype.toString().toLowerCase() + statusString.toLowerCase() + "_milestoneReport";
                if (reload == 1 || !redis.hasKey(redisk)) {
                    Map data = indicatorReportService.generateMilestoneReport(ptype, statusString);
                    TreeMap ret = new TreeMap(data);
                    if (ret.keySet().size() > 0) {
                        try {
                            redis.put(redisk, mapper.writeValueAsString(ret));
                            redis.setExpire(redisk, ProjectConstant.CacheTimeout.DAY.getSecond());
                            return ret;
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redis.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return new TreeMap(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }

    @RequestMapping(value = {"/headline/{type}/collect"}, method = {RequestMethod.GET})
    public WebAsyncTask<Map> getHeadlineReport(HttpServletRequest req, @PathVariable String type,
                                               @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                               @RequestParam(value = "status", defaultValue = "true") final String statusString,
                                               @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                if (!type.equalsIgnoreCase("internal") && !type.equalsIgnoreCase("customer")
                        && !type.equalsIgnoreCase("ip"))
                    return null;
                ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
                if (type.toLowerCase().indexOf("customer") == 0) {
                    ptype = ProjectConstant.EnumProgramType.CUSTOMER;
                } else if (type.toLowerCase().indexOf("ip") == 0) {
                    ptype = ProjectConstant.EnumProgramType.IP;
                }
                String redisk = ptype.toString().toLowerCase() + "_headlineReport";
                if (reload == 1 || !redis.hasKey(redisk)) {
                    Map data = indicatorReportService.generateHeadlineReport(ptype, statusString);
                    TreeMap ret = new TreeMap(data);
                    if (ret.keySet().size() > 0) {
                        try {
                            redis.put(redisk, mapper.writeValueAsString(ret));
                            redis.setExpire(redisk, ProjectConstant.CacheTimeout.DAY.getSecond());
                            return ret;
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redis.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return new TreeMap(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }
}
