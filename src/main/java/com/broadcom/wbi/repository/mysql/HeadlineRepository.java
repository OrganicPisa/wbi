package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Headline;
import com.broadcom.wbi.model.mysql.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;


public interface HeadlineRepository extends JpaRepository<Headline, Integer> {
    List<Headline> findByRevisionOrderByCreatedDateDesc(Revision rev);

    Headline findFirstByRevisionOrderByCreatedDateDesc(Revision rev);

    List<Headline> findByCreatedDateAfterOrderByCreatedDateDesc(Date createdDate);

    List<Headline> findByRevisionAndCreatedDateBeforeOrderByCreatedDateDesc(Revision rev, Date createdDate);

    @Query("SELECT hl.createdDate from Headline hl where hl.revision = ?1 order by hl.createdDate desc")
    List<Date> findTimestampByRevision(Revision rev);

    Long countAllByCreatedDateBefore(Date dt);
}
