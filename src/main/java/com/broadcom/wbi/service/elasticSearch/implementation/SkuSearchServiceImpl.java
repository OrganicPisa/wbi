package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.SkuSearch;
import com.broadcom.wbi.repository.elasticSearch.SkuSearchRepository;
import com.broadcom.wbi.service.elasticSearch.SkuSearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private SkuSearchRepository repo;
    @Autowired
    private ElasticsearchTemplate template;

    @Override
    public SkuSearch saveOrUpdate(SkuSearch sku) {
        return repo.save(sku);
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
        template.deleteIndex(SkuSearch.class);
    }

    @Override
    public List<SkuSearch> findByDateTime(DateTime dt) {
        return null;
    }

    @Override
    public SkuSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<SkuSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<SkuSearch> findBySkuNum(String term) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 10))
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.wildcardQuery("skuNum", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("otherName", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("aka", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("programDisplayName", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("programName", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("baseNum", "*" + term + "*"))
                ).build();
        List<SkuSearch> skus = repo.search(searchQuery).getContent();
        return skus;
    }

    @Override
    public Set<Integer> searchBySkuNum(String term) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 10))
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.wildcardQuery("skuNum", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("otherName", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("aka", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("programDisplayName", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("programName", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("baseNum", "*" + term + "*"))
                ).build();
        List<SkuSearch> skus = repo.search(searchQuery).getContent();
        if (skus != null && !skus.isEmpty()) {
            Set<Integer> rets = new HashSet<Integer>();
            for (SkuSearch sku : skus) {
                rets.add(sku.getProgram());
            }
            return rets;
        }
        return null;
    }

    @Override
    public List<SkuSearch> findByPrefixSkuNum(String term) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 10))
                .withQuery(
                        QueryBuilders.boolQuery()
                                .should(QueryBuilders.termQuery("skuNum", term + "*"))
                                .should(QueryBuilders.wildcardQuery("otherName", term + "*"))
                                .should(QueryBuilders.wildcardQuery("aka", term + "*"))
                                .should(QueryBuilders.wildcardQuery("programDisplayName", term + "*"))
                                .should(QueryBuilders.wildcardQuery("programName", term + "*"))
                                .should(QueryBuilders.termQuery("baseNum", term + "*")
                                )
                ).build();
        List<SkuSearch> skus = repo.search(searchQuery).getContent();
        return skus;
    }

    @Override
    public void saveBulk(List<SkuSearch> skus) {
        repo.save(skus);
    }

    @Override
    public List<SkuSearch> findByProgram(int pid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("program", pid))
                ).build();
        List<SkuSearch> skus = repo.search(searchQuery).getContent();
        return skus;
    }

}
