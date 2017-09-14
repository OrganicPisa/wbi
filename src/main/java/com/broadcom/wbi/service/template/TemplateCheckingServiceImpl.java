package com.broadcom.wbi.service.template;

import com.broadcom.wbi.model.elasticSearch.RevisionContactSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionInformationSearch;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.elasticSearch.TemplateSearch;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionContact;
import com.broadcom.wbi.model.mysql.RevisionInformation;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.jpa.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TemplateCheckingServiceImpl implements TemplateCheckingService {
    @Autowired
    private TemplateSearchService templateSearchService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private RevisionService revisionService;
    @Autowired
    private RevisionInformationService revisionInformationService;
    @Autowired
    private RevisionInformationSearchService revisionInformationSearchService;

    @Autowired
    private RevisionContactService revisionContactService;
    @Autowired
    private RevisionContactSearchService revisionContactSearchService;
    @Autowired
    private IndicatorGroupSearchService indicatorGroupSearchService;
    @Autowired
    private IndicatorTaskSearchService indicatorTaskSearchService;
    @Autowired
    private IndicatorDateSearchService indicatorDateSearchService;
    @Autowired
    private IGroupService iGroupService;
    @Autowired
    private IGroupHistoryService iGroupHistoryService;
    @Autowired
    private ITaskService iTaskService;
    @Autowired
    private ITaskHistoryService iTaskHistoryService;
    @Autowired
    private IDateService iDateService;
    @Autowired
    private IDateHistoryService iDateHistoryService;

    @Override
    public void checkInformationTemplate(RevisionSearch rs) {
        List<TemplateSearch> templateSearchList = templateSearchService.findByTypeCategory(rs.getProgram_type().toLowerCase(), "information", "");

        final Set checkDuplicate = Collections.synchronizedSet(new HashSet());
        if (templateSearchList != null && !templateSearchList.isEmpty()) {
            Revision rev = revisionService.findById(Integer.parseInt(rs.getId()));
            for (TemplateSearch templateSearch : templateSearchList) {
                List<RevisionInformationSearch> revisionInformationSearchList = revisionInformationSearchService.findByRevisionPhaseName(Integer.parseInt(rs.getId()), "current", templateSearch.getName().toLowerCase().trim());
//                if(revisionInformationSearchList != null)
//                    System.out.println("Found in db "+ rs.getProgram_name() + templateSearch.getName());
                if (revisionInformationSearchList == null || revisionInformationSearchList.isEmpty())
                    System.out.println("Missing " + templateSearch.getName());


//                String key = rs.getId() + "_" + templateSearch.getName().toLowerCase() + "_current";
//                if (checkDuplicate.contains(key))
//                    return;
//                System.out.println("Processing " + key);
//                checkDuplicate.add(key);
//                RevisionInformation pi = new RevisionInformation();
//                pi.setCreatedBy("vinhtran");
//                pi.setOrderNum(templateSearch.getOrderNum());
//                pi.setIsRestrictedView(templateSearch.getIsRestrictedView());
//                pi.setName(templateSearch.getName());
//                pi.setRevision(rev);
//                pi.setValue("");
//                pi.setPhase("current");
//                pi.setIsUserEditable(templateSearch.getAvailableCurrent());
//                pi.setOnDashboard(templateSearch.getOnDashboard());
//                revisionInformationService.saveOrUpdate(pi);
            }
        }

    }

    @Override
    public void checkInformationTemplate(RevisionSearch rs, String type, String title, String value, int orderNum) {
        if (type.equalsIgnoreCase("contact")) {
            List<RevisionContactSearch> revisionContactSearchList = revisionContactSearchService.findByRevision(Integer.parseInt(rs.getId()), title.toLowerCase().trim());
            if (revisionContactSearchList != null && !revisionContactSearchList.isEmpty()) {
                for (RevisionContactSearch revisionContactSearch : revisionContactSearchList) {
                    if (revisionContactSearch.getName().equalsIgnoreCase(title)) {
                        revisionContactSearch.setValue(value.toLowerCase().trim());
                        revisionContactSearchService.saveOrUpdate(revisionContactSearch);

                        RevisionContact revisionContact = revisionContactService.findById(Integer.parseInt(revisionContactSearch.getId()));
                        revisionContact.setValue(value.toLowerCase().trim());
                        revisionContactService.saveOrUpdate(revisionContact);
                    }
                }
            } else {
                Revision revision = revisionService.findById(Integer.parseInt(rs.getId()));
                RevisionContact contact = new RevisionContact();
                contact.setValue(value.toLowerCase().trim());
                contact.setName(title.toLowerCase().trim());
                contact.setRevision(revision);
                contact.setOnDashboard(true);
                contact = revisionContactService.saveOrUpdate(contact);

                RevisionContactSearch contactSearch = new RevisionContactSearch();
                contactSearch.setValue(contact.getValue());
                contactSearch.setLast_updated_date(contact.getLastUpdatedDate());
                contactSearch.setCreated_date(contact.getCreatedDate());
                contactSearch.setName(contact.getName());
                contactSearch.setOnDashboard(true);
                contactSearch.setRevision(revision.getId());
                contactSearch.setId(Integer.toString(contact.getId()));
                revisionContactSearchService.saveOrUpdate(contactSearch);
                System.out.println("Missing " + rs.getProgram_name() + " " + title);
            }
        } else {
            Revision revision = revisionService.findById(Integer.parseInt(rs.getId()));
            if (title.equalsIgnoreCase("switch"))
                title = "switch chip";
            else if (title.equalsIgnoreCase("hsip/phy"))
                title = "hsip/phy chip";
            else if (title.equalsIgnoreCase("netlogic"))
                title = "netlogic chip";
            else if (title.equalsIgnoreCase("others"))
                title = "others chip";
            List<RevisionInformationSearch> revisionInformationSearchList = revisionInformationSearchService.findByRevisionPhaseName(Integer.parseInt(rs.getId()), "current", title.toLowerCase().trim());
            if (revisionInformationSearchList != null && !revisionInformationSearchList.isEmpty()) {
                for (RevisionInformationSearch revisionInformationSearch : revisionInformationSearchList) {
                    if (revisionInformationSearch.getName().equalsIgnoreCase(title)) {
                        revisionInformationSearch.setValue(value.toLowerCase().trim());
                        revisionInformationSearchService.saveOrUpdate(revisionInformationSearch);

                        RevisionInformation revisionInformation = revisionInformationService.findById(Integer.parseInt(revisionInformationSearch.getId()));
                        revisionInformation.setValue(value.toLowerCase().trim());
                        revisionInformationService.saveOrUpdate(revisionInformation);
                    }
                }
            } else {
                System.out.println("Missing " + rs.getProgram_name() + "  " + title);
                RevisionInformation revisionInformation = new RevisionInformation();
                revisionInformation.setValue(value.toLowerCase().trim());
                revisionInformation.setIsRestrictedView(false);
                revisionInformation.setIsUserEditable(true);
                revisionInformation.setName(title.toLowerCase().trim());
                revisionInformation.setOnDashboard(false);
                revisionInformation.setOrderNum(orderNum);
                revisionInformation.setPhase("current");
                revisionInformation.setRevision(revision);
                revisionInformation.setName(title.toLowerCase().trim());
                revisionInformation = revisionInformationService.saveOrUpdate(revisionInformation);


                RevisionInformationSearch revisionInformationSearch = new RevisionInformationSearch();
                revisionInformationSearch.setLast_updated_date(revisionInformation.getLastUpdatedDate());
                revisionInformationSearch.setValue(revisionInformation.getValue());
                revisionInformationSearch.setCreated_date(revisionInformation.getCreatedDate());
                revisionInformationSearch.setName(revisionInformation.getName());
                revisionInformationSearch.setId(Integer.toString(revisionInformation.getId()));
                revisionInformationSearch.setIsRestrictedView(revisionInformation.getIsRestrictedView());
                revisionInformationSearch.setIsUserEditable(revisionInformationSearch.getIsUserEditable());
                revisionInformationSearch.setOnDashboard(revisionInformation.getOnDashboard());
                revisionInformationSearch.setOrderNum(revisionInformation.getOrderNum());
                revisionInformationSearch.setPhase("current");
                revisionInformationSearch.setRevision(revision.getId());

                revisionInformationSearchService.saveOrUpdate(revisionInformationSearch);
            }
        }
    }

    @Override
    public void checkIndicatorTemplate(RevisionSearch rs) {

    }
}
