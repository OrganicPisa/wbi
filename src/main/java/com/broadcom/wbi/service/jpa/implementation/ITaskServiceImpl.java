package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.ITask;
import com.broadcom.wbi.repository.mysql.ITaskRepository;
import com.broadcom.wbi.service.jpa.ITaskService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ITaskServiceImpl implements ITaskService {
    @Resource
    private ITaskRepository repo;

    @Override
    @Transactional
    public ITask saveOrUpdate(ITask itask) {
        return repo.save(itask);
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
    public Page<ITask> findAll(Integer init_num, Integer num) {
        return null;
    }


    @Override
    @Transactional
    public List<ITask> saveBulk(List<ITask> skills) {
        return repo.save(skills);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ITask> listAll() {

        return repo.findAll();

    }

    @Override
    @Transactional(readOnly = true)
    public ITask findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ITask> findByGroup(IGroup igroup, DateTime dt) {
        if (dt == null)
            return repo.findByIGroupOrderByCreatedDateDesc(igroup);
        return repo.findByIGroupAndCreatedDateAfterOrderByCreatedDateDesc(igroup, dt.toDate());
    }

    @Override
    @Transactional(readOnly = true)
    public ITask findByName(IGroup igroup, String name) {
        List<ITask> itasks = repo.findByIGroupAndName(igroup, name.toLowerCase());
        if (itasks != null && !itasks.isEmpty())
            return itasks.get(0);
        return null;
    }

}
