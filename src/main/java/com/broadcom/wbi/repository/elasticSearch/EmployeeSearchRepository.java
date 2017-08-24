package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.EmployeeSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EmployeeSearchRepository extends ElasticsearchRepository<EmployeeSearch, String> {

}
