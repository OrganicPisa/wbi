package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.HeadlineSearch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface HeadlineSearchService extends CRUDService<HeadlineSearch> {

    HeadlineSearch findLastNonActiveStage(int rid);

    List<HeadlineSearch> findByRevision(int rid);

    HeadlineSearch findByRevision(int rid, DateTime dt);

    Set<String> getDistinctValue(int rid, String colName);

}
