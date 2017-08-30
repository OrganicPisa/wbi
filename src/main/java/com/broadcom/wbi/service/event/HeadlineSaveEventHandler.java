package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class HeadlineSaveEventHandler implements ApplicationListener<HeadlineSaveEvent> {
    @Autowired
    private RedisCacheRepository redis;
    @Autowired
    private RevisionSearchService revisionSearchService;

    @Override
    public void onApplicationEvent(HeadlineSaveEvent headlineSaveEvent) {
        Integer rid = (Integer) headlineSaveEvent.getSource();
        RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
        if (rs != null) {
            redis.clearCache(rs.getProgram_id(), "", "program");
        }
    }
}
