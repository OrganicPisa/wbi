package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RevisionContactRepository extends JpaRepository<RevisionContact, Integer> {

    List<RevisionContact> findByRevisionOrderByNameAsc(Revision rev);

    RevisionContact findFirstByRevisionAndNameOrderByNameAsc(Revision rev, String name);

    Long countAllByCreatedDateBefore(Date dt);
}
