package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionContact;
import com.broadcom.wbi.repository.mysql.RevisionContactRepository;
import com.broadcom.wbi.service.elasticSearch.RevisionContactSearchService;
import com.broadcom.wbi.service.elasticSearch.TemplateSearchService;
import com.broadcom.wbi.service.jpa.RevisionContactService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RevisionContactServiceImpl implements RevisionContactService {
    @Resource
    private RevisionContactRepository repo;
    @Autowired
    private TemplateSearchService templSearchServ;
    @Autowired
    private RevisionContactSearchService rcSearchServ;

    @Override
    public RevisionContact saveOrUpdate(RevisionContact programContact) {
        return repo.save(programContact);
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
    public Page<RevisionContact> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<RevisionContact> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    @Transactional
    public List<RevisionContact> saveBulk(List<RevisionContact> programContacts) {
        return repo.save(programContacts);
    }


    @Override
    @Transactional(readOnly = true)
    public RevisionContact findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RevisionContact findByRevisionTitle(Revision rev, String title) {
        return repo.findFirstByRevisionAndNameOrderByNameAsc(rev, title);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionContact> findByRevision(Revision rev) {
        return repo.findByRevisionOrderByNameAsc(rev);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionContact> listAll() {
        return repo.findAll(new Sort(Sort.Direction.ASC, "name"));
    }


}
