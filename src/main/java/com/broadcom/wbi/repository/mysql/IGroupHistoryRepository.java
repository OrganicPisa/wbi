package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.IGroupHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface IGroupHistoryRepository extends JpaRepository<IGroupHistory, Integer> {

    List<IGroupHistory> findByIGroupAndCreatedDateAfterOrderByCreatedDateDesc(IGroup iGroup, Date dt);

    List<IGroupHistory> findByIGroupAndCreatedDateBetweenOrderByCreatedDateDesc(IGroup iGroup, Date dt1, Date dt2);

    IGroupHistory findFirstByIGroupOrderByCreatedDateDesc(IGroup iGroup);

    List<IGroupHistory> findByIGroupOrderByCreatedDateDesc(IGroup iGroup);

    List<IGroupHistory> findByCreatedDateAfterOrderByCreatedDateDesc(Date createdDate);

    List<IGroupHistory> findByIGroupAndStatusOrderByCreatedDateDesc(IGroup igroup, String status);

    @Query("SELECT igh.createdDate from IGroupHistory igh where igh.iGroup =?1 order by  igh.createdDate desc")
    List<Date> findTimestampByIGroup(IGroup igroup);

    Long countAllByCreatedDateBefore(Date dt);
}
