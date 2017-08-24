package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.IDate;
import com.broadcom.wbi.model.mysql.ITask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface IDateRepository extends JpaRepository<IDate, Integer> {

    List<IDate> findByITaskAndCreatedDateAfterOrderByCreatedDateDesc(ITask task, Date dt);

    List<IDate> findByITaskAndCreatedDateBetweenOrderByCreatedDateDesc(ITask task, Date dt1, Date dt2);

    IDate findFirstByITaskOrderByCreatedDateDesc(ITask task);

    List<IDate> findByITaskOrderByCreatedDateDesc(ITask task);

    List<IDate> findByCreatedDateAfterOrderByCreatedDateDesc(Date createdDate);

    List<IDate> findByITaskAndTtypeAndEtypeOrderByCreatedDateDesc(ITask itask, String ttype, String etype);

    Long countAllByCreatedDateBefore(Date dt);
}
