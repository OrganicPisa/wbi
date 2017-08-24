package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.TemplateSearch;

import java.util.List;
import java.util.Set;

public interface TemplateSearchService extends CRUDService<TemplateSearch> {

    List<TemplateSearch> findByTypeCategory(String type, String category, String group);

    Set<String> getDistinctValue(String type, String category, String group, String colName);

}