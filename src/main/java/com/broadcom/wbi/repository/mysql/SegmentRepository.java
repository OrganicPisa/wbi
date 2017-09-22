package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Integer> {

    List<Segment> findDistinctByIsSegmentIncludeInReportIsTrue();

    Segment findFirstByNameOrderByNameAsc(String name);

    Long countAllByCreatedDateBefore(Date dt);
}
