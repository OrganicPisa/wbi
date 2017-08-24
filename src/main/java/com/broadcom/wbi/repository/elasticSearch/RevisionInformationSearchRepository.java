package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.RevisionInformationSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RevisionInformationSearchRepository extends ElasticsearchRepository<RevisionInformationSearch, String> {

}
