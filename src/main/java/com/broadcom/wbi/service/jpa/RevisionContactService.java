package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionContact;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface RevisionContactService extends CRUDService<RevisionContact> {

    List<RevisionContact> findByRevision(Revision rev);

    RevisionContact findByRevisionTitle(Revision rev, String title);

    @Async
    void cloneFromAnotherRevision(Revision oldRev, Revision rev, Authentication authentication);

    @Async
    void cloneFromTemplate(Revision rev, Authentication authentication);

}
