package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.elasticSearch.TemplateSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionContact;
import com.broadcom.wbi.repository.mysql.RevisionContactRepository;
import com.broadcom.wbi.service.elasticSearch.RevisionContactSearchService;
import com.broadcom.wbi.service.elasticSearch.TemplateSearchService;
import com.broadcom.wbi.service.event.RevisionContactSaveEvent;
import com.broadcom.wbi.service.event.RevisionContactSaveEventPublisher;
import com.broadcom.wbi.service.jpa.RevisionContactService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class RevisionContactServiceImpl implements RevisionContactService {
    private final TemplateSearchService templateSearchService;
    private final RevisionContactSearchService revisionContactSearchService;
    private final RevisionContactSaveEventPublisher revisionContactSaveEventPublisher;
    @Resource
    private RevisionContactRepository repo;

    @Autowired
    public RevisionContactServiceImpl(TemplateSearchService templateSearchService, RevisionContactSearchService revisionContactSearchService, RevisionContactSaveEventPublisher revisionContactSaveEventPublisher) {
        this.templateSearchService = templateSearchService;
        this.revisionContactSearchService = revisionContactSearchService;
        this.revisionContactSaveEventPublisher = revisionContactSaveEventPublisher;
    }

    @Override
    public RevisionContact saveOrUpdate(RevisionContact programContact) {
        RevisionContact contact = repo.save(programContact);
        HashMap map = new HashMap<>();
        map.put("action", "save");
        map.put("data", contact);
        revisionContactSaveEventPublisher.publish(new RevisionContactSaveEvent(map));
        return contact;
    }

    @Override
    public void delete(Integer id) {
        HashMap map = new HashMap<>();
        map.put("action", "delete");
        map.put("data", id);
        revisionContactSaveEventPublisher.publish(new RevisionContactSaveEvent(map));
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
    @Async
    public void cloneFromAnotherRevision(Revision oldRev, Revision rev, Authentication currentAuthentication) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(currentAuthentication);
        SecurityContextHolder.setContext(ctx);
        List<RevisionContact> revisionContactList = findByRevision(oldRev);
        if ((revisionContactList != null) && (!revisionContactList.isEmpty())) {
            for (RevisionContact revisionContact : revisionContactList) {
                RevisionContact contact = new RevisionContact();
                contact.setName(revisionContact.getName());
                contact.setOnDashboard(Boolean.valueOf(true));
                contact.setRevision(rev);
                contact.setValue(revisionContact.getValue());
                saveOrUpdate(contact);
            }
        }
    }

    @Override
    @Async
    public void cloneFromTemplate(Revision rev, Authentication currentAuthentication) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(currentAuthentication);
        SecurityContextHolder.setContext(ctx);
        Program program = rev.getProgram();
        List<TemplateSearch> templateSearchList = templateSearchService.findByTypeCategory(program.getType().toString().toLowerCase(), "contact", null);
        for (TemplateSearch templateSearch : templateSearchList) {
            RevisionContact contact = new RevisionContact();
            contact.setName(templateSearch.getName());
            contact.setOnDashboard(Boolean.valueOf(true));
            contact.setRevision(rev);
            contact.setValue("");
            saveOrUpdate(contact);
        }
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
