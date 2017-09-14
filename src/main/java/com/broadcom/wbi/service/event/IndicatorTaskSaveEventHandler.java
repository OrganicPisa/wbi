package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.IndicatorTaskSearch;
import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.service.elasticSearch.IndicatorTaskSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class IndicatorTaskSaveEventHandler implements ApplicationListener<IndicatorTaskSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionSearchService revisionSearchService;
    private final IndicatorTaskSearchService indicatorTaskSearchService;

    @Autowired
    public IndicatorTaskSaveEventHandler(RedisCacheRepository redis, RevisionSearchService revisionSearchService, IndicatorTaskSearchService indicatorTaskSearchService) {
        this.redis = redis;
        this.revisionSearchService = revisionSearchService;
        this.indicatorTaskSearchService = indicatorTaskSearchService;
    }

    @Override
    public void onApplicationEvent(IndicatorTaskSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            ITaskHistory iTaskHistory = (ITaskHistory) map.get("data");

            ITask iTask = iTaskHistory.getITask();
            IGroup iGroup = iTask.getIGroup();

            Revision rev = iGroup.getRevision();
            Program program = rev.getProgram();
            IndicatorTaskSearch its = indicatorTaskSearchService.findById(Integer.toString(iTaskHistory.getId()));
            if (its == null) {
                its = new IndicatorTaskSearch();
                its.setId(Integer.toString(iTaskHistory.getId()));
            }
            its.setIgroup_id(iGroup.getId());
            its.setIgroup_name(iGroup.getName().toString().toLowerCase());
            its.setLast_updated_date(iTaskHistory.getLastUpdatedDate());
            its.setNote(iTaskHistory.getNote());
            its.setOrder_num(iTask.getOrderNum());
            its.setRevision_id(rev.getId());
            its.setRevision_name(rev.getName().toLowerCase());
            its.setStatus(iTaskHistory.getStatus().toString().toLowerCase().trim());
            its.setTask_id(iTask.getId());
            its.setTask_name(iTask.getName().toLowerCase().trim());
            its.setTask_name_in_report(iTask.getNameInReport().toLowerCase().trim());
            indicatorTaskSearchService.saveOrUpdate(its);
        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    indicatorTaskSearchService.delete(Integer.toString(id));
                }
            } catch (Exception e) {

            }
        }
    }
}
