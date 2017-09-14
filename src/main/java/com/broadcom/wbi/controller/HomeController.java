package com.broadcom.wbi.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api")
public class HomeController {

    @RequestMapping(value = {"/getServerConfig"}, method = {RequestMethod.GET})
    public Callable<HashMap> getServerConfig(HttpServletRequest req) {
        return new Callable<HashMap>() {
            public HashMap call() {
                HashMap ret = new HashMap();
                ret.put("displayProjectBookmarkBtn", true);
                ret.put("displaySwReportBtn", true);
                ret.put("ws_url", "ws://10.16.75.35:61614/stomp");

                Map wsheadermap = new HashMap();
                wsheadermap.put("login", "admin");
                wsheadermap.put("passcode", "admin");
                wsheadermap.put("debug", false);

                ret.put("ws_headers", wsheadermap);

                return ret;
            }
        };
    }

    @RequestMapping(value = {"/getIPDropDownTemplate"}, method = {RequestMethod.GET})
    public Callable<HashMap> getIPDropDownTemplate(HttpServletRequest req) {
        return new Callable<HashMap>() {
            public HashMap call() {
                HashMap ret = new HashMap();
                LinkedHashMap technologyMap = new LinkedHashMap();
                technologyMap.put("7nm", "7nm");
                technologyMap.put("16nm", "16nm");
                technologyMap.put("28nm", "28nm");

                ret.put("technology", technologyMap);

                LinkedHashMap catMap = new LinkedHashMap();
                catMap.put("DDR", "DDR");
                catMap.put("iProc", "iProc");
                catMap.put("PCIE", "PCIE");
                catMap.put("PMD", "PMD");
                catMap.put("Port Macro", "Port Macro");

                ret.put("category", catMap);

                LinkedHashMap statusMap = new LinkedHashMap();
                statusMap.put("Active", "Active");
                statusMap.put("Non Active", "Non Active");
                ret.put("status", statusMap);

                LinkedHashMap stageMap = new LinkedHashMap();
                stageMap.put("Planning", "Planning");
                stageMap.put("Execution", "Execution");
                stageMap.put("Sustaining", "Sustaining");
                stageMap.put("Inactive", "InActive");
                ret.put("stage", stageMap);

                LinkedHashMap typeMap = new LinkedHashMap();
                typeMap.put("Analog", "Analog");
                typeMap.put("Digital Hard Macro", "Digital Hard Macro");
                typeMap.put("Digital Soft Macro", "Digital Soft Macro");
                ret.put("type", typeMap);

                LinkedHashMap dftMap = new LinkedHashMap();
                dftMap.put("Logic Vision", "Logic Vision");
                dftMap.put("Tessent", "Tessent");
                ret.put("dft", dftMap);

                return ret;
            }
        };
    }
}
