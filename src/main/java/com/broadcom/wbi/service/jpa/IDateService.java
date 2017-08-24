package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.IDate;
import com.broadcom.wbi.model.mysql.ITask;
import com.broadcom.wbi.util.ProjectConstant;

import java.util.Date;
import java.util.List;

public interface IDateService extends CRUDService<IDate> {

    IDate findByTask(ITask itask);

    List<IDate> findByTask(ITask itask, Date dt);

    List<IDate> findByTask(ITask itask, Date dt1, Date dt2);

    IDate findByTaskAndType(ITask itask, ProjectConstant.EnumIndicatorTrackingDateType ttype, ProjectConstant.EnumIndicatorEndingDateType etype);

}
