package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.ITask;
import com.broadcom.wbi.model.mysql.ITaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ITaskHistoryRepository extends JpaRepository<ITaskHistory, Integer> {

    List<ITaskHistory> findByITaskOrderByCreatedDateDesc(ITask itask);

    ITaskHistory findFirstByITaskOrderByCreatedDateDesc(ITask itask);

    List<ITaskHistory> findByITaskAndCreatedDateAfterOrderByCreatedDateDesc(ITask itask, Date dt);

    List<ITaskHistory> findByITaskAndCreatedDateBetweenOrderByCreatedDateDesc(ITask itask, Date dt1, Date dt2);

    List<ITaskHistory> findByCreatedDateAfterOrderByCreatedDateDesc(Date dt);

    @Query("SELECT ith.createdDate from ITaskHistory ith where ith.iTask =?1 order by  ith.createdDate desc")
    List<Date> findTimestampByITask(ITask itask);

    Long countAllByCreatedDateBefore(Date dt);
}
