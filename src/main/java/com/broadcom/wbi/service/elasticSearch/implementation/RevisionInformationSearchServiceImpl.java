package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.elasticSearch.*;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionIP;
import com.broadcom.wbi.repository.elasticSearch.RevisionInformationSearchRepository;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.jpa.RevisionIPService;
import com.broadcom.wbi.service.jpa.RevisionService;
import com.broadcom.wbi.util.TextUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RevisionInformationSearchServiceImpl implements RevisionInformationSearchService {

    @Autowired
    private RevisionInformationSearchRepository repo;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private RevisionSearchService revSearchServ;
    @Autowired
    private RevisionService revServ;
    @Autowired
    private RevisionIPService revIPServ;
    @Autowired
    private IndicatorTaskSearchService itaskSearchServ;
    @Autowired
    private IndicatorDateSearchService idateSearchServ;
    @Autowired
    private HeadlineSearchService hlSearchServ;

    @Override
    public RevisionInformationSearch saveOrUpdate(RevisionInformationSearch info) {

        return repo.save(info);
    }

    @Override
    public void saveBulk(List<RevisionInformationSearch> infos) {
        repo.save(infos);
    }

    @Override
    public void delete(String info) {
        repo.delete(info);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public void emptyData() {
        repo.deleteAll();
    }

    @Override
    public void deleteIndex() {
        template.deleteIndex(RevisionInformationSearch.class);
    }

    @Override
    public List<RevisionInformationSearch> findByDateTime(DateTime dt) {
        return null;
    }

    @Override
    public RevisionInformationSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<RevisionInformationSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<RevisionInformationSearch> findByRevision(Integer rid, boolean onDashboard) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision", rid))
                        .must(QueryBuilders.termQuery("onDashboard", onDashboard))
                ).withSort(SortBuilders.fieldSort("orderNum").order(SortOrder.ASC)).build();
        List<RevisionInformationSearch> infos = repo.search(searchQuery).getContent();
        return infos;
    }

    @Override
    public List<RevisionInformationSearch> findByRevision(Integer rid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.termQuery("revision", rid)
                ).withSort(SortBuilders.fieldSort("orderNum").order(SortOrder.ASC)).build();
        List<RevisionInformationSearch> infos = repo.search(searchQuery).getContent();
        return infos;
    }

    @Override
    public List<RevisionInformationSearch> findByRevision(Integer rid, String title) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision", rid))
                        .must(QueryBuilders.wildcardQuery("name", title.toLowerCase().trim() + "*"))
                ).withSort(SortBuilders.fieldSort("orderNum").order(SortOrder.ASC)).build();
        List<RevisionInformationSearch> infos = repo.search(searchQuery).getContent();
        return infos;
    }

    @Override
    public List<RevisionInformationSearch> findByRevisionPhaseName(Integer rid, String phase, String title) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision", rid))
                        .must(QueryBuilders.wildcardQuery("phase", phase.toLowerCase().trim()))
                        .must(QueryBuilders.wildcardQuery("name", title.toLowerCase().trim()))
                ).withSort(SortBuilders.fieldSort("orderNum").order(SortOrder.ASC)).build();
        List<RevisionInformationSearch> info = repo.search(searchQuery).getContent();
        if (info != null && !info.isEmpty()) {
            return info;
        }
        return null;
    }


    @Override
    public List<RevisionInformationSearch> findByRevisionPhase(Integer rid, String phase) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision", rid))
                        .must(QueryBuilders.termQuery("phase", phase.toLowerCase().trim()))
                ).withSort(SortBuilders.fieldSort("orderNum").order(SortOrder.ASC)).build();
        List<RevisionInformationSearch> info = repo.search(searchQuery).getContent();
        if (info != null && !info.isEmpty()) {
            return info;
        }
        return null;
    }

    @Override
    public Set<String> getDistinctValue(Integer rid, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("revision", rid));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(AggregationBuilders.terms("col").field(colName).size(1000))
                .withSort(SortBuilders.fieldSort("orderNum").order(SortOrder.ASC))
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("col");
        Set<String> ret = new TreeSet<String>();
        DateTime dt = new DateTime();
        for (Terms.Bucket bucket : agg.getBuckets()) {
            if (colName.toLowerCase().indexOf("month") != -1) {
                dt = new DateTime(bucket.getKeyAsNumber());
                ret.add(dt.toString(dfmt));
            } else {
                ret.add(bucket.getKeyAsString().toUpperCase());
            }
        }
        if (ret != null && ret.size() > 0) {
            ret.add("PROGRAM NAME");
            return ret;
        }
        return null;
    }

    @Override
    public List<RevisionInformationSearch> findByName(String name) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("name", name.toLowerCase().trim()))
                ).withSort(SortBuilders.fieldSort("name").order(SortOrder.ASC)).build();
        List<RevisionInformationSearch> infos = repo.search(searchQuery).getContent();
        return infos;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public LinkedHashMap getRevisionInformationReport(Integer rid, String infoType) {
        LinkedHashMap ret = new LinkedHashMap();
        RevisionSearch rs = revSearchServ.findById(Integer.toString(rid));
        if (rs == null)
            throw new IDNotFoundException(rid, "revision");
        LinkedHashSet<String> headerSet = new LinkedHashSet();
        if (rs.getType().equalsIgnoreCase("chip")) {
            headerSet.add("ca");
            headerSet.add("pc");
        }
        //get phases
        Set<String> phases = getDistinctValue(rid, "phase");
        if (phases == null)
            return null;
        for (String col : phases) {
            if (!col.trim().isEmpty() && col.toLowerCase().indexOf("ecr") != -1) {
                headerSet.add(col.toLowerCase());
            }
        }
        headerSet.add("current");
        if (rs.getType().equalsIgnoreCase("chip")) {
            headerSet.add("to/final");
        }
        if (infoType.equalsIgnoreCase("dashboard")) {
            List datalist = new ArrayList();
            List revChangeList = new ArrayList();
            Map settingmap = new HashMap();
            //get dashboard
            HashMap hm = new HashMap();
            ret.put("program_name", TextUtil.formatName(rs.getProgram_name()));

            if (rs.getType().equals("customer")) {
                hm = new HashMap();
                ret.put("customer_name", TextUtil.formatName(rs.getBase_num()));
            } else if (rs.getType().equals("chip")) {
                ret.put("base_die", TextUtil.formatName(rs.getBase_num()));
                ret.put("segment", rs.getSegment().toUpperCase());
            } else if (rs.getType().equals("ip")) {
                HeadlineSearch hs = hlSearchServ.findByRevision(Integer.parseInt(rs.getId()), new DateTime());
                if (hs != null) {
                    hm = new HashMap();
                    hm.put("key", "Stage");
                    hm.put("value", TextUtil.formatName(hs.getStage()));
                    hm.put("order", 3);
                    hm.put("editable", true);
                    settingmap.put("stage", hm);

                    hm = new HashMap();
                    hm.put("key", "Status");
                    hm.put("value", hs.getStatus());
                    hm.put("order", 1);
                    hm.put("editable", true);
                    settingmap.put("status", hm);
                }
            }

            List<RevisionInformationSearch> onDashboardList = findByRevision(rid, true);
            HashMap tempmap = new HashMap();
            for (String header : headerSet) {
                for (RevisionInformationSearch ris : onDashboardList) {
                    if (ris.getPhase().equalsIgnoreCase(header)) {
                        String key = TextUtil.formatName(ris.getName());
                        if (tempmap.containsKey(key)) {
                            if (!ris.getValue().trim().isEmpty()) {
                                tempmap.put(key, ris.getId() + "&&" + ris.getOrderNum() + "&&" + TextUtil.formatName(ris.getValue()));
                            }
                        } else {
                            tempmap.put(key, ris.getId() + "&&" + ris.getOrderNum() + "&&" + TextUtil.formatName(ris.getValue()));
                        }
                    }
                }
            }
            if (tempmap.keySet().size() > 0) {
                for (Object key : tempmap.keySet()) {
                    String[] arrvalue = tempmap.get(key).toString().split("&&");
                    hm = new HashMap();
                    hm.put("key", key);
                    hm.put("value", "");
                    hm.put("order", 0);
                    hm.put("editable", true);
                    hm.put("id", 0);
                    if (arrvalue.length > 0) {
                        hm.put("id", Integer.parseInt(arrvalue[0]));
                    }
                    if (arrvalue.length > 1) {
                        Integer order = Integer.parseInt(arrvalue[1]);
                        if (rs.getType().equalsIgnoreCase("chip"))
                            order += 2;
                        else
                            order += 1;
                        hm.put("order", order);
                    }
                    if (arrvalue.length > 2) {
                        String val = arrvalue[2];
                        if (key.toString().equalsIgnoreCase("technology"))
                            val = arrvalue[2].toLowerCase();
                        hm.put("value", val);
                    }
                    if (rs.getType().equalsIgnoreCase("ip")) {
                        if (key.toString().equalsIgnoreCase("category") ||
                                key.toString().equalsIgnoreCase("technology") ||
                                key.toString().equalsIgnoreCase("type") ||
                                key.toString().equalsIgnoreCase("dft")) {
                            ret.put(key.toString().toLowerCase(), hm);
                        } else if (key.toString().equalsIgnoreCase("revision changes") ||
                                key.toString().equalsIgnoreCase("compared to revision")) {
                            revChangeList.add(hm);
                        } else {
                            datalist.add(hm);
                        }
                    } else {
                        datalist.add(hm);
                    }
                }
            }
            if (rs.getType().equalsIgnoreCase("ip")) {
                List<RevisionInformationSearch> pil = findByRevision(Integer.parseInt(rs.getId()), true);
                if (pil != null && !pil.isEmpty()) {
                    for (RevisionInformationSearch pi : pil) {
                        hm.put(pi.getName(), pi.getValue());
                    }
                }
                Revision iprev = revServ.findById(rid);
                List<RevisionIP> riplist = revIPServ.findByRevisionIP(iprev);
                StringBuilder sb = new StringBuilder();
                if (riplist != null && !riplist.isEmpty()) {
                    for (RevisionIP rip : riplist) {
                        Revision rev = rip.getRevision();
                        RevisionSearch rs1 = revSearchServ.findById(Integer.toString(rev.getId()));
                        sb.append("<a href='/program/" + rs1.getType().toLowerCase() + "/" + rs1.getProgram_id() + "/" +
                                rs1.getId() + "/dashboard'>" + TextUtil.formatName(rs1.getProgram_name()) + " " + rs1.getRev_name().toUpperCase() + "</a> (" + rip.getInstanceNum() + ")<br>");
                    }
                    if (sb.length() > 0) {
                        sb.setLength(sb.length() - 4);
                    }
                }

                ret.put("chip_instance", sb);

            }
            if (datalist.size() == 0 && settingmap.keySet().size() == 0)
                return null;
            if (datalist.size() > 0)
                ret.put("data", datalist);
            if (revChangeList.size() > 0)
                ret.put("revChange", revChangeList);
            if (settingmap.keySet().size() > 0)
                ret.put("setting", settingmap);
        } else {
            TreeMap detail = new TreeMap();
            //get all program information search
            boolean displayECR1 = false;
            boolean displayECR2 = false;
            boolean displayECR3 = false;

            List<RevisionInformationSearch> risl = findByRevision(rid);
            if (risl == null)
                return null;
            for (String header : headerSet) {
                for (RevisionInformationSearch ris : risl) {
                    if (ris.getPhase().equalsIgnoreCase(header)) {
                        LinkedHashMap map = new LinkedHashMap();
                        String k = ris.getOrderNum() + "+" + TextUtil.formatName(ris.getName().trim());
                        String key = k;
                        String value = ris.getValue().trim();
                        if (ris.getOrderNum().toString().length() == 1) {
                            key = "00" + k;
                        } else if (ris.getOrderNum().toString().length() > 1) {
                            key = "0" + k;
                        }

                        if (detail.containsKey(key)) {
                            map = (LinkedHashMap) detail.get(key);
                        }
                        if (ris.getPhase().equalsIgnoreCase("ecr1") && !value.isEmpty() && !displayECR1)
                            displayECR1 = true;
                        if (ris.getPhase().equalsIgnoreCase("ecr2") && !value.isEmpty() && !displayECR2)
                            displayECR2 = true;
                        if (ris.getPhase().equalsIgnoreCase("ecr3") && !value.isEmpty() && !displayECR3)
                            displayECR3 = true;

                        HashMap hm = new HashMap();
                        hm.put("id", ris.getId());
                        hm.put("editable", ris.getIsUserEditable());
                        if (ris.getName().equalsIgnoreCase("to_date")) {
                            value = "";
                            IndicatorTaskSearch its = itaskSearchServ.findByRevisionGroup(rid, "project", "t/o");
                            if (its != null) {
                                IndicatorDateSearch ids = null;
                                if (header.equalsIgnoreCase("current")) {
                                    ids = idateSearchServ.findByIndicatorTask(its.getTask_id(), "current_end");
                                } else if (header.equalsIgnoreCase("to/final")) {
                                    ids = idateSearchServ.findByIndicatorTask(its.getTask_id(), "actual_end");
                                }
                                if (ids != null) {
                                    DateTime ddt = new DateTime(ids.getValue());
                                    if (ddt.getYear() > 1980) {
                                        if (ddt.getHourOfDay() > 12) {
                                            ddt = ddt.plusDays(1);
                                        }
                                        value = ddt.toString(dfmt2);
                                    }
                                }
                            }
                        }
                        hm.put("value", value);
                        hm.put("rid", ris.getRevision());
                        hm.put("restrictedView", ris.getIsRestrictedView());
                        map.put(header, hm);
                        detail.put(key, map);
//						if(rs.getProgram_name().equalsIgnoreCase("sinai") && ris.getName().toString().toLowerCase().startsWith("process node")){
//							System.out.println(rs.getRev_name()+"--"+header+"--"+ris.getPhase()+" : "+value);
//						}
                    }
                }
            }
            LinkedHashMap headers = new LinkedHashMap();
            for (String header : headerSet) {
                if (header.toLowerCase().indexOf("ecr") != -1) {
                    if (header.equalsIgnoreCase("ecr1"))
                        headers.put("ecr1", displayECR1);
                    else if (header.equalsIgnoreCase("ecr2"))
                        headers.put("ecr2", displayECR2);
                    else if (header.equalsIgnoreCase("ecr3"))
                        headers.put("ecr3", displayECR3);
                } else {
                    headers.put(header, "true");
                }
            }
            ret.put("title", headers);
            LinkedHashMap data = new LinkedHashMap();
            for (Object key : detail.keySet()) {
                String[] ks = key.toString().split("\\+");
                //	if(ks.length==2){
                data.put(ks[1], detail.get(key));
                //	}
            }
            ret.put("data", data);
            ret.put("displayecr1", displayECR1);
            ret.put("displayecr2", displayECR2);
            ret.put("displayecr3", displayECR3);
        }
        return ret;
    }

    @Override
    public HashMap<String, String> findLatestByRevision(Integer rid) {
        RevisionSearch rs = revSearchServ.findById(Integer.toString(rid));
        if (rs == null)
            throw new IDNotFoundException(rid, "revision");
        HashMap<String, String> ret = new HashMap<String, String>();
        List<RevisionInformationSearch> pil = findByRevision(Integer.parseInt(rs.getId()));
        if (pil != null && !pil.isEmpty()) {
            for (RevisionInformationSearch pi : pil) {
                String key = pi.getName().toLowerCase().replaceAll("\\s", "");
                String value = pi.getValue().trim();
                if (!ret.containsKey(key) || (ret.containsKey(key) && !value.trim().isEmpty())) {
                    ret.put(key, value);
                    //System.out.println(pi.getName()+"---"+pi.getPhase()+"---"+pi.getValue());
                }
            }
        }

        return ret;
    }

}
