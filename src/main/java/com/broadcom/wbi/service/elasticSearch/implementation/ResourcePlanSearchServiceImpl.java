package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.ResourcePlanSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.ResourcePlan;
import com.broadcom.wbi.repository.elasticSearch.ResourcePlanSearchRepository;
import com.broadcom.wbi.service.elasticSearch.ResourcePlanSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ResourcePlanSearchServiceImpl implements ResourcePlanSearchService {

    private final ElasticsearchTemplate template;
    @Resource
    private ResourcePlanSearchRepository repo;

    @Autowired
    public ResourcePlanSearchServiceImpl(ElasticsearchTemplate template) {
        this.template = template;
    }


    @Override
    public void saveBulk(List<ResourcePlanSearch> resources) {
        repo.save(resources);
    }

    @Override
    public void emptyData() {
        repo.deleteAll();
    }

    @Override
    public void deleteIndex() {
        template.deleteIndex(ResourcePlan.class);
    }

    @Override
    public List<ResourcePlanSearch> findByDateTime(DateTime dt) {
        return null;
    }


    @Override
    public Iterator<ResourcePlanSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public ResourcePlanSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public ResourcePlanSearch saveOrUpdate(ResourcePlanSearch domainObject) {
        return repo.save(domainObject);
    }

    @Override
    public void delete(String id) {
        repo.delete(id);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public HashMap<String, TreeMap<String, Double>> groupByMonthSkill(RevisionSearch rs, String ptype) {
        HashMap<String, TreeMap<String, Double>> ret = new HashMap<>();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("program", rs.getProgram_id()));
        query.must(QueryBuilders.wildcardQuery("plan_type", ptype.toLowerCase()));
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(query)
                .addAggregation(AggregationBuilders.terms("skill").field("skill").size(100)
                        .subAggregation(AggregationBuilders.terms("month").field("month").size(100)
                                .subAggregation(AggregationBuilders.sum("count").field("count"))))
                .build();

//		SearchResponse hits = template.query(searchQuery, response -> response);

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("skill");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            String skill = bucket.getKeyAsString().toUpperCase();
            TreeMap<String, Double> hm = new TreeMap<>();
            if (ret.containsKey(skill)) {
                hm = ret.get(skill);
            }
            Terms skillTerms = bucket.getAggregations().get("month");
            for (Terms.Bucket skillBucket : skillTerms.getBuckets()) {
                long mili = (Long) skillBucket.getKeyAsNumber();
                DateTime dt = new DateTime(mili).plusHours(12);
                String ds = dt.toString(dfmt);
                Sum countSum = skillBucket.getAggregations().get("count");
                Double count = Math.round(countSum.getValue() * 100.0) / 100.0;
                hm.put(ds, count);
            }
            ret.put(skill, hm);
        }
        return ret;
    }

    @Override
    public TreeMap<String, Double> groupByMonth(RevisionSearch rs, String ptype) {
        TreeMap<String, Double> ret = new TreeMap<>();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("program", rs.getProgram_id()));
        query.must(QueryBuilders.wildcardQuery("plan_type", ptype.toLowerCase()));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(AggregationBuilders.terms("month").field("month").size(100)
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
            DateTime dt = new DateTime(mili).plusHours(12);
            String ds = dt.toString(dfmt);
            Sum countSum = bucket.getAggregations().get("count");
            Double count = Math.round(countSum.getValue() * 100.0) / 100.0;

            ret.put(ds, count);
        }
        return ret;

    }

    @Override
    public Set<String> getDistinctValue(RevisionSearch rs, String ptype, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("program", rs.getProgram_id()));
        query.must(QueryBuilders.wildcardQuery("plan_type", ptype.toLowerCase()));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(AggregationBuilders.terms("col").field(colName).size(100))
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("col");
        Set<String> ret = new TreeSet<String>();
        DateTime currentdt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay().plusMonths(1);
        DateTime lastdt = null;
        for (Terms.Bucket bucket : agg.getBuckets()) {
            if (colName.toLowerCase().indexOf("month") != -1) {
                DateTime dt = new DateTime(bucket.getKeyAsNumber()).plusHours(12);
                lastdt = dt;
                ret.add(dt.toString(dfmt));
            } else {
                ret.add(bucket.getKeyAsString());
            }
        }
        if (colName.toLowerCase().indexOf("month") != -1) {
            if (lastdt != null) {
                if (currentdt.getMillis() > lastdt.getMillis()) {
                    while (currentdt.getMillis() > lastdt.getMillis()) {
                        ret.add(lastdt.toString(dfmt));
                        lastdt = lastdt.plusMonths(1);
                    }
                }
            }
        }
        if (ret != null && ret.size() > 0)
            return ret;
        return null;
    }

    @Override
    public Set<String> getDistinctValue(RevisionSearch rs, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("program", rs.getProgram_id()));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query)
                .addAggregation(AggregationBuilders.terms("col").field(colName).size(100))
                .build();

        SearchResponse hits = template.query(searchQuery, new ResultsExtractor<SearchResponse>() {
            @Override
            public SearchResponse extract(SearchResponse response) {
                return response;
            }
        });
        Terms agg = hits.getAggregations().get("col");
        Set<String> ret = new TreeSet<String>();
        DateTime currentdt = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay().plusMonths(1);
        DateTime lastdt = null;
        for (Terms.Bucket bucket : agg.getBuckets()) {
            if (colName.toLowerCase().indexOf("month") != -1) {
                DateTime dt = new DateTime(bucket.getKeyAsNumber()).plusHours(12);
                lastdt = dt;
                ret.add(dt.toString(dfmt));
            } else {
                ret.add(bucket.getKeyAsString());
            }
        }
        if (colName.toLowerCase().indexOf("month") != -1) {
            if (lastdt != null) {
                if (currentdt.getMillis() > lastdt.getMillis()) {
                    while (currentdt.getMillis() > lastdt.getMillis()) {
                        ret.add(lastdt.toString(dfmt));
                        lastdt = lastdt.plusMonths(1);
                    }
                }
            }
        }
        if (ret != null && ret.size() > 0)
            return ret;
        return null;
    }

    @Override
    public void deleteByTime(DateTime dt) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("month").gte(dt.getMillis())));
        template.delete(deleteQuery, ResourcePlanSearch.class);
    }

    @Override
    public void deleteByPlanType(RevisionSearch rs, String ptype) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("program", rs.getProgram_id()));
        query.must(QueryBuilders.wildcardQuery("plan_type", ptype.toLowerCase()));

        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(query);
        template.delete(deleteQuery, ResourcePlanSearch.class);
    }

}
