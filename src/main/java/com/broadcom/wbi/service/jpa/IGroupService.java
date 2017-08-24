package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.Revision;
import org.joda.time.DateTime;

import java.util.List;

public interface IGroupService extends CRUDService<IGroup> {

    List<IGroup> findByRevision(Revision rev);

    List<IGroup> findByRevision(Revision rev, DateTime dt);

    List<IGroup> findByRevision(Revision rev, DateTime dt1, DateTime dt2);

    IGroup findByName(Revision rev, String name);

}
