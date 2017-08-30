package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.ResourceActualSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ResourceActualSearchRepository extends ElasticsearchRepository<ResourceActualSearch, String> {

}
