package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.IndicatorDateSearch;
import com.broadcom.wbi.repository.elasticSearch.IndicatorDateSearchRepository;
import com.broadcom.wbi.service.elasticSearch.IndicatorDateSearchService;
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
public class IndicatorDateSearchServiceImpl implements IndicatorDateSearchService {

    @Autowired
    private IndicatorDateSearchRepository repo;

    @Autowired
    private ElasticsearchTemplate template;

    @Override
    public IndicatorDateSearch saveOrUpdate(IndicatorDateSearch idate) {
        return repo.save(idate);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public void saveBulk(List<IndicatorDateSearch> idates) {
        repo.save(idates);
    }

    @Override
    public void delete(String id) {
        repo.delete(id);
    }

    @Override
    public void emptyData() {
        repo.deleteAll();
    }

    @Override
    public void deleteIndex() {
        template.deleteIndex(IndicatorDateSearch.class);
    }

    @Override
    public IndicatorDateSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<IndicatorDateSearch> findAll() {
        return repo.findAll().iterator();
    }


    @Override
    public List<IndicatorDateSearch> findByIndicatorTask(int tid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("task_id", tid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC)).build();
        List<IndicatorDateSearch> idates = repo.search(searchQuery).getContent();
        return idates;
    }

    @Override
    public List<IndicatorDateSearch> findByIndicatorTask(int tid, DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("task_id", tid))
                        .must(QueryBuilders.rangeQuery("last_updated_date").lte(dt.hourOfDay().withMaximumValue().getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC)).build();
        List<IndicatorDateSearch> idates = repo.search(searchQuery).getContent();
        return idates;
    }

    @Override
    public Set<String> getDistinctValue(int id, String type, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery(type, id));

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
    public IndicatorDateSearch findByIndicatorTask(int tid, String dateType) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("task_id", tid))
                        .must(QueryBuilders.termQuery("date_name", dateType))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC)).build();
        List<IndicatorDateSearch> idates = repo.search(searchQuery).getContent();
        if (idates != null && !idates.isEmpty())
            return idates.get(0);
        return null;
    }

    @Override
    public List<IndicatorDateSearch> findByIndicatorGroup(int gid, String taskName) {
        Set<String> dateName = new HashSet<String>();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("group_id", gid))
                        .must(QueryBuilders.termQuery("task_name", taskName.toLowerCase().trim()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("date_name").order(SortOrder.DESC))
                .build();
        List<IndicatorDateSearch> idates = repo.search(searchQuery).getContent();
        List<IndicatorDateSearch> ret = new ArrayList<IndicatorDateSearch>();
        if (idates != null && !idates.isEmpty()) {
            for (IndicatorDateSearch idate : idates) {
                if (!dateName.contains(idate.getDate_name())) {
                    ret.add(idate);
                    dateName.add(idate.getDate_name());
                }
            }
        }
        return ret;
    }

    @Override
    public List<IndicatorDateSearch> findAllByIndicatorGroup(int gid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("group_id", gid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("date_name").order(SortOrder.DESC))
                .build();
        List<IndicatorDateSearch> idates = repo.search(searchQuery).getContent();
        if (idates != null && !idates.isEmpty()) {
            return idates;
        }
        return null;
    }

    @Override
    public List<IndicatorDateSearch> findByIndicatorGroup(int gid) {
        Set<String> dateName = new HashSet<String>();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("group_id", gid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("date_name").order(SortOrder.DESC))
                .build();
        List<IndicatorDateSearch> idates = repo.search(searchQuery).getContent();
        List<IndicatorDateSearch> ret = new ArrayList<IndicatorDateSearch>();
        if (idates != null && !idates.isEmpty()) {
            for (IndicatorDateSearch idate : idates) {
                if (!dateName.contains(idate.getDate_name())) {
                    ret.add(idate);
                    dateName.add(idate.getDate_name());
                }
            }
//			return idates;
        }
        return ret;
    }

    @Override
    public List<IndicatorDateSearch> findByDateTime(DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("last_updated_date").gte(dt.withTimeAtStartOfDay().getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC)).build();
        List<IndicatorDateSearch> idates = repo.search(searchQuery).getContent();
        return idates;
    }

}
