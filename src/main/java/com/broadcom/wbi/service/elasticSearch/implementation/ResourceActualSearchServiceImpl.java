package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.ResourceActualSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.repository.elasticSearch.ResourceActualSearchRepository;
import com.broadcom.wbi.service.elasticSearch.ResourceActualSearchService;
import com.broadcom.wbi.util.TextUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
public class ResourceActualSearchServiceImpl implements ResourceActualSearchService {

    private final ElasticsearchTemplate template;
    @Resource
    private ResourceActualSearchRepository repo;

    @Autowired
    public ResourceActualSearchServiceImpl(ElasticsearchTemplate template) {
        this.template = template;
    }

    @Override
    public void emptyData() {
        repo.deleteAll();
    }

    @Override
    public ResourceActualSearch findOne(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<ResourceActualSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public DeleteQuery deleteByTime(DateTime dt) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("month").gte(dt.getMillis())));
        return deleteQuery;
    }

    /*******************************************************************************************
     * Resource planning for program
     *******************************************************************************************/

    @Override
    public List<Map> findByProgram(RevisionSearch rs, DateTime dt) {
        List<Map> ret = new ArrayList<Map>();
        DateTime startDate = dt.dayOfMonth().withMinimumValue().minusDays(1).withTimeAtStartOfDay();
        DateTime stopDate = dt.dayOfMonth().withMaximumValue().withTimeAtStartOfDay();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.wildcardQuery("project", rs.getProgram_name().toLowerCase().replaceAll("\\+", "plus")));
        query.must(QueryBuilders.rangeQuery("month").gt(startDate.getMillis()).lt(stopDate.getMillis()));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(query).build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        SearchHit[] results = hits.getHits().hits();
        for (SearchHit hit : results) {
            ret.add(hit.sourceAsMap());
        }
        return ret;
    }

    @Override
    public HashMap<String, TreeMap<String, Double>> groupByMonthSkill(RevisionSearch rs) {
        Set<String> skills = getProgramDistinctValue(rs, "skill");
        HashMap<String, TreeMap<String, Double>> ret = new HashMap<String, TreeMap<String, Double>>();
        final DateTime currentdt = new DateTime().dayOfMonth().withMinimumValue().plusMonths(1).withTimeAtStartOfDay();
        if (skills != null) {
            for (final String skill : skills) {
                BoolQueryBuilder query = QueryBuilders.boolQuery();
                query.must(QueryBuilders.wildcardQuery("project", rs.getProgram_name().toLowerCase().replaceAll("\\+", "plus")));
                query.must(QueryBuilders.wildcardQuery("skill", skill.toLowerCase()));
                SearchQuery searchQuery = new NativeSearchQueryBuilder()
                        .withQuery(query)
                        .addAggregation(AggregationBuilders.terms("month").field("month").size(1000)
                                .subAggregation(AggregationBuilders.sum("count").field("count")))
                        .build();

                SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
                    @Override
                    public SearchResponse extract(SearchResponse response) {
                        return response;
                    }
                });
                Terms agg = hits.getAggregations().get("month");
                TreeMap<String, Double> hm = new TreeMap<String, Double>();
                for (Terms.Bucket skillBucket : agg.getBuckets()) {
                    long mili = (Long) skillBucket.getKeyAsNumber();
                    DateTime dt = new DateTime(mili).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                    String ds = dt.toString(dfmt);
                    Sum countSum = skillBucket.getAggregations().get("count");
                    Double count = Math.round(countSum.getValue() * 100.0) / 100.0;
                    hm.put(ds, count);
                }
                if (hm.keySet().size() > 0) {
                    String lastKey = hm.lastKey();
                    DateTime dt = dfmt.parseDateTime(lastKey);
                    while (dt.getMillis() < currentdt.getMillis()) {
                        String ds = dt.toString(dfmt);
                        hm.put(ds, 0.0);
                        dt = dt.plusMonths(1);
                    }
                }
                ret.put(skill, hm);
            }
            return new HashMap<String, TreeMap<String, Double>>(ret);
        }
        return null;
    }

    @Override
    public TreeMap<String, Double> groupByMonth(RevisionSearch rs) {
        TreeMap<String, Double> ret = new TreeMap<String, Double>();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.wildcardQuery("project", rs.getProgram_name().toLowerCase().replaceAll("\\+", "plus")));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(AggregationBuilders.terms("month").field("month").size(1000)
                        .subAggregation(AggregationBuilders.sum("count").field("count")))
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("month");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            // get count
            long mili = (Long) bucket.getKeyAsNumber();
            DateTime dt = new DateTime(mili).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            String ds = dt.toString(dfmt);
            Sum countSum = bucket.getAggregations().get("count");
            Double count = Math.round(countSum.getValue() * 100.0) / 100.0;
            ret.put(ds, count);
        }
        return ret;

    }

    @Override
    public Set<String> getProgramDistinctValue(RevisionSearch rs, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.wildcardQuery("project", "*" + rs.getProgram_name().toLowerCase().replaceAll("\\+", "plus")));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(AggregationBuilders.terms("col").field(colName).size(1000))
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
        if (ret != null && ret.size() > 0)
            return ret;
        return null;
    }

    @Override
    public DateTime getLastUpdateTime(RevisionSearch rs) {
        DateTime startdt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        DateTime stopdt = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.wildcardQuery("project", rs.getProgram_name().toLowerCase().replaceAll("\\+", "plus")));
        query.must(QueryBuilders.rangeQuery("month").gte(startdt.getMillis()).lte(stopdt.getMillis()));

        TermsBuilder termsb = AggregationBuilders.terms("month").field("month");
        termsb.subAggregation(AggregationBuilders.topHits("last_updated_time").setSize(1).addSort("last_updated_time", SortOrder.DESC));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(termsb)
                .build();
        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });

        Terms agg = hits.getAggregations().get("month");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            TopHits max = bucket.getAggregations().get("last_updated_time");
            Map map = max.getHits().getAt(0).getSource();
            if (map.containsKey("last_updated_time")) {
                DateTime lastUpdatedDate = new DateTime(map.get("last_updated_time"));
                return lastUpdatedDate;
            }

        }
        return null;
    }

    /*******************************************************************************************
     * Resource actual for Trending Report
     *******************************************************************************************/
    @Override
    public TreeMap<String, Double> getCurrentReportGroup(String group_by, Boolean isCSGOnly, String employeeType) {
        TreeMap<String, Double> ret = new TreeMap<String, Double>();
        DateTime startDate = new DateTime().dayOfMonth().withMinimumValue().minusDays(1).minusYears(1).withTimeAtStartOfDay();
        DateTime stopDate = new DateTime().dayOfMonth().withMaximumValue().withTimeAtStartOfDay();
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if (!employeeType.equalsIgnoreCase("all")) {
            if (employeeType.equalsIgnoreCase("fte"))
                query.must(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            else {
                query.mustNot(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            }
        }
        if (isCSGOnly) {
            BoolQueryBuilder query1 = QueryBuilders.boolQuery();
            query1.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query1.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query1.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query1);
        }

        query.must(QueryBuilders.rangeQuery("month").gt(startDate.getMillis()).lt(stopDate.getMillis()));
        TermsBuilder termsb = AggregationBuilders.terms(group_by).field(group_by).size(1000);
        termsb.subAggregation(AggregationBuilders.sum("count").field("count"));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(termsb)
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get(group_by);
        for (Terms.Bucket bucket : agg.getBuckets()) {
            // get count
            Sum countSum = bucket.getAggregations().get("count");
            Double count = Math.round(countSum.getValue() * 100.0) / 100.0;
            ret.put(TextUtil.formatName(bucket.getKeyAsString()), count);
        }
        if (ret != null && ret.keySet().size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public TreeMap getTrendReport1Group(String groupBy, String intervalGroup, DateTime startDate, DateTime stopDate, Boolean isCSGOnly, String employeeType) {
        TreeMap ret = new TreeMap();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (!employeeType.equalsIgnoreCase("all")) {
            if (employeeType.equalsIgnoreCase("fte"))
                query.must(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            else {
                query.mustNot(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            }
        }
        if (isCSGOnly) {
            BoolQueryBuilder query1 = QueryBuilders.boolQuery();
            query1.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query1.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query1.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query1);
        }
        query.must(QueryBuilders.rangeQuery("month").gt(startDate.getMillis()).lt(stopDate.getMillis()));
        DateHistogramBuilder dhb = AggregationBuilders.dateHistogram("time").field("month").interval(DateHistogramInterval.MONTH);
        TermsBuilder termsb = AggregationBuilders.terms("terms").field(groupBy).size(1000);
        dhb.subAggregation(AggregationBuilders.sum("count").field("count"));
        termsb.subAggregation(dhb);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(termsb)
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("terms");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            Histogram dateTerm = bucket.getAggregations().get("time");
            String name = TextUtil.formatName(bucket.getKeyAsString());
            for (Histogram.Bucket dateBucket : dateTerm.getBuckets()) {
                DateTime dt = (DateTime) dateBucket.getKey();
                dt = dt.plusHours(12).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                String ds = dt.toString(dfmt);
                Sum countSum = dateBucket.getAggregations().get("count");
                Double count = Math.round(countSum.getValue() * 100.0) / 100.0;
                TreeMap tm = new TreeMap();
                if (ret.containsKey(name)) {
                    tm = (TreeMap) ret.get(name);
                }
                tm.put(ds, count);
                ret.put(name, tm);
            }
        }
        if (ret != null && ret.keySet().size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public TreeMap getTrendReport2Group(String groupBy1, String groupBy2, String intervalGroup, DateTime startDate, DateTime stopDate, Boolean isCSGOnly, String employeeType) {
        TreeMap ret = new TreeMap();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (!employeeType.equalsIgnoreCase("all")) {
            if (employeeType.equalsIgnoreCase("fte"))
                query.must(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            else {
                query.mustNot(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            }
        }
        if (isCSGOnly) {
            BoolQueryBuilder query1 = QueryBuilders.boolQuery();
            query1.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query1.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query1.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query1);
        }
        query.must(QueryBuilders.rangeQuery("month").gte(startDate.getMillis()).lte(stopDate.getMillis()));
        TermsBuilder termsb = AggregationBuilders.terms("term1").field(groupBy1).size(1000);

        TermsBuilder termsb1 = AggregationBuilders.terms("term2").field(groupBy2).size(1000);

        DateHistogramBuilder dhb = AggregationBuilders.dateHistogram("time").field("month").interval(DateHistogramInterval.MONTH);

        dhb.subAggregation(AggregationBuilders.sum("count").field("count"));
        termsb1.subAggregation(dhb);
        termsb.subAggregation(termsb1);

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(termsb)
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("term1");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            Terms project = bucket.getAggregations().get("term2");
            TreeMap termmap = new TreeMap();
            for (Terms.Bucket termBucket : project.getBuckets()) {
                Histogram dateTerm = termBucket.getAggregations().get("time");
                TreeMap timemap = new TreeMap();
                for (Histogram.Bucket dateBucket : dateTerm.getBuckets()) {
                    DateTime dt = (DateTime) dateBucket.getKey();
                    dt = dt.plusHours(12).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                    String ds = dt.toString(dfmt);
                    Sum countSum = dateBucket.getAggregations().get("count");
                    Double count = Math.round(countSum.getValue() * 100.0) / 100.0;
                    timemap.put(ds, count);
                }
                termmap.put(TextUtil.formatName(termBucket.getKeyAsString()), timemap);
            }
            ret.put(TextUtil.formatName(bucket.getKeyAsString()), termmap);

        }
        if (ret != null && ret.keySet().size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public TreeMap getTrendReport3Group(String groupBy1, String groupBy2, String groupBy3, String intervalGroup, DateTime startDate, DateTime stopDate, Boolean isCSGOnly, String employeeType) {
        TreeMap ret = new TreeMap();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (!employeeType.equalsIgnoreCase("all")) {
            if (employeeType.equalsIgnoreCase("fte"))
                query.must(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            else {
                query.mustNot(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            }
        }
        if (isCSGOnly) {
            BoolQueryBuilder query1 = QueryBuilders.boolQuery();
            query1.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query1.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query1.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query1);
        }
        query.must(QueryBuilders.rangeQuery("month").gte(startDate.getMillis()).lte(stopDate.getMillis()));

        TermsBuilder termsb = AggregationBuilders.terms("term1").field(groupBy1).size(1000);
        TermsBuilder termsb1 = AggregationBuilders.terms("term2").field(groupBy2).size(1000);
        TermsBuilder termsb2 = AggregationBuilders.terms("term3").field(groupBy3).size(1000);

        DateHistogramBuilder dhb = AggregationBuilders.dateHistogram("time").field("month").interval(DateHistogramInterval.MONTH);

        dhb.subAggregation(AggregationBuilders.sum("count").field("count"));
        termsb2.subAggregation(dhb);
        termsb1.subAggregation(termsb2);
        termsb.subAggregation(termsb1);

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(termsb)
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("term1");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            Terms project = bucket.getAggregations().get("term2");
            TreeMap termmap = new TreeMap();
            for (Terms.Bucket projectBucket : project.getBuckets()) {
                TreeMap projectmap = new TreeMap();
                Terms skill = projectBucket.getAggregations().get("term3");
                for (Terms.Bucket skillBucket : skill.getBuckets()) {
                    Histogram dateTerm = skillBucket.getAggregations().get("time");
                    TreeMap timemap = new TreeMap();
                    for (Histogram.Bucket dateBucket : dateTerm.getBuckets()) {
                        DateTime dt = (DateTime) dateBucket.getKey();
                        dt = dt.plusHours(12).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                        String ds = dt.toString(dfmt);
                        Sum countSum = dateBucket.getAggregations().get("count");
                        Double count = Math.round(countSum.getValue() * 100.0) / 100.0;
                        timemap.put(ds, count);
                    }
                    projectmap.put(TextUtil.formatName(skillBucket.getKeyAsString()), timemap);
                }
                termmap.put(TextUtil.formatName(projectBucket.getKeyAsString()), projectmap);
            }
            ret.put(TextUtil.formatName(bucket.getKeyAsString()), termmap);

        }
        if (ret != null && ret.keySet().size() > 0) {
            return ret;
        }
        return null;
    }

    /*******************************************************************************************
     * Resource planning report group by project selected
     *******************************************************************************************/

    @Override
    public TreeMap getProjectReportGroup(List<String> projects, String intervalGroup, DateTime startDate, DateTime stopDate, Boolean isCSGOnly, String employeeType) {
        TreeMap ret = new TreeMap();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        BoolQueryBuilder query1 = QueryBuilders.boolQuery();
        boolean queryvalid = false;
        if (projects != null && !projects.isEmpty()) {
            for (String project : projects) {
                query1.should(QueryBuilders.termQuery("project_name", project.trim().replaceAll("\\+", "plus").toLowerCase()));
            }
            query.must(query1);
        }
        if (!employeeType.equalsIgnoreCase("all")) {
            if (employeeType.equalsIgnoreCase("fte"))
                query.must(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            else {
                query.mustNot(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            }
        }
        if (isCSGOnly) {
            BoolQueryBuilder query2 = QueryBuilders.boolQuery();
            query2.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query2.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query2.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query2);
        }
        query.must(QueryBuilders.rangeQuery("month").gte(startDate.getMillis()).lte(stopDate.getMillis()));
        DateHistogramBuilder dhb = AggregationBuilders.dateHistogram("time").field("month").interval(DateHistogramInterval.MONTH);
        TermsBuilder termsb = AggregationBuilders.terms("terms").field("project_name").size(1000);
        dhb.subAggregation(AggregationBuilders.sum("count").field("count"));
        termsb.subAggregation(dhb);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(termsb)
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("terms");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            Histogram dateTerm = bucket.getAggregations().get("time");

            String name = TextUtil.formatName(bucket.getKeyAsString());
            for (Histogram.Bucket dateBucket : dateTerm.getBuckets()) {
                DateTime dt = (DateTime) dateBucket.getKey();
                dt = dt.plusHours(12).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                String ds = dt.toString(dfmt);
                Sum countSum = dateBucket.getAggregations().get("count");
                Double count = Math.round(countSum.getValue() * 100.00) / 100.00;
                TreeMap tm = new TreeMap();
                if (ret.containsKey(name)) {
                    tm = (TreeMap) ret.get(name);
                }
                tm.put(ds, count);
                ret.put(name, tm);
            }
        }
        if (ret != null && ret.keySet().size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public TreeMap getProjectSkillReportGroup(List<String> projects, String intervalGroup, DateTime startDate, DateTime stopDate, Boolean isCSGOnly, String employeeType) {
        TreeMap ret = new TreeMap();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        BoolQueryBuilder query1 = QueryBuilders.boolQuery();
        boolean queryvalid = false;
        if (projects != null && !projects.isEmpty()) {
            for (String project : projects) {
                query1.should(QueryBuilders.termQuery("project_name", project.trim().replaceAll("\\+", "plus").toLowerCase()));
            }
            query.must(query1);
        }
        if (!employeeType.equalsIgnoreCase("all")) {
            if (employeeType.equalsIgnoreCase("fte"))
                query.must(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            else {
                query.mustNot(QueryBuilders.termQuery("employee_type", "EMPLOYEE (E)"));
            }
        }
        if (isCSGOnly) {
            BoolQueryBuilder query2 = QueryBuilders.boolQuery();
            query2.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query2.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query2.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query2);
        }
        query.must(QueryBuilders.rangeQuery("month").gte(startDate.getMillis()).lte(stopDate.getMillis()));
        DateHistogramBuilder dhb = AggregationBuilders.dateHistogram("time").field("month").interval(DateHistogramInterval.MONTH);
        TermsBuilder termsb1 = AggregationBuilders.terms("term1").field("project_name").size(1000);
        TermsBuilder termsb2 = AggregationBuilders.terms("term2").field("skill_name").size(1000);

        dhb.subAggregation(AggregationBuilders.sum("count").field("count"));
        termsb2.subAggregation(dhb);
        termsb1.subAggregation(termsb2);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(termsb1)
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("term1");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            Terms project = bucket.getAggregations().get("term2");
            TreeMap termmap = new TreeMap();
            for (Terms.Bucket termBucket : project.getBuckets()) {
                Histogram dateTerm = termBucket.getAggregations().get("time");
                TreeMap timemap = new TreeMap();
                for (Histogram.Bucket dateBucket : dateTerm.getBuckets()) {
                    DateTime dt = (DateTime) dateBucket.getKey();
                    dt = dt.plusHours(12).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
                    String ds = dt.toString(dfmt);
                    Sum countSum = dateBucket.getAggregations().get("count");
                    Double count = Math.round(countSum.getValue() * 100.00) / 100.00;
                    timemap.put(ds, count);
                }
                termmap.put(TextUtil.formatName(termBucket.getKeyAsString()), timemap);
            }
            ret.put(TextUtil.formatName(bucket.getKeyAsString()), termmap);
        }
        if (ret != null && ret.keySet().size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public List<Map> getActualEmployeeData(DateTime startdt, DateTime stopdt, Boolean isCSGOnly) {
        List<Map> ret = new ArrayList<Map>();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (isCSGOnly) {
            BoolQueryBuilder query2 = QueryBuilders.boolQuery();
            query2.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query2.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query2.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query2);
        }
        query.must(QueryBuilders.rangeQuery("month").gte(startdt.getMillis()).lte(stopdt.getMillis()));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(query).build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        SearchHit[] results = hits.getHits().hits();
        for (SearchHit hit : results) {
            ret.add(hit.sourceAsMap());
        }
        return ret;
    }

    @Override
    public Set<String> getDistinctValue(String colName, DateTime startDate, DateTime stopDate, Boolean isCSGOnly) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (isCSGOnly) {
            BoolQueryBuilder query2 = QueryBuilders.boolQuery();
            query2.should(QueryBuilders.termQuery("charged_from", "xgs"));
            query2.should(QueryBuilders.termQuery("charged_from", "dnx"));
            query2.should(QueryBuilders.termQuery("charged_from", "nwsoft"));
            query.must(query2);
        }
        query.must(QueryBuilders.rangeQuery("month").gte(startDate.getMillis()).lte(stopDate.getMillis()));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(AggregationBuilders.terms("col").field(colName).size(1000))
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
        if (ret != null && ret.size() > 0)
            return ret;
        return null;
    }
}
