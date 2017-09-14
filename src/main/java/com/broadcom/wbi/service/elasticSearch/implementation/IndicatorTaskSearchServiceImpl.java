package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.IndicatorTaskSearch;
import com.broadcom.wbi.repository.elasticSearch.IndicatorTaskSearchRepository;
import com.broadcom.wbi.service.elasticSearch.IndicatorTaskSearchService;
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
public class IndicatorTaskSearchServiceImpl implements IndicatorTaskSearchService {

    private final IndicatorTaskSearchRepository repo;

    private final ElasticsearchTemplate template;

    @Autowired
    public IndicatorTaskSearchServiceImpl(IndicatorTaskSearchRepository repo, ElasticsearchTemplate template) {
        this.repo = repo;
        this.template = template;
    }

    @Override
    public IndicatorTaskSearch saveOrUpdate(IndicatorTaskSearch itask) {
        return repo.save(itask);
    }

    @Override
    public void saveBulk(List<IndicatorTaskSearch> itasks) {
        repo.save(itasks);
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
    public void emptyData() {
        repo.deleteAll();
    }

    @Override
    public void deleteIndex() {
        template.deleteIndex(IndicatorTaskSearch.class);
    }

    @Override
    public IndicatorTaskSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<IndicatorTaskSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<IndicatorTaskSearch> findByIndicatorGroup(int gid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("igroup_id", gid))
                ).build();
        List<IndicatorTaskSearch> itasks = repo.search(searchQuery).getContent();
        return itasks;
    }

    @Override
    public List<IndicatorTaskSearch> findByIndicatorGroup(int gid, DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        Set<String> taskName = new HashSet<String>();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("igroup_id", gid))
                        .must(QueryBuilders.rangeQuery("last_updated_date").lte(dt.hourOfDay().withMaximumValue().getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorTaskSearch> itasks = repo.search(searchQuery).getContent();
        List<IndicatorTaskSearch> ret = new ArrayList<IndicatorTaskSearch>();
        for (IndicatorTaskSearch itask : itasks) {
            if (!taskName.contains(itask.getTask_name())) {
                ret.add(itask);
                taskName.add(itask.getTask_name());
            }
        }
        if (!ret.isEmpty())
            return ret;
        return null;
    }

    @Override
    public Set<String> getDistinctValue(int gid, String type, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery(type, gid));

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
            if (colName.toLowerCase().indexOf("date") != -1) {
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
    public List<IndicatorTaskSearch> findAllByTask(int tid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("task_id", tid))
                ).build();
        List<IndicatorTaskSearch> itasks = repo.search(searchQuery).getContent();
        return itasks;
    }

    @Override
    public IndicatorTaskSearch findByIndicatorTask(int tid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("task_id", tid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorTaskSearch> itsl = repo.search(searchQuery).getContent();
        if (!itsl.isEmpty())
            return itsl.get(0);
        return null;
    }

    @Override
    public List<IndicatorTaskSearch> findByRevision(int rid, String taskName) {
        Set<Integer> gl = new HashSet<Integer>();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                        .must(QueryBuilders.termQuery("task_name", taskName.toLowerCase()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorTaskSearch> itasks = repo.search(searchQuery).getContent();
        List<IndicatorTaskSearch> ret = new ArrayList<IndicatorTaskSearch>();
        for (IndicatorTaskSearch itask : itasks) {
            if (!gl.contains(itask.getIgroup_id())) {
                ret.add(itask);
                gl.add(itask.getIgroup_id());
            }
        }
        if (!ret.isEmpty())
            return ret;
        return null;
    }

    @Override
    public IndicatorTaskSearch findByRevisionGroup(Integer rid, String gname, String tname) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                        .must(QueryBuilders.termQuery("igroup_name", gname.toLowerCase()))
                        .must(QueryBuilders.termQuery("task_name", tname.toLowerCase()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorTaskSearch> itsl = repo.search(searchQuery).getContent();
        if (!itsl.isEmpty())
            return itsl.get(0);
        return null;
    }

    @Override
    public List<IndicatorTaskSearch> findByRevision(int rid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                ).build();
        List<IndicatorTaskSearch> itasks = repo.search(searchQuery).getContent();
        return itasks;
    }

    @Override
    public List<IndicatorTaskSearch> findByDateTime(DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("last_updated_date").gte(dt.withTimeAtStartOfDay().getMillis()))
                ).build();
        List<IndicatorTaskSearch> itasks = repo.search(searchQuery).getContent();
        return itasks;
    }

    @Override
    public List<IndicatorTaskSearch> findByRevisionIndicatorGroup(int rid, int gid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("igroup_id", gid))
                        .must(QueryBuilders.termQuery("revision_id", rid))
                ).build();
        List<IndicatorTaskSearch> itasks = repo.search(searchQuery).getContent();
        return itasks;
    }

}