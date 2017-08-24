package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionOutlook;
import com.broadcom.wbi.repository.mysql.RevisionOutlookRepository;
import com.broadcom.wbi.service.jpa.RevisionOutlookService;
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
public class RevisionOutlookServiceImpl implements RevisionOutlookService {
    @Resource
    private RevisionOutlookRepository repo;

    @Override
    public RevisionOutlook saveOrUpdate(RevisionOutlook pm_outlook) {
        return repo.save(pm_outlook);
    }

    @Override
    public List<RevisionOutlook> saveBulk(List<RevisionOutlook> pm_outlooks) {
        return repo.save(pm_outlooks);
    }

    @Override
    public List<RevisionOutlook> listAll() {
        return repo.findAll(new Sort(Sort.Direction.DESC, "created_date"));
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
    public Page<RevisionOutlook> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<RevisionOutlook> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }


    @Override
    public RevisionOutlook findById(Integer id) {
        return repo.findOne(id);

    }

    @Override
    public List<RevisionOutlook> findByRevision(Revision rev, DateTime dt) {
        if (dt == null)
            return repo.findDistinctByRevisionOrderByCreatedDateDesc(rev);
        return repo.findByRevisionAndCreatedDateAfterOrderByCreatedDateDesc(rev, dt.toDate());
    }

    @Override
    public RevisionOutlook findByRevision(Revision rev) {
        return repo.findFirstByRevisionOrderByCreatedDateDesc(rev);
    }
}
