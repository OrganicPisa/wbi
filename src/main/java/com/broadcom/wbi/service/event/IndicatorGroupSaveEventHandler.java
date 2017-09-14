package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.IndicatorGroupSearch;
import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.IGroupHistory;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.service.elasticSearch.IndicatorGroupSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class IndicatorGroupSaveEventHandler implements ApplicationListener<IndicatorGroupSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionSearchService revisionSearchService;
    private final IndicatorGroupSearchService indicatorGroupSearchService;

    @Autowired
    public IndicatorGroupSaveEventHandler(RedisCacheRepository redis, RevisionSearchService revisionSearchService, IndicatorGroupSearchService indicatorGroupSearchService) {
        this.redis = redis;
        this.revisionSearchService = revisionSearchService;
        this.indicatorGroupSearchService = indicatorGroupSearchService;
    }

    @Override
    public void onApplicationEvent(IndicatorGroupSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            IGroupHistory iGroupHistory = (IGroupHistory) map.get("data");
            IGroup iGroup = iGroupHistory.getIGroup();
            Revision rev = iGroup.getRevision();
            Program program = rev.getProgram();
            IndicatorGroupSearch igs = indicatorGroupSearchService.findById(Integer.toString(iGroupHistory.getId()));
            if (igs == null) {
                igs = new IndicatorGroupSearch();
                igs.setId(Integer.toString(iGroupHistory.getId()));
            }
            igs.setIgroup_id(iGroup.getId());
            igs.setIgroup_name(iGroup.getName().toLowerCase().trim());
            igs.setLast_updated_date(iGroupHistory.getLastUpdatedDate());
            igs.setOrder_num(iGroup.getOrderNum());
            igs.setRemark(iGroupHistory.getRemark());
            igs.setStatus(iGroupHistory.getStatus().toString().toLowerCase());
            igs.setRevision_id(rev.getId());
            igs.setRevision_name(rev.getName().toLowerCase());
            indicatorGroupSearchService.saveOrUpdate(igs);
        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    indicatorGroupSearchService.delete(Integer.toString(id));
                }
            } catch (Exception e) {

            }
        } else if (actionType.equalsIgnoreCase("parent")) {
            IGroup iGroup = (IGroup) map.get("data");
            List<IndicatorGroupSearch> indicatorGroupSearchList = indicatorGroupSearchService.findAllByGroupId(iGroup.getId());
            if (indicatorGroupSearchList != null && !indicatorGroupSearchList.isEmpty()) {
                for (IndicatorGroupSearch indicatorGroupSearch : indicatorGroupSearchList) {
                    indicatorGroupSearch.setIgroup_name(iGroup.getName().toLowerCase().trim());
                    indicatorGroupSearch.setOrder_num(iGroup.getOrderNum());
                    indicatorGroupSearchService.saveOrUpdate(indicatorGroupSearch);
                }
            }
        }
    }
}
