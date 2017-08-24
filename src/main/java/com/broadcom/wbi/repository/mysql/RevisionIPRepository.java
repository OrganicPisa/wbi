package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionIP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface RevisionIPRepository extends JpaRepository<RevisionIP, Integer> {

    List<RevisionIP> findDistinctByRevisionOrderByCreatedDateDesc(Revision rev);

    RevisionIP findFirstByRevisionAndIprevisionOrderByCreatedDateDesc(Revision rev, Revision ip);

    List<RevisionIP> findDistinctByIprevisionOrderByCreatedDateDesc(Revision ip);

    Long countAllByCreatedDateBefore(Date dt);
}
