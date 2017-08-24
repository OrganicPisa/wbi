package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.RevisionInformationSearch;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@SuppressWarnings("rawtypes")
public interface RevisionInformationSearchService extends CRUDService<RevisionInformationSearch> {


    List<RevisionInformationSearch> findByRevision(Integer rid);

    LinkedHashMap getRevisionInformationReport(Integer rid, String type);

    List<RevisionInformationSearch> findByRevision(Integer rid, boolean onDashboard);

    List<RevisionInformationSearch> findByRevision(Integer rid, String title);

    List<RevisionInformationSearch> findByName(String name);

    List<RevisionInformationSearch> findByRevisionPhaseName(Integer rid, String phase, String title);

    List<RevisionInformationSearch> findByRevisionPhase(Integer rid, String phase);

    Set<String> getDistinctValue(Integer rid, String colName);

    HashMap<String, String> findLatestByRevision(Integer rid);

}