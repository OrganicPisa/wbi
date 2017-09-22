package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.ResourcePlan;
import com.broadcom.wbi.model.mysql.SkillMapping;
import com.broadcom.wbi.repository.mysql.ResourcePlanRepository;
import com.broadcom.wbi.service.event.ResourcePlanSaveEvent;
import com.broadcom.wbi.service.event.ResourcePlanSaveEventPublisher;
import com.broadcom.wbi.service.jpa.ResourcePlanService;
import com.broadcom.wbi.service.jpa.SkillMappingService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("rawtypes")
@Service
public class ResourcePlanServiceImpl implements ResourcePlanService {

    @Resource
    private ResourcePlanRepository repo;

    private final SkillMappingService skillMappingService;
    private final ResourcePlanSaveEventPublisher resourcePlanSaveEventPublisher;

    @Autowired
    public ResourcePlanServiceImpl(SkillMappingService skillMappingService, ResourcePlanSaveEventPublisher resourcePlanSaveEventPublisher) {
        this.skillMappingService = skillMappingService;
        this.resourcePlanSaveEventPublisher = resourcePlanSaveEventPublisher;
    }

    @Override
    public ResourcePlan saveOrUpdate(ResourcePlan resourcePlan) {
        ResourcePlan rs = repo.save(resourcePlan);
        HashMap map = new HashMap<>();
        map.put("action", "save");
        map.put("data", rs);
        resourcePlanSaveEventPublisher.publish(new ResourcePlanSaveEvent(map));
        return rs;
    }

    @Override
    public List<ResourcePlan> saveBulk(List<ResourcePlan> resourcePlanList) {
        return repo.save(resourcePlanList);
    }

    @Override
    public void delete(Integer id) {
        HashMap map = new HashMap<>();
        map.put("action", "delete");
        map.put("data", id);
        resourcePlanSaveEventPublisher.publish(new ResourcePlanSaveEvent(map));
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
    public Page<ResourcePlan> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<ResourcePlan> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public ResourcePlan findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<ResourcePlan> listAll() {
        return repo.findAll(new Sort(Sort.Direction.ASC, "month"));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ResourcePlan> findByProgram(Program program) {
        return repo.findDistinctByProgram(program);
    }

    @Override
    @Transactional(readOnly = true)
    public Date findMaxResourceDate(Program program) {
        return repo.findMaxResourceDateByProgram(program);
    }

    @Override
    @Transactional(readOnly = true)
    public Date findMinResourceDate(Program program) {
        return repo.findMinResourceDateByProgram(program);
    }

    @Override
    public List<ResourcePlan> findByProgramAndType(Program program, String type) {
        return repo.findDistinctByProgramAndType(program, type);
    }

    @Override
    public List<String> findDistinctResourceType(Program program) {
        return repo.findDistinctTypeByProgram(program);
    }

    @Override
    public List<String> findDistinctTypeByProgram(Program program) {
        return repo.findDistinctTypeByProgram(program);
    }


    @Override
    @SuppressWarnings("unchecked")
    public void doParse(Program program, File file) {
        List list = new ArrayList();
        List<ResourcePlan> rplist = new ArrayList();
        List<SkillMapping> skills = new ArrayList();
        Set<String> exceltypeList = new HashSet<String>();
        try {
            FileInputStream is = new FileInputStream(file);
            Workbook wb = WorkbookFactory.create(is);
            sheetLoop:
            for (int sheet_num = 0; sheet_num < wb.getNumberOfSheets(); sheet_num++) {
                Sheet sheet = wb.getSheetAt(sheet_num);
                if (sheet.getRow(0) == null ||
                        sheet.getRow(0).getCell(0) == null ||
                        sheet.getRow(0).getCell(0).getCellTypeEnum().equals(CellType.BLANK)) {
                    continue sheetLoop;
                }
                if (sheet.getRow(0).getCell(0).getStringCellValue().toLowerCase().indexOf("mapping") == -1)
                    continue sheetLoop;
                Row row = null;
                Row dateRow = null;
                int skillCount = 0;
                List<String> projectRegion = new ArrayList();
                CellRangeAddress range;
                for (int m = 0; m < sheet.getNumMergedRegions(); m++) {
                    range = sheet.getMergedRegion(m);
                    String[] addr = range.formatAsString().split(":");
                    if ((addr[0].contains("A")) && (addr[1].contains("A"))) {
                        projectRegion.add(range.formatAsString());
                    }
                }
                Collections.sort(projectRegion);
                Collections.reverse(projectRegion);
                projectRegionLoop:
                for (String projectRange : projectRegion) {
                    String[] xpRange = projectRange.replaceAll("[a-zA-Z]", "").split(":");
                    int startRange = Integer.parseInt(xpRange[0]);
                    int stopRange = Integer.parseInt(xpRange[1]);
                    int titleStart = startRange - 1;
                    String title = sheet.getRow(titleStart).getCell(0).getStringCellValue();
                    //handle mapping table section
                    if (title.toLowerCase().indexOf("mapping") != -1) {
                        List<SkillMapping> skillMappingList = skillMappingService.findByProgram(program);
                        if (skillMappingList != null && !skillMappingList.isEmpty()) {
                            for (SkillMapping skillMapping : skillMappingList) {
                                skillMappingService.delete(skillMapping.getId());
                            }
                        }
                        for (int i = startRange; i < stopRange; i++) {
                            if (sheet.getRow(i) != null && sheet.getRow(i).getCell(1).getCellTypeEnum().equals(CellType.STRING)) {
                                SkillMapping skill = new SkillMapping();
                                skill.setPlan_skill(sheet.getRow(i).getCell(1).getStringCellValue().trim());
                                skill.setActual_skill(sheet.getRow(i).getCell(2).getStringCellValue().trim());
                                skill.setProgram(program);
                                skill.setOrderNum(Integer.valueOf(skillCount));
                                skill.setExclude(sheet.getRow(i).getCell(3).getStringCellValue().trim());
                                skillMappingService.saveOrUpdate(skill);
                                skillCount++;
                            }
                        }
                    }
                    //process data
                    else {
                        exceltypeList.add(title.toUpperCase().trim());
                        List<ResourcePlan> resourcePlanList = findByProgramAndType(program, title.trim());
                        dateRow = sheet.getRow(titleStart);
                        int colCount = dateRow.getLastCellNum();
                        if (resourcePlanList != null && !resourcePlanList.isEmpty()) {
                            if (title.equalsIgnoreCase("pc")) {
                                continue projectRegionLoop;
                            }
                            for (ResourcePlan resourcePlan : resourcePlanList) {
                                delete(resourcePlan.getId());
                            }
                        }
                        for (int rowIndex = startRange; rowIndex < stopRange; rowIndex++) {
                            row = sheet.getRow(rowIndex);
                            if (row.getCell(1).getCellTypeEnum().equals(CellType.STRING)) {
                                colloop:
                                for (int colIndex = 2; colIndex < colCount; colIndex++) {
                                    if (DateUtil.isCellDateFormatted(dateRow.getCell(colIndex))) {
                                        try {
                                            DateTime ds = new DateTime(dateRow.getCell(colIndex).getDateCellValue());
                                            if (ds.getYear() < 2005)
                                                continue colloop;
                                            ResourcePlan rp = new ResourcePlan();
                                            rp.setInclude_contractor(true);
                                            rp.setMonth(ds.toDate());
                                            rp.setProgram(program);
                                            rp.setPlan_skill(row.getCell(1).getStringCellValue().trim());
                                            rp.setType(title.toUpperCase().trim());
                                            Double count = 0.0;

                                            try {
                                                if (row.getCell(colIndex).getCellTypeEnum().equals(CellType.BLANK)) {
                                                    count = 0.0;
                                                } else if (row.getCell(colIndex).getCellTypeEnum().equals(CellType.NUMERIC)) {
                                                    count = row.getCell(colIndex).getNumericCellValue();
                                                } else if (row.getCell(colIndex).getCellTypeEnum().equals(CellType.STRING)) {
                                                    String cs = row.getCell(colIndex).getStringCellValue().replaceAll("\\s", "");
                                                    if (cs.indexOf("0.5") != -1) {
                                                        count = 0.5;
                                                    } else {
                                                        count = Double.parseDouble(cs);
                                                    }
                                                } else if (row.getCell(colIndex).getCellTypeEnum().equals(CellType.FORMULA)) {
                                                    count = 0.0;
                                                    if (row.getCell(colIndex).getCachedFormulaResultTypeEnum().equals(CellType.NUMERIC)) {
                                                        count = row.getCell(colIndex).getNumericCellValue();
                                                    } else if (row.getCell(colIndex).getCachedFormulaResultTypeEnum().equals(CellType.STRING)) {
                                                        String cs = row.getCell(colIndex).getStringCellValue().replaceAll("\\s", "");
                                                        if (cs.indexOf("0.5") != -1) {
                                                            count = 0.5;
                                                        } else {
                                                            count = Double.parseDouble(cs);
                                                        }
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                e.printStackTrace();
                                            }
                                            rp.setCount(count);
                                            saveOrUpdate(rp);
                                        } catch (IllegalStateException localIllegalStateException) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            List<String> dbTypeList = findDistinctResourceType(program);
            if (dbTypeList != null && !dbTypeList.isEmpty()) {
                if (exceltypeList != null && !exceltypeList.isEmpty()) {
                    for (String excelType : exceltypeList) {
                        if (!dbTypeList.contains(excelType)) {
                            List<ResourcePlan> resourcePlanList = findByProgramAndType(program, excelType.trim());
                            if (resourcePlanList != null && !resourcePlanList.isEmpty()) {
                                if (!excelType.equalsIgnoreCase("pc")) {
                                    for (ResourcePlan resourcePlan : resourcePlanList) {
                                        delete(resourcePlan.getId());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
