package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.TemplateSearch;
import com.broadcom.wbi.repository.elasticSearch.TemplateSearchRepository;
import com.broadcom.wbi.service.elasticSearch.TemplateSearchService;
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
public class TemplateSearchServiceImpl implements TemplateSearchService {

    @Autowired
    private TemplateSearchRepository repo;

    @Autowired
    private ElasticsearchTemplate template;


    @Override
    public TemplateSearch saveOrUpdate(TemplateSearch field) {
        return repo.save(field);
    }

    @Override
    public void saveBulk(List<TemplateSearch> infos) {
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
        template.deleteIndex(TemplateSearch.class);
    }

    @Override
    public List<TemplateSearch> findByDateTime(DateTime dt) {
        return null;
    }

    @Override
    public TemplateSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<TemplateSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<TemplateSearch> findByTypeCategory(String type, String category, String group) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (type != null && !type.trim().isEmpty())
            query.must(QueryBuilders.wildcardQuery("type", type.toLowerCase()));
        if (category != null && !category.trim().isEmpty())
            query.must(QueryBuilders.wildcardQuery("category", category.toLowerCase()));
        if (group != null && !group.trim().isEmpty())
            query.must(QueryBuilders.wildcardQuery("group", group.toLowerCase()));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(query)
                .withSort(SortBuilders.fieldSort("orderNum").order(SortOrder.ASC)).build();
        List<TemplateSearch> infos = repo.search(searchQuery).getContent();
        return infos;
    }

    @Override
    public Set<String> getDistinctValue(String type, String category, String group, String colName) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (type != null && !type.trim().isEmpty())
            query.must(QueryBuilders.termQuery("type", type.toLowerCase()));
        if (category != null && !category.trim().isEmpty())
            query.must(QueryBuilders.wildcardQuery("category", category.toLowerCase()));
        if (group != null && !group.trim().isEmpty())
            query.must(QueryBuilders.wildcardQuery("group", group.toLowerCase()));
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
