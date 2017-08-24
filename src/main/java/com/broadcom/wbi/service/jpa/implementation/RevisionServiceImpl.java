package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.repository.mysql.RevisionRepository;
import com.broadcom.wbi.service.jpa.RevisionService;
import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class RevisionServiceImpl implements RevisionService {

    @Resource
    private RevisionRepository repo;

    @Override
    public Revision saveOrUpdate(Revision revision) {
        return repo.save(revision);
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
    public List<Revision> findByUpdateTime(DateTime dt) {
        return repo.findByCreatedDateAfterOrderByCreatedDateDesc(dt.toDate());
    }

    @Override
    public Page<Revision> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Revision> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public void delete(Integer id) {
        repo.delete(id);
    }

    @Override
    public List<Revision> saveBulk(List<Revision> revisions) {
        return repo.save(revisions);
    }

    @Override
    public Revision findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<Revision> findByProgram(Program program, DateTime dt) {
        if (dt == null)
            return repo.findByProgramOrderByNameAsc(program);
        return repo.findByProgramAndCreatedDateAfter(program, dt.toDate());
    }

    @Override
    public Revision findByProgramName(Program program, String name) {
        return repo.findFirstByProgramAndNameOrderByNameAsc(program, name.toUpperCase());
    }

    @Override
    public List<Integer> findByEmployee(Employee empl) {
        List<Revision> revs = repo.findByEmployeesContainsAndAndIsActive(empl, ProjectConstant.EnumProgramStatus.ACTIVE);
        if (revs != null) {
            List<Integer> ret = new ArrayList<>();
            for (Revision rev : revs) {
                ret.add(rev.getId());
            }
            return ret;
        }
        return null;
    }

    @Override
    public List<Revision> listAll() {
        return repo.findAll(new Sort(Sort.Direction.ASC, "created_date"));
    }

    @Override
    public boolean isEmployeeBookmark(Revision rev, Employee empl) {
        List<Revision> revs = repo.findByEmployeesContainsOrderByIsActiveDesc(empl);

        if (revs != null && !revs.isEmpty()) {
            if (revs.contains(rev))
                return true;
        }
        return false;
    }

//	@Override
//	public List<Integer> findBySegment(Segment segment, String name, ProjectConstant.EnumProgramType type) {
//		List<Integer> revs = new JPAQuery(em).from(qrev).innerJoin(qrev.program, qprogram)
//				.innerJoin(qprogram.segments, qsegment)
//				.where(qsegment.eq(segment).and(qrev.name.toLowerCase().like("%" + name.toLowerCase() + "%"))
//						.and(qprogram.type.eq(type)))
//				.orderBy(qrev.orderNum.asc(), qrev.createdDate.desc()).list(qrev.id);
//		if (revs != null && !revs.isEmpty())
//			return revs;
//		return null;
//	}

//	@Override
//	public List<Integer> findBySegment(Segment segment, ProjectConstant.EnumProgramType type, ProjectConstant.EnumProgramStatus stat) {
//		List<Integer> revs = null;
//		if (segment != null && type != null && stat != null) {
//			revs = new JPAQuery(em).from(qrev).innerJoin(qrev.program, qprogram).innerJoin(qprogram.segments, qsegment)
//					.where(qsegment.eq(segment).and(qprogram.type.eq(type)).and(qrev.isActive.eq(stat)))
//					.orderBy(qrev.orderNum.asc(), qrev.createdDate.desc()).list(qrev.id);
//		} else {
//			if (segment != null) {
//				if (type != null) {
//					revs = new JPAQuery(em).from(qrev).innerJoin(qrev.program, qprogram)
//							.innerJoin(qprogram.segments, qsegment)
//							.where(qsegment.eq(segment).and(qprogram.type.eq(type))).orderBy(qrev.name.desc())
//							.list(qrev.id);
//				} else {
//					revs = new JPAQuery(em).from(qrev).innerJoin(qprogram.segments, qsegment)
//							.where(qsegment.eq(segment).and(qrev.isActive.eq(stat))).orderBy(qrev.name.desc())
//							.list(qrev.id);
//				}
//			} else {
//				if (type != null && stat != null) {
//					revs = new JPAQuery(em).from(qrev).innerJoin(qrev.program, qprogram)
//							.where(qprogram.type.eq(type).and(qrev.isActive.eq(stat)))
//							.orderBy(qrev.orderNum.asc(), qrev.createdDate.desc()).list(qrev.id);
//				} else {
//					if (type != null) {
//						revs = new JPAQuery(em).from(qrev).innerJoin(qrev.program, qprogram)
//								.where(qprogram.type.eq(type)).orderBy(qrev.orderNum.asc(), qrev.createdDate.desc())
//								.list(qrev.id);
//					} else if (stat != null) {
//						revs = new JPAQuery(em).from(qrev).where(qrev.isActive.eq(stat))
//								.orderBy(qrev.orderNum.asc(), qrev.createdDate.desc()).list(qrev.id);
//					}
//				}
//			}
//		}
//		if (revs != null && !revs.isEmpty())
//			return revs;
//		return null;
//	}

//	@Override
//	public List<Integer> findBySegmentGroup(String segmentGroup, ProjectConstant.EnumProgramStatus stat) {
//		List<Integer> revs = new JPAQuery(em).from(qrev).innerJoin(qrev.program, qprogram)
//				.innerJoin(qprogram.segments, qsegment)
//				.where(qsegment.segmentGroup.toLowerCase().like(segmentGroup.toLowerCase()).and(qrev.isActive.eq(stat)))
//				.orderBy(qrev.orderNum.asc(), qrev.createdDate.desc()).list(qrev.id);
//		if (revs != null && !revs.isEmpty())
//			return revs;
//		return null;
//	}

}
