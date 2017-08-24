package com.broadcom.wbi.service.jpa;


import com.broadcom.wbi.model.mysql.Link;
import com.broadcom.wbi.model.mysql.Revision;

import java.util.List;

public interface LinkService extends CRUDService<Link> {

    List<Link> findByRevision(Revision rev);

    List<Link> findByType(Revision rev, String type);

}
