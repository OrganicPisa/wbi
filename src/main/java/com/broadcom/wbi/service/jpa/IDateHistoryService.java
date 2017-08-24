package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.IDate;
import com.broadcom.wbi.model.mysql.IDateHistory;
import org.joda.time.DateTime;

import java.util.List;

public interface IDateHistoryService extends CRUDService<IDateHistory> {


    IDateHistory findByDate(IDate d);

    List<IDateHistory> findByDate(IDate d, DateTime dt);

    List<IDateHistory> findByDate(IDate d, DateTime dt1, DateTime dt2);

    List<IDateHistory> findByUpdateDate(DateTime dt);
}
