package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.IndicatorDateSearch;
import com.broadcom.wbi.model.mysql.IDate;
import com.broadcom.wbi.model.mysql.IDateHistory;
import com.broadcom.wbi.model.mysql.IGroup;
import com.broadcom.wbi.model.mysql.ITask;
import com.broadcom.wbi.service.elasticSearch.IndicatorDateSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class IndicatorDateSaveEventHandler implements ApplicationListener<IndicatorDateSaveEvent> {
    private final RedisCacheRepository redis;
    private final RevisionSearchService revisionSearchService;
    private final IndicatorDateSearchService indicatorDateSearchService;

    @Autowired
    public IndicatorDateSaveEventHandler(RedisCacheRepository redis, RevisionSearchService revisionSearchService, IndicatorDateSearchService indicatorDateSearchService) {
        this.redis = redis;
        this.revisionSearchService = revisionSearchService;
        this.indicatorDateSearchService = indicatorDateSearchService;
    }

    @Override
    public void onApplicationEvent(IndicatorDateSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            IDateHistory iDateHistory = (IDateHistory) map.get("data");
            IDate iDate = iDateHistory.getIDate();
            ITask iTask = iDate.getITask();
            IGroup iGroup = iTask.getIGroup();
            IndicatorDateSearch ids = indicatorDateSearchService.findById(Integer.toString(iDateHistory.getId()));
            if (ids == null) {
                ids = new IndicatorDateSearch();
                ids.setId(Integer.toString(iDateHistory.getId()));
            }
            ids.setDate_id(iDate.getId());
            ids.setComment(iDateHistory.getComment());
            ids.setDate_name(iDate.getTtype().toString().toLowerCase() + "_" + iDate.getEtype().toString().toLowerCase());
            ids.setGroup_id(iGroup.getId());
            ids.setGroup_name(iGroup.getName().toLowerCase().trim());
            ids.setLast_updated_date(iDateHistory.getLastUpdatedDate());
            ids.setStatus(iDateHistory.getStatus().toString().toLowerCase().trim());
            ids.setTask_id(iTask.getId());
            ids.setTask_name(iTask.getName().toLowerCase().trim());
            ids.setValue(iDateHistory.getValue());
            indicatorDateSearchService.saveOrUpdate(ids);
        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    indicatorDateSearchService.delete(Integer.toString(id));
                }
            } catch (Exception e) {

            }
        }
    }
}
