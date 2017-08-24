package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface IGroupRepository extends JpaRepository<IGroup, Integer> {

    List<IGroup> findByRevisionAndCreatedDateAfterOrderByCreatedDateDesc(Revision rev, Date dt);

    List<IGroup> findByRevisionAndCreatedDateBetweenOrderByCreatedDateDesc(Revision rev, Date dt1, Date dt2);

    IGroup findFirstByRevisionOrderByCreatedDateDesc(Revision rev);

    List<IGroup> findByRevisionOrderByCreatedDateDesc(Revision rev);

    List<IGroup> findByCreatedDateAfterOrderByCreatedDateDesc(Date createdDate);

    @Query("SELECT igh.createdDate from IGroup igh where igh.revision =?1 order by  igh.createdDate desc ")
    List<Date> findTimestampByRevision(Revision rev);

    @Query("SELECT igh from IGroup igh where igh.revision =?1 and LOWER(igh.name) = LOWER(?2) order by  igh.name desc ")
    List<IGroup> findFirstByRevisionAndName(Revision rev, String name);

    Long countAllByCreatedDateBefore(Date dt);
}
