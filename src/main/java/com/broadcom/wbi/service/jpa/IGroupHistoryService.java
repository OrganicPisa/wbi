package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.IGroupHistory;
import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public interface IGroupHistoryService extends CRUDService<IGroupHistory> {

    List<IGroupHistory> findByGroupAndStatus(IGroup igroup, ProjectConstant.EnumIndicatorStatus status);

    IGroupHistory findByGroup(IGroup igroup);

    List<IGroupHistory> findByGroup(IGroup igroup, DateTime dt);

    List<IGroupHistory> findByGroup(IGroup igroup, DateTime dt1, DateTime dt2);

    List<Date> findTimestampByGroup(IGroup igroup);

    List<IGroupHistory> findByUpdateDate(DateTime dt);

}
