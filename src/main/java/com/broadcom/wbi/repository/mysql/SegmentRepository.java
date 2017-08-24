package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface SegmentRepository extends JpaRepository<Segment, Integer> {

    List<Segment> findDistinctByIsSegmentIncludeInReportIsTrue();

    Segment findFirstByNameOrderByNameAsc(String name);

    Long countAllByCreatedDateBefore(Date dt);
}
