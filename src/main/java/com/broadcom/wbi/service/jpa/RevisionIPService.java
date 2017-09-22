package com.broadcom.wbi.service.jpa;


import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionIP;

import java.util.List;

public interface RevisionIPService extends CRUDService<RevisionIP> {

    List<RevisionIP> findByRevision(Revision revision);

    List<RevisionIP> findByRevisionIP(Revision iprevision);

    RevisionIP findByRevision(Revision rev, Revision revip);

    List<RevisionIP> findByEitherRevisionOrRevisionIP(Revision revision);


}
