package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.SkillMapping;
import com.broadcom.wbi.repository.mysql.SkillMappingRepository;
import com.broadcom.wbi.service.jpa.SkillMappingService;
import org.joda.time.DateTime;
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
public class SkillMappingServiceImpl implements SkillMappingService {

    @Resource
    private SkillMappingRepository repo;

    @Override
    public SkillMapping saveOrUpdate(SkillMapping SkillMapping) {
        return repo.save(SkillMapping);
    }

    @Override
    public List<SkillMapping> saveBulk(List<SkillMapping> SkillMappingList) {
        return repo.save(SkillMappingList);
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
    public Page<SkillMapping> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<SkillMapping> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public SkillMapping findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<SkillMapping> listAll() {
        return repo.findAll(new Sort(Sort.Direction.ASC, "order_num"));
    }


    @Override
    public List<SkillMapping> findByProgram(Program program) {
        return repo.findDistinctByProgramOrderByOrderNumAsc(program);
    }
}
