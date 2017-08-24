package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionIP;
import com.broadcom.wbi.repository.mysql.RevisionIPRepository;
import com.broadcom.wbi.service.jpa.RevisionIPService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RevisionIPServiceImpl implements RevisionIPService {

    final static DateTimeFormatter dfmt = DateTimeFormat.forPattern("MM/dd/yy");
    @Resource
    private RevisionIPRepository repo;

    @Override
    @Transactional
    public RevisionIP saveOrUpdate(RevisionIP RevisionIP) {
        return repo.save(RevisionIP);
    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
    public void delete(Integer id) {
        repo.delete(id);
    }

    @Override
    @Transactional
    public List<RevisionIP> saveBulk(List<RevisionIP> RevisionIPs) {
        return repo.save(RevisionIPs);
    }

    @Override
    @Transactional(readOnly = true)
    public RevisionIP findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionIP> findByRevision(Revision rev) {
        return repo.findDistinctByRevisionOrderByCreatedDateDesc(rev);
    }

    @Override
    @Transactional(readOnly = true)
    public RevisionIP findByRevision(Revision rev, Revision revip) {

        return repo.findFirstByRevisionAndIprevisionOrderByCreatedDateDesc(rev, revip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionIP> findByRevisionIP(Revision rev) {

        return repo.findDistinctByIprevisionOrderByCreatedDateDesc(rev);
    }

    @Override
    public List<RevisionIP> listAll() {
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
    public Page<RevisionIP> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<RevisionIP> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

}
