package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.IndicatorGroupSearch;
import com.broadcom.wbi.repository.elasticSearch.IndicatorGroupSearchRepository;
import com.broadcom.wbi.service.elasticSearch.IndicatorGroupSearchService;
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
public class IndicatorGroupSearchServiceImpl implements IndicatorGroupSearchService {

    @Autowired
    private IndicatorGroupSearchRepository repo;

    @Autowired
    private ElasticsearchTemplate template;

    @Override
    public IndicatorGroupSearch saveOrUpdate(IndicatorGroupSearch ig) {
        return repo.save(ig);
    }

    @Override
    public void saveBulk(List<IndicatorGroupSearch> igs) {
        repo.save(igs);
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
        template.deleteIndex(IndicatorGroupSearch.class);
    }

    @Override
    public IndicatorGroupSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public IndicatorGroupSearch findByGroupId(int gid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("igroup_id", gid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorGroupSearch> igsl = repo.search(searchQuery).getContent();
        if (igsl != null && !igsl.isEmpty())
            return igsl.get(0);
        return null;
    }

    @Override
    public Iterator<IndicatorGroupSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<IndicatorGroupSearch> findByRevision(int rid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorGroupSearch> igsl = repo.search(searchQuery).getContent();
        Set<String> check = new HashSet<String>();
        List<IndicatorGroupSearch> ret = new ArrayList<IndicatorGroupSearch>();
        for (IndicatorGroupSearch igs : igsl) {
            if (!check.contains(igs.getIgroup_name())) {
                ret.add(igs);
                check.add(igs.getIgroup_name());
            }
        }
        return ret;
    }

    @Override
    public List<IndicatorGroupSearch> findByRevision(int rid, DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                        .must(QueryBuilders.rangeQuery("last_updated_date").lte(dt.plusDays(1).getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorGroupSearch> igsl = repo.search(searchQuery).getContent();
        if (igsl != null && !igsl.isEmpty()) {
            List<IndicatorGroupSearch> ret = new ArrayList<IndicatorGroupSearch>();
            Set<String> gname = new HashSet<String>();
            for (IndicatorGroupSearch igs : igsl) {
                if (!gname.contains(igs.getIgroup_name())) {
                    ret.add(igs);
                    gname.add(igs.getIgroup_name());
                }
            }
            if (!ret.isEmpty())
                return ret;
        }
        return null;
    }

    @Override
    public IndicatorGroupSearch findByGroupId(int gid, DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("igroup_id", gid))
                        .must(QueryBuilders.rangeQuery("last_updated_date").lte(dt.plusDays(1).getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorGroupSearch> igs = repo.search(searchQuery).getContent();
        if (igs != null && !igs.isEmpty())
            return igs.get(0);
        return null;
    }

    @Override
    public IndicatorGroupSearch findByRevision(int rid, String gname, DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                        .must(QueryBuilders.termQuery("igroup_name", gname.toLowerCase()))
                        .must(QueryBuilders.rangeQuery("last_updated_date").lte(dt.plusDays(1).getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorGroupSearch> igs = repo.search(searchQuery).getContent();
        if (igs != null && !igs.isEmpty())
            return igs.get(0);
        return null;
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
    public List<IndicatorGroupSearch> findAllByGroupId(int gid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("igroup_id", gid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorGroupSearch> igs = repo.search(searchQuery).getContent();
        if (igs != null && !igs.isEmpty())
            return igs;
        return null;
    }

    @Override
    public List<IndicatorGroupSearch> findByDateTime(DateTime dt) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("last_updated_date").gte(dt.withTimeAtStartOfDay().getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<IndicatorGroupSearch> igs = repo.search(searchQuery).getContent();
        if (igs != null && !igs.isEmpty())
            return igs;
        return null;
    }
}
