package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class CacheClearEventHandler implements ApplicationListener<CacheClearEvent> {
    private final RedisCacheRepository redis;
    private final RevisionSearchService revisionSearchService;

    @Autowired
    public CacheClearEventHandler(RedisCacheRepository redis, RevisionSearchService revisionSearchService) {
        this.redis = redis;
        this.revisionSearchService = revisionSearchService;
    }

    @Override
    public void onApplicationEvent(CacheClearEvent cacheClearEvent) {
        Integer rid = (Integer) cacheClearEvent.getSource();
        RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
        if (rs != null) {
            redis.clearCache(rs.getProgram_id(), "", "program");
        }
    }
}
