package com.broadcom.wbi.repository.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.HeadlineSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface HeadlineSearchRepository extends ElasticsearchRepository<HeadlineSearch, String> {

}
