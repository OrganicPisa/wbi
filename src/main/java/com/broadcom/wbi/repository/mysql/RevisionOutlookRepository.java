package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionOutlook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RevisionOutlookRepository extends JpaRepository<RevisionOutlook, Integer> {

    List<RevisionOutlook> findDistinctByRevisionOrderByCreatedDateDesc(Revision rev);

    List<RevisionOutlook> findByRevisionAndCreatedDateAfterOrderByCreatedDateDesc(Revision rev, Date dt);

    RevisionOutlook findFirstByRevisionOrderByCreatedDateDesc(Revision rev);

    Long countAllByCreatedDateBefore(Date dt);
}
