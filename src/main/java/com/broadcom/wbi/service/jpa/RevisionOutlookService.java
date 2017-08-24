package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionOutlook;
import org.joda.time.DateTime;

import java.util.List;

public interface RevisionOutlookService extends CRUDService<RevisionOutlook> {

    List<RevisionOutlook> findByRevision(Revision rev, DateTime dt);

    RevisionOutlook findByRevision(Revision rev);


}
