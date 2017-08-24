package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Headline;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.repository.mysql.HeadlineRepository;
import com.broadcom.wbi.service.elasticSearch.HeadlineSearchService;
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
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class HeadlineServiceImpl implements HeadlineService {
    @Autowired
    private HeadlineSearchService hlSearchServ;
    @Resource
    private HeadlineRepository repo;

    @Override
    public Headline saveOrUpdate(Headline headline) {
        return repo.save(headline);
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
        return repo.findByRevisionAndCreatedDateAfterOrderByCreatedDateDesc(rev, dt.toDate());
    }

    @Override
    public List<Date> findTimestampByRevision(Revision rev) {
        return repo.findTimestampByRevision(rev);
    }


}
