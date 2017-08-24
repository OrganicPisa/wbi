package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.repository.mysql.SegmentRepository;
import com.broadcom.wbi.service.jpa.SegmentService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class SegmentServiceImpl implements SegmentService {
    @Resource
    private SegmentRepository repo;

    @Override
    public Segment saveOrUpdate(Segment segment) {
        return repo.save(segment);
    }

    @Override
    public List<Segment> saveBulk(List<Segment> segmentList) {
        return repo.save(segmentList);
    }

    @Override
    public void delete(Integer id) {
        repo.delete(id);
    }

    @Override
    public Long count() {
        return repo.count();
    }

    @Override
    public Long count(DateTime dt) {
        return repo.countAllByCreatedDateBefore(dt.toDate());
    }

    @Override
    public Page<Segment> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Segment> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public Segment findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<Segment> listAll() {
        return repo.findAll(new Sort(Sort.Direction.ASC, "order_num"));
    }

    @Override
    public List<Segment> findAllActive() {

        return repo.findDistinctByIsSegmentIncludeInReportIsTrue();
    }

    @Override
    public Segment findByName(String name) {
        return repo.findFirstByNameOrderByNameAsc(name.toUpperCase().trim());
    }
}
