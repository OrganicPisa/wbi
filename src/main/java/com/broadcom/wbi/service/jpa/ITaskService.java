package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.ITask;
import org.joda.time.DateTime;

import java.util.List;

public interface ITaskService extends CRUDService<ITask> {

    List<ITask> findByGroup(IGroup igroup, DateTime dt);

    ITask findByName(IGroup igroup, String name);

}
