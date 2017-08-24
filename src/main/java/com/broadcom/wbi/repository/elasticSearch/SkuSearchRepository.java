package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.SkuSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SkuSearchRepository extends ElasticsearchRepository<SkuSearch, String> {
}
