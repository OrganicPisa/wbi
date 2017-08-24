package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.repository.mysql.IGroupRepository;
import com.broadcom.wbi.service.jpa.IGroupService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class IGroupServiceImpl implements IGroupService {
    @Resource
    private IGroupRepository repo;

    @Override
    @Transactional
    public IGroup saveOrUpdate(IGroup igroup) {
        return repo.save(igroup);
    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
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
    public Page<IGroup> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<IGroup> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }


    @Override
    @Transactional
    public List<IGroup> saveBulk(List<IGroup> igl) {
        return repo.save(igl);
    }

    @Override
    @Transactional(readOnly = true)
    public IGroup findById(Integer id) {

        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IGroup> listAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IGroup> findByRevision(Revision rev, DateTime dt) {
        if (dt == null)
            return repo.findByRevisionOrderByCreatedDateDesc(rev);
        return repo.findByRevisionAndCreatedDateAfterOrderByCreatedDateDesc(rev, dt.toDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IGroup> findByRevision(Revision rev, DateTime dt1, DateTime dt2) {
        return repo.findByRevisionAndCreatedDateBetweenOrderByCreatedDateDesc(rev, dt1.toDate(), dt2.toDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IGroup> findByRevision(Revision rev) {
        return repo.findByRevisionOrderByCreatedDateDesc(rev);
    }

    @Override
    @Transactional(readOnly = true)
    public IGroup findByName(Revision rev, String name) {
        List<IGroup> igroups = repo.findFirstByRevisionAndName(rev, name);
        if (igroups != null && !igroups.isEmpty())
            return igroups.get(0);
        return null;
    }
}
