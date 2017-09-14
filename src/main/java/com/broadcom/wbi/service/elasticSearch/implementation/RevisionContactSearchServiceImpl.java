package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.RevisionContactSearch;
import com.broadcom.wbi.repository.elasticSearch.RevisionContactSearchRepository;
import com.broadcom.wbi.service.elasticSearch.RevisionContactSearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

@Service
public class RevisionContactSearchServiceImpl implements RevisionContactSearchService {

    private final ElasticsearchTemplate template;
    @Resource
    private RevisionContactSearchRepository repo;

    @Autowired
    public RevisionContactSearchServiceImpl(ElasticsearchTemplate template) {
        this.template = template;
    }

    @Override
    public RevisionContactSearch saveOrUpdate(RevisionContactSearch contact) {
        return repo.save(contact);
    }

    @Override
    public void saveBulk(List<RevisionContactSearch> contacts) {
        repo.save(contacts);
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
        template.deleteIndex(RevisionContactSearch.class);
    }

    @Override
    public List<RevisionContactSearch> findByDateTime(DateTime dt) {
        return null;
    }

    @Override
    public RevisionContactSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<RevisionContactSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<RevisionContactSearch> findByRevision(Integer rid, boolean onDashboard) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision", rid))
                        .must(QueryBuilders.termQuery("onDashboard", onDashboard))
                ).build();
        List<RevisionContactSearch> urls = repo.search(searchQuery).getContent();
        return urls;
    }

    @Override
    public List<RevisionContactSearch> findByRevision(Integer rid, String title) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 10))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision", rid))
                        .must(QueryBuilders.wildcardQuery("name", "*" + title.toLowerCase().trim() + "*"))
                ).build();
        List<RevisionContactSearch> urls = repo.search(searchQuery).getContent();
        return urls;
    }

    @Override
    public List<RevisionContactSearch> findByRevision(Integer rid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("revision", rid))
                ).build();
        List<RevisionContactSearch> urls = repo.search(searchQuery).getContent();
        return urls;
    }

}
