package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.ITask;
import com.broadcom.wbi.model.mysql.ITaskHistory;
import com.broadcom.wbi.repository.mysql.ITaskHistoryRepository;
import com.broadcom.wbi.service.event.IndicatorTaskSaveEvent;
import com.broadcom.wbi.service.event.IndicatorTaskSaveEventPublisher;
import com.broadcom.wbi.service.jpa.ITaskHistoryService;
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
public class ITaskHistoryServiceImpl implements ITaskHistoryService {
    @Resource
    private ITaskHistoryRepository repo;
    @Autowired
    private IndicatorTaskSaveEventPublisher indicatorTaskSaveEventPublisher;

    @Override
    public ITaskHistory saveOrUpdate(ITaskHistory itaskh) {
        ITaskHistory iTaskHistory = repo.save(itaskh);
        HashMap map = new HashMap();
        map.put("action", "save");
        map.put("data", iTaskHistory);
        indicatorTaskSaveEventPublisher.publish(new IndicatorTaskSaveEvent(map));
        return iTaskHistory;
    }

    @Override
    public void delete(Integer id) {
        HashMap map = new HashMap();
        map.put("action", "delete");
        map.put("data", id);
        indicatorTaskSaveEventPublisher.publish(new IndicatorTaskSaveEvent(map));
        repo.delete(id);
    }

    @Override
    public List<ITaskHistory> saveBulk(List<ITaskHistory> ithl) {
        return repo.save(ithl);
    }

    @Override
    public ITaskHistory findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<ITaskHistory> findByTask(ITask itask, DateTime dt) {
        if (dt == null)
            return repo.findByITaskOrderByCreatedDateDesc(itask);
        return repo.findByITaskAndCreatedDateAfterOrderByCreatedDateDesc(itask, dt.toDate());
    }

    @Override
    public ITaskHistory findByTask(ITask itask) {
        return repo.findFirstByITaskOrderByCreatedDateDesc(itask);
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
    public Page<ITaskHistory> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<ITaskHistory> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public List<ITaskHistory> findByUpdateDate(DateTime dt) {
        return repo.findByCreatedDateAfterOrderByCreatedDateDesc(dt.toDate());
    }

    @Override
    public List<ITaskHistory> findByTask(ITask itask, DateTime dt1, DateTime dt2) {
        return repo.findByITaskAndCreatedDateBetweenOrderByCreatedDateDesc(itask, dt1.toDate(), dt2.toDate());
    }

    @Override
    public List<Date> findTimestampByTask(ITask iTask) {
        return repo.findTimestampByITask(iTask);
    }

    @Override
    public List<ITaskHistory> listAll() {
        return repo.findAll();
    }

}
