package com.broadcom.wbi.service.jpa;


import com.broadcom.wbi.model.mysql.Link;
import com.broadcom.wbi.model.mysql.Revision;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface LinkService extends CRUDService<Link> {

    List<Link> findByRevision(Revision rev);

    List<Link> findByType(Revision rev, String type);

    @Async
    void cloneFromAnotherRevision(Revision oldRev, Revision rev, Authentication authentication);

}
