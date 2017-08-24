package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.IndicatorDateSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IndicatorDateSearchRepository extends ElasticsearchRepository<IndicatorDateSearch, String> {

}
