package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Segment;

import java.util.List;

public interface SegmentService extends CRUDService<Segment> {

    Segment findByName(String name);

    List<Segment> findAllActive();


}
