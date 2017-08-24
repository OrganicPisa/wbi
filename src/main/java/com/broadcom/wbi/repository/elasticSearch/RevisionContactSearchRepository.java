package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.RevisionContactSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RevisionContactSearchRepository extends ElasticsearchRepository<RevisionContactSearch, String> {

}
