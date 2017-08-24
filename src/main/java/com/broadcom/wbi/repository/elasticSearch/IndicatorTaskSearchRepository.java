package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.IndicatorTaskSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IndicatorTaskSearchRepository extends ElasticsearchRepository<IndicatorTaskSearch, String> {

}
