package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.SkuSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.Sku;
import com.broadcom.wbi.service.elasticSearch.SkuSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.service.jpa.RevisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class SkuSaveEventHandler implements ApplicationListener<SkuSaveEvent> {
    private final RedisCacheRepository redis;
    private final SkuSearchService skuSearchService;
    private final RevisionService revisionService;


    @Autowired
    public SkuSaveEventHandler(RedisCacheRepository redis, SkuSearchService skuSearchService, RevisionService revisionService) {
        this.redis = redis;
        this.revisionService = revisionService;
        this.skuSearchService = skuSearchService;
    }

    @Override
    public void onApplicationEvent(SkuSaveEvent event) {
        HashMap map = (HashMap) event.getSource();
        String actionType = map.get("action").toString();
        if (actionType.equalsIgnoreCase("save")) {
            Sku sku = (Sku) map.get("data");
            SkuSearch ss = skuSearchService.findById(Integer.toString(sku.getId()));
            if (ss == null) {
                ss = new SkuSearch();
                ss.setId(Integer.toString(sku.getId()));
            }

            Program program = sku.getProgram();
            List<Revision> revisionList = revisionService.findByProgram(program, null);
            Revision rev = null;
            if (revisionList != null && !revisionList.isEmpty())
                rev = revisionList.get(0);
            if (rev != null) {
                ss.setUrl("/program/" + program.getType().toString().toLowerCase() + "/" + program.getId() + "/" + rev.getId() + "/dashboard");
                ss.setBaseNum(program.getBaseNum());
                ss.setProgramDisplayName(program.getDisplayName());
                ss.setProgramType(program.getType().toString());
                ss.setProgram(program.getId());
                ss.setAka(sku.getAka());
                ss.setSkuNum(sku.getSkuNum());
                ss.setFrequency(sku.getFrequency());
                ss.setDescription(sku.getDescription());
                ss.setIoCapacity(sku.getIoCapacity());
                ss.setDateAvailable(sku.getDateAvailable());
                ss.setNumOfSerdes(sku.getNumOfSerdes());
                ss.setPortConfig(sku.getPortConfig());
                ss.setItemp(sku.getItemp());
                skuSearchService.saveOrUpdate(ss);
            }

        } else if (actionType.equalsIgnoreCase("delete")) {
            try {
                Integer id = (Integer) map.get("data");
                if (id > 0) {
                    skuSearchService.delete(Integer.toString(id));
                }
            } catch (Exception e) {

            }
        }

    }
}
