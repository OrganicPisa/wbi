package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Headline;
import com.broadcom.wbi.model.mysql.Revision;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public interface HeadlineService extends CRUDService<Headline> {

    List<Date> findTimestampByRevision(Revision rev);

    List<Headline> findByRevision(Revision rev, DateTime dt);

    Headline findByRevision(Revision rev);

    List<Headline> findByUpdateDate(DateTime dt);

}
