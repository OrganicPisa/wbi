package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.IndicatorGroupSearch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface IndicatorGroupSearchService extends CRUDService<IndicatorGroupSearch> {

    List<IndicatorGroupSearch> findByRevision(int rid);

    IndicatorGroupSearch findByGroupId(int gid, DateTime dt);

    IndicatorGroupSearch findByRevision(int rid, String gname, DateTime dt);

    IndicatorGroupSearch findByGroupId(int gid);

    List<IndicatorGroupSearch> findByRevision(int rid, DateTime dt);

    Set<String> getDistinctValue(int id, String type, String colName);

    List<IndicatorGroupSearch> findAllByGroupId(int gid);


}
