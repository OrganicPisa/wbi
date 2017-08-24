package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionContact;

import java.util.List;

public interface RevisionContactService extends CRUDService<RevisionContact> {

    List<RevisionContact> findByRevision(Revision rev);

    RevisionContact findByRevisionTitle(Revision rev, String title);

}
