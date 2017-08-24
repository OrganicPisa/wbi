package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.IndicatorTaskSearch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface IndicatorTaskSearchService extends CRUDService<IndicatorTaskSearch> {
    List<IndicatorTaskSearch> findByIndicatorGroup(int gid);

    List<IndicatorTaskSearch> findByRevisionIndicatorGroup(int rid, int gid);

    List<IndicatorTaskSearch> findByRevision(int rid, String taskName);

    List<IndicatorTaskSearch> findByIndicatorGroup(int gid, DateTime dt);

    Set<String> getDistinctValue(int gid, String type, String colName);

    List<IndicatorTaskSearch> findAllByTask(int tid);

    IndicatorTaskSearch findByIndicatorTask(int tid);

    IndicatorTaskSearch findByRevisionGroup(Integer rid, String gname, String tname);

    List<IndicatorTaskSearch> findByRevision(int rid);

}
