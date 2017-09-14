package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.Link;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.repository.mysql.LinkRepository;
import com.broadcom.wbi.service.jpa.LinkService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LinkServiceImpl implements LinkService {

    @Resource
    private LinkRepository repo;

    @Override
    @Transactional
    public Link saveOrUpdate(Link link) {
        return repo.save(link);
    }

    @Override
    @Transactional
    public List<Link> saveBulk(List<Link> links) {
        return repo.save(links);
    }

    @Override
    public List<Link> listAll() {
        return repo.findAll();
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
    public Page<Link> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Link> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Link findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> findByRevision(Revision rev) {
        return repo.findByRevisionOrderByCreatedDateDesc(rev);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> findByType(Revision rev, String type) {
        return repo.findByRevisionAndTypeOrderByCreatedDateDesc(rev, type.toLowerCase().trim());
    }

    @Override
    @Async
    public void cloneFromAnotherRevision(Revision oldRev, Revision rev, Authentication currentAuthentication) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(currentAuthentication);
        SecurityContextHolder.setContext(ctx);
        List<Link> links = findByRevision(oldRev);
        if (links != null && !links.isEmpty()) {
            for (Link l : links) {
                Link link = new Link();
                link.setCategory(l.getCategory());
                link.setDisplay_name(l.getDisplay_name());
                link.setOrderNum(l.getOrderNum());
                link.setRevision(rev);
                link.setType(l.getType());
                link.setUrl(l.getUrl());
                saveOrUpdate(link);
            }
        }
    }
}
