package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.ProgramService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
public class ProgramSaveEventHandler implements ApplicationListener<ProgramSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionSearchService revisionSearchService;
    private final ProgramService programService;

    @Autowired
    public ProgramSaveEventHandler(RedisCacheRepository redis, RevisionSearchService revisionSearchService, ProgramService programService) {
        this.redis = redis;
        this.revisionSearchService = revisionSearchService;
        this.programService = programService;
    }

    @Override
    public void onApplicationEvent(ProgramSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            Program program = (Program) map.get("data");
            Set<Segment> segmentSet = program.getSegments();
            if (segmentSet != null && !segmentSet.isEmpty()) {
                Segment segment = segmentSet.iterator().next();
                List<RevisionSearch> revisionSearchList = revisionSearchService.findByProgram(program.getId());
                if (revisionSearchList != null && !revisionSearchList.isEmpty()) {
                    for (RevisionSearch revisionSearch : revisionSearchList) {
                        revisionSearch.setProgram_name(program.getName().toLowerCase().trim());
                        revisionSearch.setBase_num(program.getBaseNum().toLowerCase().trim());
                        revisionSearch.setProgram_id(program.getId());
                        revisionSearch.setSegment(segment.getName().toLowerCase().trim());
                        revisionSearch.setProgram_type(program.getType().toString().toLowerCase().trim());
                        revisionSearchService.saveOrUpdate(revisionSearch);
                    }
                }
            }
            redis.clearCache(program.getId(), "", "program");
        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    Program program = programService.findById(id);
                    if (program != null) {
                        redis.clearCache(id, "", "program");
                        revisionSearchService.delete(Integer.toString(id));
                    }
                }
            } catch (Exception e) {

            }
        }

    }
}
