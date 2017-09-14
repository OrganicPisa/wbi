package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.HeadlineSearch;
import com.broadcom.wbi.model.mysql.Headline;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.service.elasticSearch.HeadlineSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class HeadlineSaveEventHandler implements ApplicationListener<HeadlineSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionSearchService revisionSearchService;
    private final HeadlineSearchService headlineSearchService;

    @Autowired
    public HeadlineSaveEventHandler(RedisCacheRepository redis, RevisionSearchService revisionSearchService, HeadlineSearchService headlineSearchService) {
        this.redis = redis;
        this.revisionSearchService = revisionSearchService;
        this.headlineSearchService = headlineSearchService;
    }

    @Override
    public void onApplicationEvent(HeadlineSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            Headline headline = (Headline) map.get("data");
            Revision revision = headline.getRevision();
            Program program = revision.getProgram();
            HeadlineSearch hls = headlineSearchService.findById(Integer.toString(headline.getId()));
            if (hls == null) {
                hls = new HeadlineSearch();
                hls.setId(Integer.toString(headline.getId()));
            }
            hls.setRevision_name(revision.getName().toLowerCase().trim());
            hls.setRevision_id(revision.getId());
            hls.setHeadline(headline.getHeadline());
            hls.setLast_updated_date(headline.getLastUpdatedDate());
            hls.setId(Integer.toString(headline.getId()));
            hls.setStage(headline.getStage().toString().toLowerCase());
            hls.setPrediction_flag(headline.getPrediction_flag().toString().toLowerCase());
            hls.setSchedule_flag(headline.getSchedule_flag().toString().toLowerCase());
            hls.setStatus(revision.getIsActive().toString().toLowerCase());
            hls.setBudget_flag(headline.getBudget_flag().toString().toLowerCase());
            hls.setResource_flag(headline.getResource_flag().toString().toLowerCase());
            headlineSearchService.saveOrUpdate(hls);

            redis.clearCache(program.getId(), "", "program");
        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    redis.clearCache(id, "", "program");
                    revisionSearchService.delete(Integer.toString(id));
                }
            } catch (Exception e) {

            }
        }
    }
}
