package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.SkuSearch;

import java.util.List;
import java.util.Set;

public interface SkuSearchService extends CRUDService<SkuSearch> {

    void emptyData();

    List<SkuSearch> findBySkuNum(String skuNum);

    List<SkuSearch> findByProgram(int pid);

    List<SkuSearch> findByPrefixSkuNum(String term);

    Set<Integer> searchBySkuNum(String term);
}
