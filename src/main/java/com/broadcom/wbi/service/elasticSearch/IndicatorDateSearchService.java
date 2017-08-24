package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.IndicatorDateSearch;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface IndicatorDateSearchService extends CRUDService<IndicatorDateSearch> {


    List<IndicatorDateSearch> findByIndicatorTask(int tid);

    List<IndicatorDateSearch> findByIndicatorGroup(int gid, String taskName);

    IndicatorDateSearch findByIndicatorTask(int tid, String dateType);

    List<IndicatorDateSearch> findByIndicatorTask(int tid, DateTime dt);

    Set<String> getDistinctValue(int id, String type, String colName);

    List<IndicatorDateSearch> findByIndicatorGroup(int gid);


    List<IndicatorDateSearch> findAllByIndicatorGroup(int gid);

}
