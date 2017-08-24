package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.TemplateSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TemplateSearchRepository extends ElasticsearchRepository<TemplateSearch, String> {

}
