package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.ResourcePlanSearch;
import com.broadcom.wbi.model.mysql.ResourcePlan;
import com.broadcom.wbi.service.elasticSearch.ResourcePlanSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ResourcePlanSaveEventHandler implements ApplicationListener<ResourcePlanSaveEvent> {
    private final RedisCacheRepository redis;
    private final ResourcePlanSearchService resourcePlanSearchService;


    @Autowired
    public ResourcePlanSaveEventHandler(RedisCacheRepository redis,
                                        ResourcePlanSearchService resourcePlanSearchService) {
        this.redis = redis;
        this.resourcePlanSearchService = resourcePlanSearchService;
    }

    @Override
    public void onApplicationEvent(ResourcePlanSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            ResourcePlan resourcePlan = (ResourcePlan) map.get("data");
            ResourcePlanSearch resourcePlanSearch = resourcePlanSearchService.findById(Integer.toString(resourcePlan.getId()));
            if (resourcePlanSearch == null) {
                resourcePlanSearch = new ResourcePlanSearch();
                resourcePlanSearch.setId(Integer.toString(resourcePlan.getId()));
            }
            resourcePlanSearch.setCount(resourcePlan.getCount());
            resourcePlanSearch.setInclude_contractor(resourcePlan.getInclude_contractor());
            resourcePlanSearch.setMonth(resourcePlan.getMonth());
            resourcePlanSearch.setPlan_type(resourcePlan.getType().toLowerCase().trim());
            resourcePlanSearch.setProgram(resourcePlan.getProgram().getId());
            resourcePlanSearch.setSkill(resourcePlan.getPlan_skill().toLowerCase().trim());
            resourcePlanSearchService.saveOrUpdate(resourcePlanSearch);

        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    resourcePlanSearchService.delete(Integer.toString(id));
                }
            } catch (Exception e) {

            }
        }

    }
}
