package com.broadcom.wbi.service.resource.parser.implementation;

import com.broadcom.wbi.model.elasticSearch.ResourceActualSearch;
import com.broadcom.wbi.model.mssql.ResourceReader;
import com.broadcom.wbi.service.elasticSearch.ResourceActualSearchService;
import com.broadcom.wbi.service.resource.parser.DataResourceParseService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataResourceParseServiceImpl implements DataResourceParseService {

    private final ResourceActualSearchService resourceActualSearchService;
    private final ElasticsearchTemplate template;
    @PersistenceContext(unitName = "mssqlPU")
    EntityManager em;

    @Autowired
    public DataResourceParseServiceImpl(ResourceActualSearchService resourceActualSearchService, ElasticsearchTemplate template) {
        this.resourceActualSearchService = resourceActualSearchService;
        this.template = template;
    }

    @Override
    public void doCollectAndInsertData(DateTime dt) {
        if (!template.indexExists(ResourceActualSearch.class)) {
            template.createIndex(ResourceActualSearch.class);
            template.putMapping(ResourceActualSearch.class);
        }
        List<ResourceReader> readers = collectData(dt.toString(dtfm), dt.toString(dtfm));
        if (readers == null || readers.isEmpty())
            return;

        List<ResourceActualSearch> rsl = new ArrayList<>();
        rsloop:
        for (ResourceReader rs : readers) {
            if (rs.getId() == null || rs.getManager() == null || rs.getEmployee() == null) {
                continue rsloop;
            }
            int approved_skill = 0;
            int approved_project = 0;
            if (rs.getId().getProject().toLowerCase().startsWith("nws ")) {
                if (approveProjectTableMap.containsKey(rs.getId().getProject().toUpperCase())) {
                    approved_project = 1;
                }
            } else {
                approved_project = 1;
            }
            if (approveSkillTableMap.containsKey(rs.getId().getSkill().toUpperCase())) {
                approved_skill = 1;
            }
            String dsc = "OTHERS";
            if (rs.getLoc_country() != null && designCenterTableMap.containsKey(rs.getLoc_country().toUpperCase())) {
                dsc = designCenterTableMap.get(rs.getLoc_country().toUpperCase());
            }

            String skill = rs.getId().getSkill().replaceAll("\\+", "plus").replaceAll("\"", "").trim();
            if (skill.toLowerCase().contains("device verifi"))
                skill = "DVT";
            else if (skill.equalsIgnoreCase("management"))
                skill = "MGMT";

            ResourceActualSearch r = new ResourceActualSearch();

            r.setEmployee(rs.getEmployee().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase().trim());
            r.setEmployee_name(rs.getEmployee().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase().trim());
            r.setApproved_project(approved_project);
            r.setApproved_skill(approved_skill);
            if (rs.getCategory() != null)
                r.setCategory(rs.getCategory().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase().trim());
            else
                r.setCategory("");
            if (rs.getId().getCharged_from() != null)
                r.setCharged_from(rs.getId().getCharged_from().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase().trim());
            else
                r.setCharged_from("");
            if (rs.getId().getCharged_to() != null)
                r.setCharged_to(rs.getId().getCharged_to().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase().trim());
            else
                r.setCharged_to("");
            if (rs.getCost_center() != null)
                r.setCost_center(rs.getCost_center().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase().trim());
            else
                r.setCost_center("");
            r.setCount(rs.getId().getCount());
            r.setDesign_center(dsc.toUpperCase());
            r.setEmployee_id(rs.getEmployee_id());
            if (rs.getPerson_status() != null)
                r.setEmployee_status(rs.getPerson_status().toLowerCase());
            else
                r.setEmployee_status("I");
            if (rs.getEmployee_type() != null)
                r.setEmployee_type(rs.getEmployee_type().replaceAll("\\+", "plus").replaceAll("\"", "").toUpperCase());
            else
                r.setEmployee_type("");
            if (rs.getProfit_center_group() != null)
                r.setProfit_center_group(rs.getProfit_center_group().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            else
                r.setProfit_center_group("");

            if (rs.getProfit_center_project() != null)
                r.setProfit_center_project(rs.getProfit_center_project().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            else
                r.setProfit_center_project("");
            if (rs.getLoc_country() != null)
                r.setLoc_country(rs.getLoc_country().replaceAll("\\+", "plus").replaceAll("\"", "").toUpperCase());
            else
                r.setLoc_country("");
            if (rs.getLoc_name() != null)
                r.setLoc_name(rs.getLoc_name().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            else
                r.setLoc_name("");

            r.setManager(rs.getManager().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            r.setManager_name(rs.getManager().toLowerCase());
            r.setManager_id(rs.getManager_id());
            r.setMonth(rs.getDate());
            if (rs.getId().getProject() != null) {
                r.setProject(rs.getId().getProject().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
                r.setProject_name(rs.getId().getProject().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            } else {
                r.setProject("");
                r.setProject_name("");
            }
            if (rs.getProject_type() != null)
                r.setProject_type(rs.getProject_type().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            else
                r.setProject_type("");
            if (rs.getSegment_name() != null)
                r.setSegment_name(rs.getSegment_name().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            else
                r.setSegment_name("");
            r.setSkill(skill.toLowerCase());
            r.setSkill_name(skill.toLowerCase());
            if (rs.getSub_category() != null)
                r.setSub_category(rs.getSub_category().replaceAll("\\+", "plus").replaceAll("\"", "").toLowerCase());
            else
                r.setSub_category("");
            rsl.add(r);
        }
        if (rsl.size() > 0) {
            resourceActualSearchService.saveBulk(rsl);
        }

        System.out.println("Finish insert " + dt.toString());
    }

    private List<ResourceReader> collectData(String startDate, String endDate) {
        System.out.println("Starting to collect from MSSQL " + startDate);
        StoredProcedureQuery query = em.createNamedStoredProcedureQuery("generateReport");
        query.setParameter("bu", "%");
        query.setParameter("lob_project", "%");
        query.setParameter("lob_employee", "%");
        query.setParameter("project", "%");
        query.setParameter("parent_project", "%");
        query.setParameter("ace_hierarchy", "000000");
        query.setParameter("begin_date", startDate);
        query.setParameter("end_date", endDate);
        query.setParameter("forecast_type", "%");
        query.setParameter("file_path", "%");
        List<ResourceReader> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result;
        }
        return null;
    }

    @Override
    public void cleanup(DateTime dt) {
        if (dt == null)
            dt = new DateTime();
        DateTime fromdt = new DateTime(dt).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        DateTime todt = fromdt.dayOfMonth().withMaximumValue().hourOfDay().withMaximumValue();
        System.out.println("Cleaning ...." + fromdt.toString());
        Query query = new Query();
        query.addCriteria(Criteria.where("month").gte(fromdt).lte(todt));
        if (template.indexExists(ResourceActualSearch.class)) {
            resourceActualSearchService.deleteByTime(fromdt);
        } else {
            template.createIndex(ResourceActualSearch.class);
            template.putMapping(ResourceActualSearch.class);
        }
        template.refresh(ResourceActualSearch.class);
    }


}
