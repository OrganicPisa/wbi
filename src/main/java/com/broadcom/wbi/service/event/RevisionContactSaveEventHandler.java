package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.RevisionContactSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionContact;
import com.broadcom.wbi.service.elasticSearch.RevisionContactSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class RevisionContactSaveEventHandler implements ApplicationListener<RevisionContactSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionContactSearchService revisionContactSearchService;


    @Autowired
    public RevisionContactSaveEventHandler(RedisCacheRepository redis,
                                           RevisionContactSearchService revisionContactSearchService) {
        this.redis = redis;
        this.revisionContactSearchService = revisionContactSearchService;
    }

    @Override
    public void onApplicationEvent(RevisionContactSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            RevisionContact contact = (RevisionContact) map.get("data");
            Revision revision = contact.getRevision();
            Program program = revision.getProgram();
            RevisionContactSearch revisionContactSearch = revisionContactSearchService.findById(Integer.toString(contact.getId()));
            if (revisionContactSearch == null) {
                revisionContactSearch = new RevisionContactSearch();
                revisionContactSearch.setId(Integer.toString(contact.getId()));
            }
            revisionContactSearch.setName(contact.getName().toLowerCase().trim());
            revisionContactSearch.setOnDashboard(true);
            revisionContactSearch.setRevision(revision.getId());
            revisionContactSearch.setCreated_date(contact.getCreatedDate());
            revisionContactSearch.setLast_updated_date(contact.getLastUpdatedDate());
            revisionContactSearch.setValue(contact.getValue().toString().toLowerCase().trim());
            revisionContactSearchService.saveOrUpdate(revisionContactSearch);
            if (contact.getOnDashboard()) {
                redis.clearCache(program.getId(), "", "program");
            }

        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    revisionContactSearchService.delete(Integer.toString(id));
                }
            } catch (Exception e) {

            }
        }

    }
}
