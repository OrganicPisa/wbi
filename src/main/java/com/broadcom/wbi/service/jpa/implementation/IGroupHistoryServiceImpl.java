package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.IGroupHistory;
import com.broadcom.wbi.repository.mysql.IGroupHistoryRepository;
import com.broadcom.wbi.service.jpa.IGroupHistoryService;
import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.DateTime;
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
public class IGroupHistoryServiceImpl implements IGroupHistoryService {

    @Resource
    private IGroupHistoryRepository repo;

    @Override
    public IGroupHistory saveOrUpdate(IGroupHistory igrouph) {
        return repo.save(igrouph);
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
    public Page<IGroupHistory> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<IGroupHistory> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public List<IGroupHistory> saveBulk(List<IGroupHistory> ighl) {
        return repo.save(ighl);
    }

    @Override
    public List<IGroupHistory> listAll() {
        return repo.findAll();
    }

    @Override
    public IGroupHistory findById(Integer id) {

        return repo.findOne(id);
    }

    @Override
    public List<IGroupHistory> findByGroup(IGroup igroup, DateTime dt) {
        if (dt == null)
            repo.findByIGroupOrderByCreatedDateDesc(igroup);
        return repo.findByIGroupAndCreatedDateAfterOrderByCreatedDateDesc(igroup, dt.toDate());
    }

    @Override
    public List<IGroupHistory> findByUpdateDate(DateTime dt) {

        return repo.findByCreatedDateAfterOrderByCreatedDateDesc(dt.toDate());
    }

    @Override
    public IGroupHistory findByGroup(IGroup igroup) {

        return repo.findFirstByIGroupOrderByCreatedDateDesc(igroup);
    }

    @Override
    public List<IGroupHistory> findByGroup(IGroup igroup, DateTime dt1, DateTime dt2) {
        return repo.findByIGroupAndCreatedDateBetweenOrderByCreatedDateDesc(igroup, dt1.toDate(), dt2.toDate());
    }

    @Override
    public List<IGroupHistory> findByGroupAndStatus(IGroup igroup, ProjectConstant.EnumIndicatorStatus status) {
        return repo.findByIGroupAndStatusOrderByCreatedDateDesc(igroup, status.toString().toLowerCase());
    }


    @Override
    public List<Date> findTimestampByGroup(IGroup igroup) {
        return repo.findTimestampByIGroup(igroup);
    }

}
