package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.ResourceProgramClassification;
import com.broadcom.wbi.repository.mysql.ResourceProgramClassificationRepository;
import com.broadcom.wbi.service.jpa.ResourceProgramClassificationService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ResourceProgramClassificationServiceImpl implements ResourceProgramClassificationService {

    @Resource
    private ResourceProgramClassificationRepository repo;

    @Override
    public ResourceProgramClassification saveOrUpdate(ResourceProgramClassification resourceProgramClassification) {
        return repo.save(resourceProgramClassification);
    }

    @Override
    public List<ResourceProgramClassification> saveBulk(List<ResourceProgramClassification> resourceProgramClassificationList) {
        return repo.save(resourceProgramClassificationList);
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
    public Page<ResourceProgramClassification> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<ResourceProgramClassification> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public ResourceProgramClassification findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<ResourceProgramClassification> listAll() {
        return repo.findAll(new Sort(Sort.Direction.ASC, "name"));
    }

    @Override
    public List<ResourceProgramClassification> findByType(String type, Boolean status) {
        return repo.findDistinctByTypeAndStatusOrderByCreatedDateAsc(type, status);
    }

    @Override
    public List<ResourceProgramClassification> findByNameType(String type, String name) {
        if (type != null && name != null)
            return repo.findDistinctByTypeAndNameOrderByCreatedDateDesc(type, name);
        if (type == null && name != null)
            return repo.findDistinctByNameOrderByCreatedDateDesc(name);
        if (type != null && name == null)
            return repo.findDistinctByTypeOrderByCreatedDateDesc(type);
        return null;
    }
}
