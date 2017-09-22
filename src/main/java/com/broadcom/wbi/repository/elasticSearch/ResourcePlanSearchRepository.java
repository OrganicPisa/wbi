package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.ResourcePlanSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ResourcePlanSearchRepository extends ElasticsearchRepository<ResourcePlanSearch, String> {

}
