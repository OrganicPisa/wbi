package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.IDate;
import com.broadcom.wbi.model.mysql.ITask;
import com.broadcom.wbi.repository.mysql.IDateRepository;
import com.broadcom.wbi.service.jpa.IDateService;
import com.broadcom.wbi.util.ProjectConstant;
import com.broadcom.wbi.util.ProjectConstant.EnumIndicatorEndingDateType;
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
public class IDateServiceImpl implements IDateService {

    @Resource
    private IDateRepository repo;

    @Override
    @Transactional
    public IDate saveOrUpdate(IDate idate) {
        return repo.save(idate);
    }

    @Override
    @Transactional
    public List<IDate> saveBulk(List<IDate> idates) {
        return repo.save(idates);
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
    public Page<IDate> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<IDate> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public IDate findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IDate> listAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IDate> findByTask(ITask itask, Date dt) {
        if (dt == null)
            return repo.findByITaskOrderByCreatedDateDesc(itask);

        return repo.findByITaskAndCreatedDateAfterOrderByCreatedDateDesc(itask, dt);
    }

    @Override
    public List<IDate> findByTask(ITask itask, Date dt1, Date dt2) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public IDate findByTask(ITask itask) {
        return repo.findFirstByITaskOrderByCreatedDateDesc(itask);
    }

    @Override
    @Transactional(readOnly = true)
    public IDate findByTaskAndType(ITask itask, ProjectConstant.EnumIndicatorTrackingDateType ttype, EnumIndicatorEndingDateType etype) {
        List<IDate> idates = repo.findByITaskAndTtypeAndEtypeOrderByCreatedDateDesc(itask, ttype, etype);
        if (idates != null && !idates.isEmpty())
            return idates.get(0);
        return null;
    }

}
