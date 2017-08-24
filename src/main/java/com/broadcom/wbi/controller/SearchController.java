package com.broadcom.wbi.controller;

import com.broadcom.wbi.model.elasticSearch.RevisionInformationSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.elasticSearch.SkuSearch;
import com.broadcom.wbi.service.elasticSearch.RevisionInformationSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.elasticSearch.SkuSearchService;
import com.broadcom.wbi.util.TextUtil;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping({"/api/search"})
public class SearchController {
    final static DateTimeFormatter dfmt1 = DateTimeFormat.forPattern("MM/dd/yy");
    @Autowired
    private SkuSearchService skuSearchServ;
    @Autowired
    private RevisionInformationSearchService riSearchServ;
    @Autowired
    private RevisionSearchService revSearchServ;

    @RequestMapping(value = {"/program"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> searchSku(@RequestParam("term") final String term,
                                        @RequestParam(value = "type", defaultValue = "") final String type) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                List ret = Collections.synchronizedList(new ArrayList());
                List<SkuSearch> skulist = skuSearchServ.findBySkuNum(term);
                if (skulist != null) {
                    ExecutorService executor = Executors.newFixedThreadPool(5);
                    for (final SkuSearch sku : skulist) {
                        executor.submit(new Runnable() {
                            public void run() {
                                if (type.trim().isEmpty()
                                        || sku.getProgramType().toLowerCase().indexOf(type.toLowerCase()) == 0) {
                                    if (sku.getProgramDisplayName().toLowerCase().indexOf("_hidden") != -1)
                                        return;
                                    HashMap<String, String> hm = new HashMap<String, String>();
                                    if (sku.getUrl() == null || sku.getUrl().trim().isEmpty()) {
                                        RevisionSearch rs = revSearchServ.findByProgram(sku.getProgram(), "a0");
                                        if (rs == null)
                                            return;
                                        hm.put("url", "/program/" + sku.getProgramType().toLowerCase() + "/"
                                                + sku.getProgram() + "/" + rs.getId() + "/dashboard");
                                    } else {
                                        hm.put("url", sku.getUrl());
                                    }
                                    StringBuilder name = new StringBuilder();
                                    name.append("<span><strong>" + TextUtil.formatName(sku.getAka()));
                                    if (sku.getSkuNum().trim().length() > 2) {
                                        name.append(" " + sku.getSkuNum() + "</strong></span>");
                                    } else {
                                        name.append("</strong></span>");
                                    }
//                                    hm.put("formated", "<span><strong>" + TextUtil.formatName(sku.getAka()) + " "
//                                            + sku.getSkuNum() + "</strong></span>");
//                                    if (sku.getDescription().length() > 2) {
//                                        name.append("<span><strong>" + TextUtil.formatName(sku.getAka()) + " "
//                                                + sku.getSkuNum() + "</strong><p>" + sku.getDescription()
//                                                + "</p></span>");
//                                    }
//                                    hm.put("formated", name.toString());
                                    hm.put("pname", TextUtil.formatName(sku.getProgramDisplayName()));
                                    hm.put("aka", TextUtil.formatName(sku.getAka()));
                                    hm.put("num", sku.getSkuNum());
                                    hm.put("description", sku.getDescription());
                                    hm.put("ptype", sku.getProgramType().toLowerCase());
                                    ret.add(hm);
                                }
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(2, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(1800000, callable);
    }

    @RequestMapping(value = {"/information"}, method = {RequestMethod.GET})
    public Callable<List<HashMap<String, String>>> searchProgramInfo(
            @RequestParam(value = "rid", defaultValue = "") final int rid) {
        return new Callable<List<HashMap<String, String>>>() {
            public List<HashMap<String, String>> call() {
                List<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
                List<RevisionInformationSearch> pilist = riSearchServ.findByRevision(rid, true);
                if (pilist != null) {
                    for (RevisionInformationSearch pi : pilist) {
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("name", pi.getName());
                        hm.put("value", pi.getValue());
                        hm.put("phase", pi.getPhase());
                        hm.put("onDashboard", Boolean.toString(pi.getOnDashboard()));
                        ret.add(hm);
                    }
                    return ret;
                }
                return null;
            }
        };
    }

    @RequestMapping(value = {"/revision"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> searchRevision(@RequestParam("term") final String term,
                                             @RequestParam(value = "type", defaultValue = "") final String type) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                List ret = Collections.synchronizedList(new ArrayList());
                Set<Integer> pilist = skuSearchServ.searchBySkuNum(term);
                if (pilist != null) {
                    ExecutorService executor = Executors.newFixedThreadPool(5);
                    for (final Integer pid : pilist) {
                        executor.submit(new Runnable() {
                            public void run() {
                                List<RevisionSearch> rslist = revSearchServ.findByProgram(pid);
                                if (rslist != null && !rslist.isEmpty()) {
                                    for (RevisionSearch rs : rslist) {
                                        if (rs.getRev_name().toLowerCase().indexOf("head") == -1) {
                                            if (type.trim().isEmpty()
                                                    || rs.getType().toLowerCase().indexOf(type.toLowerCase()) == 0) {
                                                HashMap hm = new HashMap();
                                                hm.put("url", "/program/" + rs.getType().toLowerCase() + "/"
                                                        + rs.getProgram_id() + "/" + rs.getId() + "/dashboard");
                                                String pname = TextUtil.formatName(rs.getProgram_name())
                                                        + " " + rs.getRev_name().toUpperCase();
                                                if (rs.getType().equalsIgnoreCase("ip")) {
                                                    List<RevisionInformationSearch> risl = riSearchServ.findByRevisionPhaseName(Integer.parseInt(rs.getId()),
                                                            "current", "category");
                                                    if (risl != null && !risl.isEmpty()) {
                                                        pname = TextUtil.formatName(risl.get(0).getValue() + " " +
                                                                rs.getProgram_name()) + " " + rs.getRev_name().toUpperCase();
                                                    }
                                                }
                                                hm.put("formated",
                                                        "<span><strong>" + pname
                                                                + "</strong></span>");
                                                hm.put("pname", TextUtil.formatName(rs.getProgram_name()));
                                                hm.put("rname", rs.getRev_name().toUpperCase());
                                                hm.put("rid", Integer.parseInt(rs.getId()));
                                                hm.put("pid", pid);
                                                ret.add(hm);
                                            } else {
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(2, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(1800000, callable);
    }
}
