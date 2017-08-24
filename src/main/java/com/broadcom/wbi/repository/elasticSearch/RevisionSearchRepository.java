package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RevisionSearchRepository extends ElasticsearchRepository<RevisionSearch, String> {

}
