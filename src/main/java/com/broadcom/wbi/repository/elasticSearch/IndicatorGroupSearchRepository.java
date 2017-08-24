package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.IndicatorGroupSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IndicatorGroupSearchRepository extends ElasticsearchRepository<IndicatorGroupSearch, String> {

}
