package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionIP;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.service.jpa.RevisionIPService;
import com.broadcom.wbi.service.jpa.RevisionService;
import com.broadcom.wbi.util.ProjectConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
public class RevisionSaveEventHandler implements ApplicationListener<RevisionSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionSearchService revisionSearchService;
    private final RevisionIPService revisionIPService;
    private final RevisionService revisionService;

    @Autowired
    public RevisionSaveEventHandler(RedisCacheRepository redis, RevisionSearchService revisionSearchService, RevisionIPService revisionIPService, RevisionService revisionService) {
        this.redis = redis;
        this.revisionSearchService = revisionSearchService;
        this.revisionIPService = revisionIPService;
        this.revisionService = revisionService;
    }

    @Override
    public void onApplicationEvent(RevisionSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            Revision revision = (Revision) map.get("data");
            Program program = revision.getProgram();
            Set<Segment> segmentSet = program.getSegments();
            Segment segment = segmentSet.iterator().next();

            RevisionSearch revisionSearch = revisionSearchService.findById(Integer.toString(revision.getId()));
            if (revisionSearch == null) {
                revisionSearch = new RevisionSearch();
                revisionSearch.setId(Integer.toString(revision.getId()));
                revisionSearch.setOutlook("");
            }
            revisionSearch.setProgram_name(program.getName().toLowerCase().trim());
            revisionSearch.setProgram_order_num(program.getOrderNum());
            revisionSearch.setBase_num(program.getBaseNum().toLowerCase().trim());
            revisionSearch.setProgram_id(program.getId());
            revisionSearch.setSegment(segment.getName().toLowerCase().trim());
            revisionSearch.setProgram_type(program.getType().toString().toLowerCase().trim());

            revisionSearch.setInclude_in_report(revision.getIsRevisionIncludeInReport());
            revisionSearch.setIp_related(revision.getIpRelated());
            revisionSearch.setIs_active(Boolean.valueOf(true));
            if (!revision.getIsActive().equals(ProjectConstant.EnumProgramStatus.ACTIVE)) {
                revisionSearch.setIs_active(Boolean.valueOf(false));
            }
            revisionSearch.setIs_protected(revision.getIsProtected());
            revisionSearch.setLast_updated_outlook_date(revision.getLastUpdatedDate());
            revisionSearch.setRev_name(revision.getName().toLowerCase());
            revisionSearch.setRev_order_num(revision.getOrderNum());
            revisionSearchService.saveOrUpdate(revisionSearch);

            redis.clearCache(program.getId(), "", "program");
        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    redis.clearCache(id, "", "revision");
                    Revision revision = revisionService.findById(id);
                    if (revision != null) {
                        List<RevisionIP> revisionIPList = revisionIPService.findByEitherRevisionOrRevisionIP(revision);
                        if (revisionIPList != null && revisionIPList.isEmpty()) {
                            for (RevisionIP revisionIP : revisionIPList) {
                                revisionIPService.delete(revisionIP.getId());
                            }
                        }
                        revisionSearchService.delete(Integer.toString(id));
                    }
                }
            } catch (Exception e) {

            }
        }
    }
}
