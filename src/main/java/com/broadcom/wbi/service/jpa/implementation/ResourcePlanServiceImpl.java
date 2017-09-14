package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.ResourcePlan;
import com.broadcom.wbi.repository.mysql.ResourcePlanRepository;
import com.broadcom.wbi.service.jpa.ResourcePlanService;
import com.broadcom.wbi.service.jpa.SkillMappingService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@SuppressWarnings("rawtypes")
@Service
public class ResourcePlanServiceImpl implements ResourcePlanService {

    @Resource
    private ResourcePlanRepository repo;

    @Autowired
    private SkillMappingService skillMappingService;

    @Override
    public ResourcePlan saveOrUpdate(ResourcePlan resourcePlan) {
        return repo.save(resourcePlan);
    }

    @Override
    public List<ResourcePlan> saveBulk(List<ResourcePlan> resourcePlanList) {
        return repo.save(resourcePlanList);
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
    public List<String> findDistinctPlanSkill(Program program, String type) {
        return repo.findDistinctSkillByProgramAndType(program, type);
    }

    @Override
    public List<String> findDistinctResourceType(Program program) {
        return repo.findDistinctTypeByProgram(program);
    }


//	@Override
//	@SuppressWarnings("unchecked")
//	public List doParse(String f, Program program, String username) {
//		File file = new File(f);
//		List list = new ArrayList();
//		List<ResourcePlan> rplist = new ArrayList();
//		List<SkillMapping> skills = new ArrayList();
//		try {
//			FileInputStream is = new FileInputStream(file);
//			Workbook wb = WorkbookFactory.create(is);
//			for (int sheet_num = 0; sheet_num < wb.getNumberOfSheets(); sheet_num++) {
//				Sheet sheet = wb.getSheetAt(sheet_num);
//				Row row = null;
//				Row dateRow = null;
//				int skillCount = 0;
//
//				List<String> projectRegion = new ArrayList();
//				CellRangeAddress range;
//				for (int m = 0; m < sheet.getNumMergedRegions(); m++) {
//					range = sheet.getMergedRegion(m);
//					String[] addr = range.formatAsString().split(":");
//					if ((addr[0].contains("A")) && (addr[1].contains("A"))) {
//						projectRegion.add(range.formatAsString());
//					}
//				}
//				Collections.sort(projectRegion);
//				Collections.reverse(projectRegion);
//				for (String projectRange : projectRegion) {
//					String[] xpRange = projectRange.replaceAll("[a-zA-Z]", "").split(":");
//					int start = Integer.parseInt(xpRange[0]);
//					int stop = Integer.parseInt(xpRange[1]);
//					int titleStart = start - 1;
//					String title = sheet.getRow(titleStart).getCell(0)
//							.getStringCellValue();
//					if (title.toLowerCase().indexOf("map") != -1) {
//						QSkillMapping qsm = QSkillMapping.skillMapping;
//						List<SkillMapping> skillList = new JPAQuery(em)
//								.from(qsm).where(qsm.program.eq(program)).list(qsm);
//						if (skillList != null) {
//							for (SkillMapping skillmap : skillList) {
//								try {
//									skillServ.delete(skillmap.getId());
//								} catch (IDNotFoundException e) {
//									e.printStackTrace();
//								}
//							}
//						}
//						for (int i = start; i < stop; i++) {
//							if ((sheet.getRow(i) != null)
//									&& (sheet.getRow(i).getCell(1)
//											.getCellType() != 3)) {
//								SkillMapping skill = new SkillMapping();
//								skill.setPlan_skill(sheet.getRow(i).getCell(1).getStringCellValue().trim());
//								skill.setActual_skill(sheet.getRow(i).getCell(2).getStringCellValue().trim());
//								skill.setProgram(program);
//								skill.setOrderNum(Integer.valueOf(skillCount));
//								skill.setCreatedBy(username);
//								skill.setCreatedDate(new Date());
//								skill.setExclude(sheet.getRow(i).getCell(3).getStringCellValue().trim());
//								skills.add(skill);
//								skillCount++;
//							}
//						}
//					} else {
//						List<Integer> rppl = new JPAQuery(em)
//								.from(qrpp)
//								.where(qrpp.program.eq(program).and(
//										qrpp.type.toLowerCase().like(title)))
//								.list(qrpp.id);
//						dateRow = sheet.getRow(titleStart);
//						int colCount = dateRow.getLastCellNum();
//						title = dateRow.getCell(0).getStringCellValue().trim();
//						if ((rppl == null) || (rppl.isEmpty())) {
//							for (int i = start; i < stop; i++) {
//								row = sheet.getRow(i);
//								if (row.getCell(1).getCellType() != 3) {
//									for (int j = 2; j < colCount; j++) {
//										if (dateRow.getCell(j).getCellType() != 3) {
//											try {
//												ResourcePlan rp = new ResourcePlan();
//												rp.setCreatedBy(username);
//												rp.setInclude_contractor(true);
//												rp.setMonth(dateRow.getCell(j).getDateCellValue());
//												rp.setProgram(program);
//												rp.setPlan_skill(row.getCell(1).getStringCellValue().trim());
//												rp.setType(title);
//												rp.setCreatedDate(new Date());
//												Double count = 0.0;
//												switch (row.getCell(j).getCellType()) {
//												case 3:
//													count = 0.0;
//													break;
//												case 0:
//													count = row.getCell(j).getNumericCellValue();
//													break;
//												case 1:
//													String cs = row.getCell(j).getStringCellValue().replaceAll("\\s",	"");
//													if (cs.indexOf("0.5") != -1) {
//														count = 0.5;
//													} else {
//														count = Double.parseDouble(cs);
//													}
//													break;
//												}
//												rp.setCount(count);
//												rplist.add(rp);
//											} catch (IllegalStateException localIllegalStateException) {
//											}
//										}
//									}
//								}
//							}
//						} else {
//							rppl = new JPAQuery(em).from(qrpp)
//									.where(qrpp.program.eq(program)
//											.and(qrpp.type.toLowerCase().like("por")))
//											.list(qrpp.id);
//							if (rppl != null && !rppl.isEmpty()) {
//								for (Integer rp_id : rppl) {
//									try {
//										delete(rp_id);
//									} catch (IDNotFoundException e) {
//										e.printStackTrace();
//									}
//								}
//							}
//							for (int i = start; i < stop; i++) {
//								row = sheet.getRow(i);
//								if (row.getCell(1).getCellType() != 3) {
//									for (int j = 2; j < colCount; j++) {
//										if (dateRow.getCell(j).getCellType() != 3) {
//											try {
//												ResourcePlan rp = new ResourcePlan();
//												rp.setCreatedBy(username);
//												rp.setInclude_contractor(true);
//												rp.setMonth(dateRow.getCell(j).getDateCellValue());
//												rp.setProgram(program);
//												rp.setPlan_skill(row.getCell(1).getStringCellValue().trim());
//												rp.setType("POR");
//												rp.setCreatedDate(new Date());
//												Double count = 0.0;
//												switch (row.getCell(j).getCellType()) {
//												case 3:
//													count = 0.0;
//													break;
//												case 0:
//													count = row.getCell(j).getNumericCellValue();
//													break;
//												case 1:
//													String cs = row.getCell(j).getStringCellValue().replaceAll("\\s","");
//													if (cs.indexOf("0.5") != -1) {
//														count = 0.5D;
//													} else {
//														count = Double.parseDouble(cs);
//													}
//													break;
//												}
//												rp.setCount(count);
//												rplist.add(rp);
//											} catch (IllegalStateException localIllegalStateException) {
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			list.add(skills);
//			list.add(rplist);
//			return list;
//		} catch (InvalidFormatException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

}
