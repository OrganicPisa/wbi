package com.broadcom.wbi.service.information;

import com.broadcom.wbi.model.elasticSearch.HeadlineSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionInformationSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.service.elasticSearch.HeadlineSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionInformationSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.util.ProjectConstant;
import com.broadcom.wbi.util.TextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class InformationReportServiceImpl implements InformationReportService {
    private final RevisionSearchService revisionSearchService;
    private final RevisionInformationSearchService revisionInformationSearchService;
    private final HeadlineSearchService headlineSearchService;
    private final IndicatorService indicatorService;

    @Autowired
    public InformationReportServiceImpl(RevisionSearchService revisionSearchService, RevisionInformationSearchService revisionInformationSearchService, HeadlineSearchService headlineSearchService, IndicatorService indicatorService) {
        this.revisionSearchService = revisionSearchService;
        this.revisionInformationSearchService = revisionInformationSearchService;
        this.headlineSearchService = headlineSearchService;
        this.indicatorService = indicatorService;
    }

    @Override
    public Map generateInformationReport(ProjectConstant.EnumProgramType ptype) {
        final List dataList = Collections.synchronizedList(new ArrayList());
        List<RevisionSearch> revisions = revisionSearchService.findByProgramType(ptype, true);
        if (revisions == null)
            return null;
        LinkedHashSet<String> headerSet = new LinkedHashSet();
        if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
            headerSet.add("ca");
            headerSet.add("pc");
            headerSet.add("ecr1");
            headerSet.add("ecr2");
            headerSet.add("ecr3");
        }
        headerSet.add("current");
        if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
            headerSet.add("to/final");
        }


        List keyList = new ArrayList();
        HashMap hm = new HashMap();
        Set<String> keys = new LinkedHashSet<String>();
        if (ptype.equals(ProjectConstant.EnumProgramType.CUSTOMER)) {
            keys.add("customer");
            hm = new HashMap();
            hm.put("field", "customer");
            hm.put("name", "customer");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("program");
            hm = new HashMap();
            hm.put("field", "program");
            hm.put("name", "program");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);
        } else if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
            keys.add("program");
            hm = new HashMap();
            hm.put("field", "program");
            hm.put("name", "chip");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("revision");
            hm = new HashMap();
            hm.put("field", "revision");
            hm.put("name", "rev");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "3%");
            keyList.add(hm);

            keys.add("base die");
            hm = new HashMap();
            hm.put("field", "basedie");
            hm.put("name", "base die");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("status");
            hm = new HashMap();
            hm.put("field", "status");
            hm.put("name", "s");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");

            keyList.add(hm);

            keys.add("ca");
            hm = new HashMap();
            hm.put("field", "ca");
            hm.put("name", "ca");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("pc");
            hm = new HashMap();
            hm.put("field", "pc");
            hm.put("name", "pc");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);
        }
        RevisionSearch rev = revisions.get(0);
        if (rev != null) {
            List<RevisionInformationSearch> riList = revisionInformationSearchService.findByRevision(Integer.parseInt(rev.getId()));
            for (RevisionInformationSearch ris : riList) {
                String key = ris.getName().toLowerCase().trim().replaceAll("\\(.*\\)", "").trim();
                if (!keys.contains(key) && !key.equalsIgnoreCase("code name")
                        && !key.equalsIgnoreCase("program aka")) {
                    keys.add(key);
                    HashMap map = new HashMap();
                    map.put("field", key.replaceAll("\\s", ""));
                    map.put("name", key);
                    map.put("cellTemplate",
                            "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
                    if (key.equalsIgnoreCase("to date"))
                        hm.put("width", "5%");
                    keyList.add(map);
                }
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    LinkedHashMap revhm = new LinkedHashMap();
                    HashMap hm = new HashMap();
                    if (ptype.equals(ProjectConstant.EnumProgramType.CUSTOMER)) {
                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getBase_num()));
                        revhm.put("customer", hm);
                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getProgram_name()));
                        revhm.put("program", hm);
                    } else if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
                        String status = "Inactive";
                        hm.put("program", TextUtil.formatName(rs.getProgram_name()));
                        hm.put("revision", rs.getRev_name().toUpperCase());
                        hm.put("basedie", TextUtil.formatName(rs.getBase_num()));

                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getBase_num()));
                        revhm.put("basedie", hm);
                        hm = new HashMap();
                        hm.put("latest", rs.getRev_name().toUpperCase());
                        revhm.put("revision", hm);
                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getProgram_name()));
                        revhm.put("program", hm);

                        HeadlineSearch hls = headlineSearchService.findByRevision(Integer.parseInt(rs.getId()), null);
                        if (hls != null) {
                            if (hls.getStatus().toLowerCase().indexOf("active") == 0) {
                                status = "Active";
                            }
                        }
                        hm = new HashMap();
                        hm.put("latest", status);
                        revhm.put("status", hm);

                        List frontPageMilestone = indicatorService.getFrontPageMilestone(Integer.parseInt(rs.getId()));
                        if (frontPageMilestone != null) {
                            for (Object milestone : frontPageMilestone) {
                                Map tmap = null;
                                if (milestone instanceof LinkedHashMap)
                                    tmap = (LinkedHashMap) milestone;
                                else if (milestone instanceof HashMap)
                                    tmap = (HashMap) milestone;
                                if (tmap != null) {
                                    if (tmap.get("key").toString().equalsIgnoreCase("t/o")) {
                                        String value = tmap.get("key").toString().replaceAll("\\<[^>]*>", "");
                                        hm = new HashMap();
                                        hm.put("latest", value);
                                        revhm.put(tmap.get("key").toString(), hm);
                                    }
                                }
                            }
                        }
                    }

                    hm = new HashMap();
                    hm.put("latest", rs.getSegment().toUpperCase());
                    revhm.put("segment", hm);

                    LinkedHashMap infomap = revisionInformationSearchService.getRevisionInformationReport(Integer.parseInt(rs.getId()), "detail");
                    if (infomap.containsKey("data")) {
                        HashMap map = (HashMap) infomap.get("data");
                        Boolean ecr1 = Boolean.valueOf(infomap.get("displayecr1").toString());
                        Boolean ecr2 = Boolean.valueOf(infomap.get("displayecr2").toString());
                        Boolean ecr3 = Boolean.valueOf(infomap.get("displayecr3").toString());
                        for (Object k : map.keySet()) {
                            String key = k.toString().toLowerCase().replaceAll("\\(.*\\)", "")
                                    .replaceAll("\\s", "").trim();
                            hm = new HashMap();
                            if (revhm.containsKey(key))
                                hm = (HashMap) revhm.get(key);
                            HashMap fieldmap = (HashMap) map.get(k);
                            floop:
                            for (Object f : fieldmap.keySet()) {
                                String field = f.toString().toLowerCase();
                                HashMap fielddata = (HashMap) fieldmap.get(f);
                                String value = fielddata.get("value").toString().trim();
                                if (field.indexOf("ecr") != -1) {
                                    if (ecr1 || ecr2 || ecr3) {
                                        field = "ecr";
                                    } else
                                        continue floop;
                                }
                                if (!hm.containsKey(field) || (hm.containsKey(field) && !value.isEmpty()))
                                    hm.put(field, value);
                                if (!hm.containsKey("latest")
                                        || (hm.containsKey("latest") && !value.isEmpty()))
                                    hm.put("latest", value);
                            }
                            revhm.put(key, hm);
                        }
                        dataList.add(revhm);
                    }
                }

            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (dataList.size() > 0) {
            Map ret = new HashMap();
            ret.put("title", keyList);
            ret.put("data", dataList);
            ret.put("keys", keys);
            return ret;
        }
        return null;

    }
}
