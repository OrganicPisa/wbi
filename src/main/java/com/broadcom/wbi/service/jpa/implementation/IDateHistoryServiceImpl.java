package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.IDate;
import com.broadcom.wbi.model.mysql.IDateHistory;
import com.broadcom.wbi.repository.mysql.IDateHistoryRepository;
import com.broadcom.wbi.service.jpa.IDateHistoryService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class IDateHistoryServiceImpl implements IDateHistoryService {

    @Resource
    private IDateHistoryRepository repo;

    @Override
    public IDateHistory saveOrUpdate(IDateHistory idateh) {
        return repo.save(idateh);
    }

    @Override
    public void delete(Integer id) {
        repo.delete(id);
    }

    @Override
    public List<IDateHistory> saveBulk(List<IDateHistory> idhl) {
        return repo.save(idhl);
    }

    @Override
    public List<IDateHistory> listAll() {
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
    public IDateHistory findById(Integer id) {
        return repo.findOne(id);
    }


    @Override
    public Page<IDateHistory> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<IDateHistory> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public IDateHistory findByDate(IDate idate) {
        return repo.findFirstByIDateOrderByCreatedByDesc(idate);
    }

    @Override
    public List<IDateHistory> findByUpdateDate(DateTime dt) {

        return repo.findByCreatedDateAfterOrderByCreatedDateDesc(dt.toDate());
    }

    @Override
    public List<IDateHistory> findByDate(IDate idate, DateTime dt) {
        if (dt == null)
            return repo.findByIDateOrderByCreatedDateDesc(idate);
        return repo.findByIDateAndCreatedDateAfterOrderByCreatedDateDesc(idate, dt.toDate());
    }


    @Override
    public List<IDateHistory> findByDate(IDate idate, DateTime dt1, DateTime dt2) {
        return repo.findByIDateAndCreatedDateBetweenOrderByCreatedDateDesc(idate, dt1.toDate(), dt2.toDate());
    }

}
