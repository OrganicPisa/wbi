package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.repository.mysql.RevisionRepository;
import com.broadcom.wbi.service.event.RevisionSaveEvent;
import com.broadcom.wbi.service.event.RevisionSaveEventPublisher;
import com.broadcom.wbi.service.jpa.HeadlineService;
import com.broadcom.wbi.service.jpa.ProgramService;
import com.broadcom.wbi.service.jpa.RevisionService;
import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(rollbackFor = {Exception.class})
public class RevisionServiceImpl implements RevisionService {

    @Resource
    private RevisionRepository repo;
    private final RevisionSaveEventPublisher revisionSaveEventPublisher;
    @Autowired
    private ProgramService programService;
    @Autowired
    private HeadlineService headlineService;


    @Autowired
    public RevisionServiceImpl(RevisionSaveEventPublisher revisionSaveEventPublisher) {
        this.revisionSaveEventPublisher = revisionSaveEventPublisher;
    }

    @Override
    public Revision saveOrUpdate(Revision revision) {
        Revision rev = repo.save(revision);
        HashMap map = new HashMap<>();
        map.put("action", "save");
        map.put("data", revision);
        revisionSaveEventPublisher.publish(new RevisionSaveEvent(map));
        return rev;
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
        HashMap map = new HashMap<>();
        map.put("action", "save");
        map.put("data", id);
        revisionSaveEventPublisher.publish(new RevisionSaveEvent(map));
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

    @Override
    public Revision createNewRevision(HashMap map, Program program) {
        final Segment seg = program.getSegments().iterator().next();
        final ProjectConstant.EnumProgramStatus status = ProjectConstant.EnumProgramStatus.ACTIVE;
        final ProjectConstant.EnumIndicatorStatus flag = ProjectConstant.EnumIndicatorStatus.BLACK;
        //handle ip program
        if (program.getType().equals(ProjectConstant.EnumProgramType.IP)) {
            if (map.containsKey("info")) {
                HashMap infomap = (HashMap) map.get("info");
                if (infomap.containsKey("category")) {
                    String catname = infomap.get("category").toString() + "_hidden";
                    List<Program> cat_pl = programService.findByName(catname.toLowerCase());
                    if (cat_pl != null && !cat_pl.isEmpty()) {
                        Program cat_p = cat_pl.get(0);
                        if (cat_p != null) {
                            Revision rev = findByProgramName(cat_p, "head_ip");
                            if (rev == null) {
                                rev = new Revision();
                                rev.setIpRelated("");
                                rev.setIsActive(status);
                                rev.setIsRevisionIncludeInReport(true);
                                rev.setName("head_ip");
                                rev.setOrderNum(1);
                                rev.setProgram(cat_p);
                                rev.setIsProtected(false);
                                rev = saveOrUpdate(rev);

                                Headline hl = new Headline();
                                hl.setBudget_flag(flag);
                                hl.setHeadline("");
                                hl.setIsActive(status);
                                hl.setPrediction_flag(flag);
                                hl.setResource_flag(flag);
                                hl.setSchedule_flag(flag);
                                hl.setBudget_flag(flag);
                                hl.setRevision(rev);
                                hl.setStage(ProjectConstant.EnumHeadlineStage.PLANNING);
                                hl = headlineService.saveOrUpdate(hl);
                            }
                        }
                    }
                }
            }
        }
        try {
            // insert new revision
            String rname = "A0";
            if (map.containsKey("rname")) {
                rname = map.get("rname").toString().toUpperCase();
            }
            Revision rev = findByProgramName(program, rname);
            if (rev == null) {
                rev = new Revision();
                rev.setIpRelated("");
                rev.setIsActive(status);
                rev.setIsRevisionIncludeInReport(true);
                rev.setName(rname);
                rev.setOrderNum(1);
                rev.setProgram(program);
                rev.setIsProtected(false);
                rev = saveOrUpdate(rev);
            }

            return rev;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

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
