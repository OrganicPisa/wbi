package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Headline;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.repository.mysql.HeadlineRepository;
import com.broadcom.wbi.service.elasticSearch.HeadlineSearchService;
import com.broadcom.wbi.service.event.HeadlineSaveEvent;
import com.broadcom.wbi.service.event.HeadlineSaveEventPublisher;
import com.broadcom.wbi.service.jpa.HeadlineService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class HeadlineServiceImpl implements HeadlineService {
    private final HeadlineSearchService hlSearchServ;
    private final HeadlineSaveEventPublisher headlineSaveEventPublisher;
    @Resource
    private HeadlineRepository repo;

    @Autowired
    public HeadlineServiceImpl(HeadlineSearchService hlSearchServ, HeadlineSaveEventPublisher headlineSaveEventPublisher) {
        this.hlSearchServ = hlSearchServ;
        this.headlineSaveEventPublisher = headlineSaveEventPublisher;
    }

    @Override
    public Headline saveOrUpdate(Headline headline) {
        Headline hl = repo.save(headline);

        HashMap map = new HashMap();
        map.put("action", "save");
        map.put("data", hl);
        headlineSaveEventPublisher.publish(new HeadlineSaveEvent(map));
        return hl;
    }

    @Override
    public List<Headline> saveBulk(List<Headline> headlines) {
        return repo.save(headlines);
    }

    @Override
    public List<Headline> listAll() {
        return repo.findAll();
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
    public Page<Headline> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Headline> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public void delete(Integer id) {
        HashMap map = new HashMap();
        map.put("action", "delete");
        map.put("data", id);
        headlineSaveEventPublisher.publish(new HeadlineSaveEvent(map));
        repo.delete(id);
    }


    @Override
    public Headline findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public Headline findByRevision(Revision rev) {
        return repo.findFirstByRevisionOrderByCreatedDateDesc(rev);
    }

    @Override
    public List<Headline> findByUpdateDate(DateTime dt) {
        return repo.findByCreatedDateAfterOrderByCreatedDateDesc(dt.toDate());
    }

    @Override
    public List<Headline> findByRevision(Revision rev, DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        return repo.findByRevisionAndCreatedDateBeforeOrderByCreatedDateDesc(rev, dt.toDate());
    }

    @Override
    public List<Date> findTimestampByRevision(Revision rev) {
        return repo.findTimestampByRevision(rev);
    }


}
