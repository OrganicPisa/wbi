package com.broadcom.wbi.service.resource.parser;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public interface DataResourceParseService {
    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd");
    DateTimeFormatter dtfm = DateTimeFormat.forPattern("M/d/yyyy");

    @SuppressWarnings("serial")
    Map<String, String> designCenterTableMap = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("TAIWAN", "TDC");
                    put("CHINA", "CDC");
                    put("CHN", "CDC");
                    put("SHANGHAI", "CDC");
                    put("NANJING", "CDC");
                    put("SHENZHEN", "CDC");
                    put("HONG KONG", "CDC");

                    put("UNITED KINGDOM", "EDC");
                    put("UK", "EDC");
                    put("SPAIN", "EDC");
                    put("SWEDEN", "EDC");
                    put("BULGARIA", "EDC");
                    put("GERMANY", "EDC");
                    put("ROMANIA", "EDC");
                    put("UKRAINE", "EDC");

                    put("ISRAEL", "YDC");
                    put("NAZARETH", "YDC");

                    put("INDIA", "IDC");
                    put("BANGALORE", "IDC");
                    put("HYDERABAD", "IDC");
                    put("USA", "SDC");
                    put("CANADA", "SDC");
                }
            }
    );

    @SuppressWarnings("serial")
    Map<String, String> approveSkillTableMap = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("AE", "1");
                    put("ARCH", "1");
                    put("AV", "1");
                    put("DE", "1");
                    put("DFT", "1");
                    put("DV", "1");
                    put("DVT", "1");
                    put("DE/DV", "1");
                    put("PD", "1");
                    put("DV", "1");
                    put("HW", "1");
                    put("SQA", "1");
                    put("PM", "1");
                    put("IPM", "1");
                    put("CPM", "1");
                    put("MKT", "1");
                    put("MGMT", "1");
                    put("SW", "1");
                    put("SW DESIGN", "1");
                    put("SV", "1");

                }
            }
    );

    @SuppressWarnings("serial")
    Map<String, String> approveProjectTableMap = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("NWS_AE", "1");
                    put("NWS_ENG_INFRA", "1");
                    put("NWS_ENG_SUSTAINING", "1");
                    put("NWS_ARCH_INFRA", "1");
                    put("NWS_ARCH_SUSTAINING", "1");
                    put("NWS_SW_INFRA", "1");
                    put("NWS_SW_SUSTAINING", "1");
                    put("NWS_PM_INFRA", "1");
                    put("NWS_MKT", "1");
                    put("NWS_MANAGEMENT", "1");
                }
            }
    );

    @Async
    void doCollectAndInsertData(DateTime dt);

    void cleanup(DateTime dt);
}
