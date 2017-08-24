package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.RevisionContactSearch;

import java.util.List;

public interface RevisionContactSearchService extends CRUDService<RevisionContactSearch> {

    List<RevisionContactSearch> findByRevision(Integer rid, boolean onDashboard);

    List<RevisionContactSearch> findByRevision(Integer rid);

    List<RevisionContactSearch> findByRevision(Integer rid, String title);

}