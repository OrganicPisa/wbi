package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.IDate;
import com.broadcom.wbi.model.mysql.IDateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;


public interface IDateHistoryRepository extends JpaRepository<IDateHistory, Integer> {

    List<IDateHistory> findByIDateAndCreatedDateAfterOrderByCreatedDateDesc(IDate date, Date dt);

    List<IDateHistory> findByIDateAndCreatedDateBetweenOrderByCreatedDateDesc(IDate date, Date dt1, Date dt2);

    IDateHistory findFirstByIDateOrderByCreatedByDesc(IDate date);

    List<IDateHistory> findByIDateOrderByCreatedDateDesc(IDate date);

    List<IDateHistory> findByCreatedDateAfterOrderByCreatedDateDesc(Date createdDate);

    Long countAllByCreatedDateBefore(Date dt);
}
