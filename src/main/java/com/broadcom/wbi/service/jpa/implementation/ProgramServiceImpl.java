package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.model.mysql.Sku;
import com.broadcom.wbi.repository.mysql.ProgramRepository;
import com.broadcom.wbi.service.event.ProgramSaveEvent;
import com.broadcom.wbi.service.event.ProgramSaveEventPublisher;
import com.broadcom.wbi.service.jpa.ProgramService;
import com.broadcom.wbi.service.jpa.SegmentService;
import com.broadcom.wbi.service.jpa.SkuService;
import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProgramServiceImpl implements ProgramService {
    @Resource
    private ProgramRepository repo;

    private final ProgramSaveEventPublisher programSaveEventPublisher;
    private final SegmentService segmentService;
    private final SkuService skuService;

    @Autowired
    public ProgramServiceImpl(ProgramSaveEventPublisher programSaveEventPublisher, SegmentService segmentService, SkuService skuService) {
        this.programSaveEventPublisher = programSaveEventPublisher;
        this.segmentService = segmentService;
        this.skuService = skuService;
    }

    @Override
    @Transactional
    public Program saveOrUpdate(Program program) {
        Program p = repo.save(program);
        HashMap map = new HashMap<>();
        map.put("action", "save");
        map.put("data", p);
        programSaveEventPublisher.publish(new ProgramSaveEvent(map));
        return program;
    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
    public void delete(Integer id) {
        HashMap map = new HashMap<>();
        map.put("action", "delete");
        map.put("data", id);
        programSaveEventPublisher.publish(new ProgramSaveEvent(map));
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


    @Override
    public Program createNewProgram(HashMap map, String createtypestring) {
        Program p = null;
        ProjectConstant.EnumProgramType programType = ProjectConstant.EnumProgramType.CHIP;
        String baseNum = "NA";
        if (map.containsKey("baseDie")) {
            baseNum = map.get("baseDie").toString();
        }
        String pname = map.get("pname").toString().trim();
        if (pname.isEmpty())
            return null;

        List<Program> programList = findByName(pname);
        if (programList != null && !programList.isEmpty())
            return null;

        Segment seg = segmentService.findByName("XGS");
        Set<Segment> segs = new HashSet<Segment>();
        if (createtypestring.toLowerCase().indexOf("customer") == 0) {
            programType = ProjectConstant.EnumProgramType.CUSTOMER;
            seg = segmentService.findByName("customer");
        } else if (createtypestring.toLowerCase().indexOf("software") == 0) {
            programType = ProjectConstant.EnumProgramType.SOFTWARE;
            seg = segmentService.findByName("software");
        } else if (createtypestring.toLowerCase().indexOf("ip") == 0) {
            programType = ProjectConstant.EnumProgramType.IP;
            seg = segmentService.findByName("ip");
        } else {
            programType = ProjectConstant.EnumProgramType.CHIP;
            if (map.containsKey("segment")) {
                HashMap segmap = (HashMap) map.get("segment");
                if (segmap.containsKey("id")) {
                    seg = segmentService.findById(Integer.parseInt(segmap.get("id").toString()));
                }
            }
        }
        if (seg != null) {
            segs.add(seg);
        }
        p = new Program();
        p.setBaseNum(baseNum);
        p.setSegments(segs);
        p.setDisplayName(map.get("pname").toString());
        p.setIsProgramIncludeInReport(true);
        p.setName(map.get("pname").toString());
        p.setType(programType);
        p.setOrderNum(0);
        p = saveOrUpdate(p);

        if (createtypestring.toLowerCase().indexOf("ip") == 0) {
            if (map.containsKey("info")) {
                HashMap infomap = (HashMap) map.get("info");
                if (createtypestring.toLowerCase().indexOf("ipprogram") == 0 && infomap.containsKey("category")) {
                    String catname = infomap.get("category").toString() + "_hidden";
                    List<Program> cat_pl = findByName(catname.toLowerCase());
                    if (cat_pl == null || cat_pl.isEmpty()) {
                        segs = new HashSet<Segment>();
                        segs.add(seg);
                        Program cat_p = new Program();
                        cat_p.setBaseNum("NA");
                        cat_p.setSegments(segs);
                        cat_p.setDisplayName(catname.toLowerCase());
                        cat_p.setIsProgramIncludeInReport(true);
                        cat_p.setName(catname);
                        cat_p.setType(programType);
                        cat_p.setOrderNum(0);
                        cat_p = saveOrUpdate(cat_p);

                        Sku sku = new Sku();
                        String aka = map.get("pname").toString();
                        sku.setAka(aka);
                        sku.setDescription("");
                        sku.setFrequency("");
                        sku.setIoCapacity("");
                        sku.setNumOfSerdes("");
                        sku.setPortConfig("");
                        sku.setSkuNum(p.getBaseNum());
                        sku.setDateAvailable("");
                        sku.setProgram(cat_p);
                        sku.setItemp("");
                        skuService.saveOrUpdate(sku);
                    }
                }
            }
        }
        return p;
    }
}
