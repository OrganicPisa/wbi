package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.ITask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface ITaskRepository extends JpaRepository<ITask, Integer> {

    List<ITask> findByIGroupOrderByCreatedDateDesc(IGroup igroup);

    List<ITask> findByIGroupAndCreatedDateAfterOrderByCreatedDateDesc(IGroup igroup, Date dt);

    List<ITask> findByIGroupAndCreatedDateBetweenOrderByCreatedDateDesc(IGroup igroup, Date dt1, Date dt2);

    List<ITask> findByCreatedDateAfter(Date dt);

    @Query("SELECT igh from ITask igh where igh.iGroup =?1 and LOWER(igh.name) = LOWER(?2) order by  igh.name desc ")
    List<ITask> findByIGroupAndName(IGroup igroup, String name);

    Long countAllByCreatedDateBefore(Date dt);
}
