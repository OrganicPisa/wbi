package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.HeadlineSearch;
import com.broadcom.wbi.repository.elasticSearch.HeadlineSearchRepository;
import com.broadcom.wbi.service.elasticSearch.HeadlineSearchService;
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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class HeadlineSearchServiceImpl implements HeadlineSearchService {
    @Autowired
    private HeadlineSearchRepository repo;

    @Autowired
    private ElasticsearchTemplate template;

    @Override
    public HeadlineSearch saveOrUpdate(HeadlineSearch hl) {
        return repo.save(hl);
    }

    @Override
    public void saveBulk(List<HeadlineSearch> hls) {
        repo.save(hls);
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
        template.deleteIndex(HeadlineSearch.class);
    }

    @Override
    public List<HeadlineSearch> findByDateTime(DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("last_updated_date").lte(dt.withTimeAtStartOfDay().getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC)).build();
        List<HeadlineSearch> idates = repo.search(searchQuery).getContent();
        return idates;
    }

    @Override
    public HeadlineSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<HeadlineSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<HeadlineSearch> findByRevision(int rid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<HeadlineSearch> hlsl = repo.search(searchQuery).getContent();
        return hlsl;
    }

    @Override
    public HeadlineSearch findLastNonActiveStage(int rid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                        .mustNot(QueryBuilders.wildcardQuery("stage", "^no%"))
                        .mustNot(QueryBuilders.wildcardQuery("stage", "^inactive%"))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<HeadlineSearch> hlsl = repo.search(searchQuery).getContent();
        if (hlsl != null && !hlsl.isEmpty()) {
            for (HeadlineSearch hls : hlsl) {
                if (hls.getStage().toLowerCase().indexOf("non") == -1 &&
                        hls.getStage().toLowerCase().indexOf("inactive") == -1) {
                    return hls;
                }
            }
        }
        return null;
    }

    @Override
    public HeadlineSearch findByRevision(int rid, DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision_id", rid))
                        .must(QueryBuilders.rangeQuery("last_updated_date").lte(dt.plusDays(1).getMillis()))
                ).withSort(SortBuilders.fieldSort("last_updated_date").order(SortOrder.DESC))
                .build();
        List<HeadlineSearch> hlsl = repo.search(searchQuery).getContent();
        if (hlsl != null && !hlsl.isEmpty())
            return hlsl.get(0);
        return null;
    }

    @Override
    public Set<String> getDistinctValue(int rid, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termQuery("revision_id", rid));

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


}
