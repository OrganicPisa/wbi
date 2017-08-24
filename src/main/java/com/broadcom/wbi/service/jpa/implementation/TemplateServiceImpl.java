package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Template;
import com.broadcom.wbi.repository.mysql.TemplateRepository;
import com.broadcom.wbi.service.jpa.TemplateService;
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
public class TemplateServiceImpl implements TemplateService {
    @Resource
    private TemplateRepository repo;

    @Override
    public Template saveOrUpdate(Template template) {
        return repo.save(template);
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
    public Page<Template> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Template> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public List<Template> saveBulk(List<Template> skills) {
        return repo.save(skills);
    }

    @Override
    public Template findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<Template> listAll() {
        return repo.findAll();
    }

    @Override
    public List<Template> findByType(String type) {
        return repo.findByTypeOrderByCreatedDateDesc(type.toLowerCase().trim());
    }

    @Override
    public List<Template> findByTypeCategory(String type, String category) {
        return repo.findByTypeAndCategoryOrderByCreatedDateDesc(type.toLowerCase().trim(), category.toLowerCase().trim());
    }

    @Override
    public List<Template> findByTypeCategoryGroup(String type, String category, String group) {
        return repo.findByTypeAndCategoryAndGroupOrderByCreatedDateDesc(type.toLowerCase().trim(),
                category.toLowerCase().trim(), group.toLowerCase().trim());
    }

    @Override
    public List<Template> findByTypeCategoryGroupOndashboard(String type, String category, String group) {

        return repo.findByTypeAndCategoryAndGroupAndOnDashboardIsTrueOrderByCreatedDateDesc(type.toLowerCase().trim(),
                category.toLowerCase().trim(), group.toLowerCase().trim());
    }
}
