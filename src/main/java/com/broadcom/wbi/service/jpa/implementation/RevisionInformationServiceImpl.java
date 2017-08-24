package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionInformation;
import com.broadcom.wbi.repository.mysql.RevisionInformationRepository;
import com.broadcom.wbi.service.jpa.RevisionInformationService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class RevisionInformationServiceImpl implements RevisionInformationService {

    @Resource
    private RevisionInformationRepository repo;

    @Override
    @Transactional
    public RevisionInformation saveOrUpdate(RevisionInformation revisionInformation) {
        return repo.save(revisionInformation);
    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
    public void delete(Integer id) {
        repo.delete(id);
    }

    @Override
    @Transactional
    public List<RevisionInformation> saveBulk(List<RevisionInformation> revisionInformations) {
        return repo.save(revisionInformations);
    }

    @Override
    @Transactional(readOnly = true)
    public RevisionInformation findById(Integer id) {

        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionInformation> findByRevision(Revision rev) {

        return repo.findByRevisionOrderByOrderNumAsc(rev);
    }

    @Override
    public List<RevisionInformation> findByRevisionPhase(Revision rev, String phase) {
        return repo.findByRevisionAndPhaseOrderByOrderNumAsc(rev, phase.toLowerCase().trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionInformation> findByRevision(Revision rev, boolean onDashboard) {
        if (onDashboard) {
            return repo.findDistinctByRevisionAndOnDashboardIsTrueOrderByOrderNumAsc(rev, onDashboard);
        }
        return repo.findByRevisionOrderByOrderNumAsc(rev);
    }

    @Override
    public List<RevisionInformation> findByRevisionPhaseName(Revision rev, String phase, String name) {
        return repo.findDistinctByRevisionAndPhaseAndNameOrderByOrderNumAsc(rev, phase.toLowerCase().trim(), name.toLowerCase().trim());
    }

//	@Override
//	public void updateValueByRevisionPhaseName(Revision rev,String phase, HashMap infomap) {
//		for (Object key : infomap.keySet()){
//			List<RevisionInformation> ril = findByRevisionPhaseName(rev, "current", key.toString().toLowerCase());
//
//			if(ril != null){
//				for (RevisionInformation ri : ril){
//					ri.setValue(infomap.get(key).toString());
//					update(ri);
//				}
//			}
//			List<RevisionInformationSearch> risl = riSearchServ.findByRevisionPhaseName(rev.getId(), "current", key.toString().toLowerCase());
//			if(risl != null){
//				for(RevisionInformationSearch ris : risl){
//					ris.setValue(infomap.get(key).toString());
//					riSearchServ.save(ris);
//				}
//			}
//		}
//	}

    @Override
    public List<RevisionInformation> listAll() {

        return repo.findAll(new Sort(Sort.Direction.ASC, "name"));
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
    public Page<RevisionInformation> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<RevisionInformation> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

//	@Override
//	public void initInfoNewRevision(Revision rev, String createtypestring, String username){
//		List<TemplateSearch> infoTemplateSearchList = templSearchServ.findByTypeCategory(createtypestring.toLowerCase(), "information", null);
//		Timestamp ts = new Timestamp(new Date().getTime());
//		List<String> phases = new ArrayList<String>();
//		if(createtypestring.toLowerCase().indexOf("chip")!=-1 ||
//				createtypestring.toLowerCase().indexOf("rev")!=-1){
//			phases= Arrays.asList("ca", "pc", "ecr1", "ecr2", "ecr3", "current", "to/final");
//		}
//		else{
//			phases= Arrays.asList("current");
//		}
//
//		//add information
//		for (String phase : phases){
//			for (TemplateSearch temp : infoTemplateSearchList){
//				RevisionInformation pi = new RevisionInformation();
//				pi.setCreatedBy(username);
//				pi.setOrderNum(temp.getOrderNum());
//				pi.setCreatedDate(ts);
//				pi.setLastUpdatedDate(ts);
//				pi.setIsRestrictedView(temp.getIsRestrictedView());
//				pi.setName(temp.getName());
//				pi.setRevision(rev);
//				pi.setValue("");
//				pi.setPhase(phase);
//				if(phase.equalsIgnoreCase("ca")){
//					pi.setIsUserEditable(temp.getAvailableCA());
//				}
//				else if(phase.equalsIgnoreCase("pc")){
//					pi.setIsUserEditable(temp.getAvailablePC());
//				}
//				else if(phase.equalsIgnoreCase("current")){
//					pi.setIsUserEditable(temp.getAvailableCurrent());
//				}
//				else if(phase.equalsIgnoreCase("to/final")){
//					pi.setIsUserEditable(temp.getAvailableTO());
//				}
//				else if(phase.toLowerCase().indexOf("ecr")==0){
//					pi.setIsUserEditable(temp.getAvailableECR());
//				}
//				pi.setOnDashboard(temp.getOnDashboard());
//				pi = create(pi);
//
//				RevisionInformationSearch pis = new RevisionInformationSearch();
//				pis.setCreated_date(ts);
//				pis.setId(Integer.toString(pi.getId()));
//				pis.setOrderNum(pi.getOrderNum());
//				pis.setLast_updated_date(ts);
//				pis.setName(pi.getName().toLowerCase().trim());
//				pis.setOnDashboard(temp.getOnDashboard());
//				pis.setPhase(phase.toLowerCase().trim());
//				pis.setValue("");
//				pis.setIsUserEditable(pi.getIsUserEditable());
//				pis.setIsRestrictedView(pi.getIsRestrictedView());
//				pis.setRevision(rev.getId());
//				riSearchServ.save(pis);
//			}
//		}
//	}

//	@Override
//	public void cloneInformation(Revision oldRev, Revision rev, String username){
//		Timestamp ts = new Timestamp(new Date().getTime());
//		List<RevisionInformation> infos = findByRevision(oldRev);
//		if(infos != null && !infos.isEmpty()){
//			for (RevisionInformation ct : infos){
//				RevisionInformation pi = new RevisionInformation();
//				pi.setCreatedBy(username);
//				pi.setOrderNum(ct.getOrderNum());
//				pi.setCreatedDate(ts);
//				pi.setLastUpdatedDate(ts);
//				pi.setIsRestrictedView(ct.getIsRestrictedView());
//				pi.setName(ct.getName());
//				pi.setRevision(rev);
//				pi.setValue(ct.getValue());
//				pi.setPhase(ct.getPhase());
//				pi.setIsUserEditable(ct.getIsUserEditable());
//				pi.setOnDashboard(ct.getOnDashboard());
//				pi = create(pi);
//
//				RevisionInformationSearch pis = new RevisionInformationSearch();
//				pis.setCreated_date(ts);
//				pis.setId(Integer.toString(pi.getId()));
//				pis.setOrderNum(pi.getOrderNum());
//				pis.setLast_updated_date(ts);
//				pis.setName(pi.getName().toLowerCase().trim());
//				pis.setOnDashboard(ct.getOnDashboard());
//				pis.setPhase(ct.getPhase().toLowerCase().trim());
//				pis.setValue(ct.getValue());
//				pis.setIsUserEditable(pi.getIsUserEditable());
//				pis.setIsRestrictedView(pi.getIsRestrictedView());
//				pis.setRevision(rev.getId());
//				riSearchServ.save(pis);
//			}
//		}
//	}
//	@Override
//	public HashMap parseInfoSpreadSheet(String username, File file) {
//		HashMap ret = new HashMap();
//		try {
//			FileInputStream is = new FileInputStream(file);
//			Workbook wb = WorkbookFactory.create(is);
//			final DateTime currentdt = new DateTime();
//			for (int i =0 ; i< wb.getNumberOfSheets(); i++){
//				Sheet ws = wb.getSheetAt(i);
//				Row titleRow = null;
//				String pname = wb.getSheetName(i).toLowerCase();
//				System.out.println("Sheet name is "+ws.getSheetName());
//				List<Program> ps = progServ.findByName(pname);
//				if(ps== null && pname.indexOf("+")!=-1){
//					ps = progServ.findByName(pname.replaceAll("\\+", "plus"));
//				}
//				if(ps != null){
//					Program p = ps.get(0);
//					List<Revision> revs = revServ.findByProgram(p, currentdt);
//					if(revs != null){
//						for(int ridx = 0; ridx<29; ridx++){
//							Row row = ws.getRow(ridx);
//							if(ridx ==0){
//								titleRow = row;
//							}
//							else{
//								String title = "";
//								for (int cidx = 0; cidx<titleRow.getLastCellNum(); cidx++){
//									Cell cell = row.getCell(cidx);
//									if(cell != null){
//										if(cidx ==0){
//											title = cell.getStringCellValue();
//										}
//										else{
//											String phase = titleRow.getCell(cidx).getStringCellValue();
//											String value="";
//											if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
//												if(cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC){
//													if(DateUtil.isCellDateFormatted(cell)){
//														DateTime dt = new DateTime(cell.getDateCellValue());
//														value = dt.toString(dfmt);
//													}
//													else{
//														value = Double.toString(cell.getNumericCellValue());
//													}
//												}
//												else{
//													switch(cell.getCachedFormulaResultType()){
//														case Cell.CELL_TYPE_STRING:
//															value = cell.getStringCellValue();
//															break;
//														case Cell.CELL_TYPE_BLANK:
//															value = "";
//															break;
//														case Cell.CELL_TYPE_BOOLEAN:
//															value = Boolean.toString(cell.getBooleanCellValue());
//															break;
//													}
//												}
//											}
//											else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
//												if(DateUtil.isCellDateFormatted(cell)){
//													DateTime dt = new DateTime(cell.getDateCellValue());
//													value = dt.toString(dfmt);
//												}
//												else{
//													value = Double.toString(cell.getNumericCellValue());
//												}
//											}
//											else{
//												switch(cell.getCellType()){
//													case Cell.CELL_TYPE_STRING:
//														value = cell.getStringCellValue();
//														break;
//													case Cell.CELL_TYPE_BLANK:
//														value = "";
//														break;
//													case Cell.CELL_TYPE_BOOLEAN:
//														value = Boolean.toString(cell.getBooleanCellValue());
//														break;
//												}
//											}
//											if (phase.equalsIgnoreCase("ecr")){
//												phase = "ECR1";
//											}
//											else if (phase.toLowerCase().indexOf("ca")!=-1){
//												phase = "CA";
//											}
//											if(title.toLowerCase().indexOf("bandwidth") != -1)
//												title = "Bandwidth (Gb)";
//											else if(title.toLowerCase().indexOf("max power") != -1)
//												title = "Max Power (W)";
//											else if(title.toLowerCase().indexOf("timing closure") != -1)
//												title = "Timing Closure Tool and Version";
//											else if(title.toLowerCase().indexOf("process") != -1)
//												title = "Process Node";
//											else if(title.toLowerCase().indexOf("effort pre") != -1)
//												title = "Effort pre-TO (MM)";
//											else if(title.toLowerCase().indexOf("effort post") != -1)
//												title = "Effort post-TO (MM)";
//											else if(title.toLowerCase().indexOf("cell count") != -1)
//												title = "Cell count";
//
//
//											for(Revision rev : revs){
//												List<RevisionInformation> ril = findByRevisionPhaseName(rev, phase, title);
//												if(ril != null){
//													for (RevisionInformation ri : ril){
//														if(ri.getIsUserEditable()){
//															ri.setValue(value);
//															ri.setLastUpdatedDate(new Timestamp(currentdt.getMillis()));
//															update(ri);
//														}
//													}
//												}
//												List<RevisionInformationSearch> risl = riSearchServ.findByRevisionPhaseName(rev.getId(), phase.toLowerCase(), title.toLowerCase());
//												if(risl != null){
//													for(RevisionInformationSearch ris : risl){
//														if(ris.getIsUserEditable()){
//															ris.setValue(value.toLowerCase());
//															ris.setLast_updated_date(new Timestamp(currentdt.getMillis()));
//															riSearchServ.save(ris);
//														}
//													}
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//
//			}
//		} catch (FileNotFoundException e) {
//			ret.put("err", "Upload File Fail");
//			e.printStackTrace();
//		} catch (InvalidFormatException e) {
//			ret.put("err", "Invalid Format");
//			e.printStackTrace();
//		} catch (IOException e) {
//			ret.put("err", "IO Failed");
//			e.printStackTrace();
//		}
//		return ret;
//	}
}