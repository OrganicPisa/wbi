package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.EmployeeSearch;
import com.broadcom.wbi.repository.elasticSearch.EmployeeSearchRepository;
import com.broadcom.wbi.service.elasticSearch.EmployeeSearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public class EmployeeSearchServiceImpl implements EmployeeSearchService {

    private final EmployeeSearchRepository repo;

    private final ElasticsearchTemplate template;

    @Autowired
    public EmployeeSearchServiceImpl(EmployeeSearchRepository repo, ElasticsearchTemplate template) {
        this.repo = repo;
        this.template = template;
    }

    @Override
    public EmployeeSearch saveOrUpdate(EmployeeSearch empl) {
        return repo.save(empl);
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
        template.deleteIndex(EmployeeSearch.class);
    }

    @Override
    public List<EmployeeSearch> findByDateTime(DateTime dt) {
        return null;
    }

    @Override
    public EmployeeSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<EmployeeSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<EmployeeSearch> findByName(String term) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 20))
                .withQuery(QueryBuilders.boolQuery().should(QueryBuilders.wildcardQuery("first_name", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("last_name", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("full_name", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("email", "*" + term + "*"))
                        .should(QueryBuilders.wildcardQuery("acc_nt", "*" + term + "*")))
                .build();
        List<EmployeeSearch> employees = repo.search(searchQuery).getContent();
        if (employees != null && !employees.isEmpty())
            return employees;
        return employees;
    }

    @Override
    public List<EmployeeSearch> findByFirstNameAndLastName(String fname, String lname) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 20))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.wildcardQuery("full_name", "*" + fname.toLowerCase() + "*"))
                        .must(QueryBuilders.wildcardQuery("full_name", "*" + lname.toLowerCase() + "*")))
                .build();
        List<EmployeeSearch> employees = repo.search(searchQuery).getContent();
        if (employees != null && !employees.isEmpty())
            return employees;
        return employees;
    }

    @Override
    public List<EmployeeSearch> findBySegment(String segment) {
        // TODO Auto-generated method stub
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 10000)).withQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.wildcardQuery("segment", segment.toLowerCase() + "*"))
        ).withSort(SortBuilders.fieldSort("full_name").order(SortOrder.DESC))
                .build();
        List<EmployeeSearch> employees = repo.search(searchQuery).getContent();
        if (employees != null && !employees.isEmpty())
            return employees;
        return null;
    }

    @Override
    public List<EmployeeSearch> findBySegment(String segment, String profit_center) {
        // TODO Auto-generated method stub
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 10000)).withQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.wildcardQuery("segment", segment.toLowerCase() + "*"))
                .must(QueryBuilders.wildcardQuery("profit_center", profit_center.toLowerCase()))
        ).withSort(SortBuilders.fieldSort("full_name").order(SortOrder.DESC))
                .build();
        List<EmployeeSearch> employees = repo.search(searchQuery).getContent();
        if (employees != null && !employees.isEmpty())
            return employees;
        return null;
    }


    @Override
    public List<EmployeeSearch> findByManager(String managerName) {
        // TODO Auto-generated method stub
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 10000)).withQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("supervisor", managerName.toLowerCase() + "*"))
        ).withSort(SortBuilders.fieldSort("full_name").order(SortOrder.DESC))
                .build();
        List<EmployeeSearch> employees = repo.search(searchQuery).getContent();
        if (employees != null && !employees.isEmpty())
            return employees;
        return null;
    }

    @Override
    public void saveBulk(List<EmployeeSearch> esl) {
        repo.save(esl);
    }

}