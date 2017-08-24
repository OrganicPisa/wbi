package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.ITask;
import com.broadcom.wbi.model.mysql.ITaskHistory;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public interface ITaskHistoryService extends CRUDService<ITaskHistory> {

    List<ITaskHistory> findByTask(ITask itask, DateTime dt);

    List<ITaskHistory> findByTask(ITask itask, DateTime dt1, DateTime dt2);

    List<Date> findTimestampByTask(ITask itask);

    ITaskHistory findByTask(ITask itask);

    List<ITaskHistory> findByUpdateDate(DateTime dt);

}
