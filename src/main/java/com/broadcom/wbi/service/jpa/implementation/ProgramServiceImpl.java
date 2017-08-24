package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.repository.mysql.ProgramRepository;
import com.broadcom.wbi.service.jpa.ProgramService;
import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ProgramServiceImpl implements ProgramService {
    @Resource
    private ProgramRepository repo;


    @Override
    @Transactional
    public Program saveOrUpdate(Program program) {
        return repo.save(program);
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
    public Page<Program> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Program> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    @Transactional
    public List<Program> saveBulk(List<Program> programs) {
        return repo.save(programs);
    }

    @Override
    @Transactional(readOnly = true)
    public Program findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Program> findByName(String name) {
        return repo.findByName(name.trim());
    }

    //	@Override
//	@Transactional(readOnly = true)
//	public List<Program> findByNameType(String name, ProgramType type) {
//		List<Program> programs = new JPAQuery(em)
//				.from(qprogram)
//				.where(qprogram.name.toLowerCase().like("%"+name.toLowerCase()+"%")
//						.and(qprogram.type.eq(type)))
//				.orderBy(qprogram.name.asc()).list(qprogram);
//		if (programs != null && !programs.isEmpty())
//			return programs;
//		return null;
//	}
    @Override
    @Transactional(readOnly = true)
    public List<Program> checkExist(String name, String base, ProjectConstant.EnumProgramType type) {
        List<Program> programs = null;
        if (name != null && !name.trim().isEmpty() &&
                base != null && !base.trim().isEmpty()) {
            return repo.findByNameAndBaseNumAndType(name, base, type.toString());
        } else {
            if (name != null && !name.trim().isEmpty()) {
                return repo.findByNameAndType(name, type.toString());
            } else if (base != null && !base.trim().isEmpty()) {
                return repo.findByBaseNumAndType(base, type.toString());
            }
        }
        return null;
    }
//	@Override
//	@Transactional(readOnly = true)
//	public List<Program> checkExistBase(String base, ProgramType type) {
//		List<Program> programs = new JPAQuery(em).from(qprogram)
//				.where(qprogram.baseNum.like(base)
//						.and(qprogram.type.eq(type)))
//				.orderBy(qprogram.name.asc()).list(qprogram);
//		if (programs != null && !programs.isEmpty())
//			return programs;
//		return null;
//	}

    @Override
    @Transactional(readOnly = true)
    public List<Program> findBySegment(Segment segment, ProjectConstant.EnumProgramType type) {
        return repo.findBySegmentsContainsAndTypeOrderByOrderNumAsc(segment, type.toString());
    }


    @Override
    @Transactional(readOnly = true)
    public List<Program> listAll() {
        return repo.findAll();
    }
}
