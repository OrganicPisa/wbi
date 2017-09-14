package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.RevisionInformationSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionInformation;
import com.broadcom.wbi.service.elasticSearch.RevisionInformationSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class RevisionInformationSaveEventHandler implements ApplicationListener<RevisionInformationSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionInformationSearchService revisionInformationSearchService;


    @Autowired
    public RevisionInformationSaveEventHandler(RedisCacheRepository redis,
                                               RevisionInformationSearchService revisionInformationSearchService) {
        this.redis = redis;
        this.revisionInformationSearchService = revisionInformationSearchService;
    }

    @Override
    public void onApplicationEvent(RevisionInformationSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            RevisionInformation ri = (RevisionInformation) map.get("data");

            RevisionInformationSearch ris = revisionInformationSearchService.findById(Integer.toString(ri.getId()));
            Revision revision = ri.getRevision();
            Program program = revision.getProgram();
            if (ris == null) {
                ris = new RevisionInformationSearch();
                ris.setId(Integer.toString(ri.getId()));
                ris.setCreated_date(ri.getCreatedDate());
            }

            ris.setOrderNum(ri.getOrderNum());
            ris.setLast_updated_date(ri.getLastUpdatedDate());
            ris.setName(ri.getName().toLowerCase().trim());
            ris.setOnDashboard(ri.getOnDashboard());
            ris.setPhase(ri.getPhase().toLowerCase().trim());
            ris.setValue(ri.getValue().toLowerCase().trim());
            ris.setIsUserEditable(ri.getIsUserEditable());
            ris.setIsRestrictedView(ri.getIsRestrictedView());
            ris.setRevision(revision.getId());
            revisionInformationSearchService.saveOrUpdate(ris);

            if (ri.getOnDashboard()) {
                redis.clearCache(program.getId(), "", "program");
            }
        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0)
                    revisionInformationSearchService.delete(Integer.toString(id));
            } catch (Exception e) {

            }
        }
    }
}
