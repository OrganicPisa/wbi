package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface RevisionInformationRepository extends JpaRepository<RevisionInformation, Integer> {

    List<RevisionInformation> findByRevisionOrderByOrderNumAsc(Revision rev);

    List<RevisionInformation> findByRevisionAndPhaseOrderByOrderNumAsc(Revision rev, String phase);

    List<RevisionInformation> findDistinctByRevisionAndOnDashboardIsTrueOrderByOrderNumAsc(Revision rev, boolean onDashboard);

    List<RevisionInformation> findDistinctByRevisionAndPhaseAndNameOrderByOrderNumAsc(Revision rev, String phase, String name);

    Long countAllByCreatedDateBefore(Date dt);
}
