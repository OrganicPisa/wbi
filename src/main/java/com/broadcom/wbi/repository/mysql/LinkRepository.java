package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Link;
import com.broadcom.wbi.model.mysql.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<Link, Integer> {

    List<Link> findDistinctByCategoryOrderByCreatedDateDesc(String cat);

    List<Link> findByRevisionOrderByCreatedDateDesc(Revision rev);

    List<Link> findByRevisionAndTypeOrderByCreatedDateDesc(Revision rev, String type);

    @Query("SELECT COUNT(p) FROM Link p WHERE p.createdDate < ?1")
    Long count(Date dt);

    Long countAllByCreatedDateBefore(Date dt);
}
