package com.broadcom.wbi.service.event;

import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CacheClearEventHandler implements ApplicationListener<CacheClearEvent> {
    @Autowired
    private RedisCacheRepository redis;

    @Override
    public void onApplicationEvent(CacheClearEvent event) {
        HashMap hm = (HashMap) event.getSource();
        Integer id = 0;
        String key = "";
        String resetType = "program";

        if (hm.containsKey("id")) {
            try {
                id = Integer.parseInt(hm.get("id").toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }
        if (id < 1) return;

        if (hm.containsKey("key"))
            key = hm.get("key").toString().toLowerCase().trim();
        if (hm.containsKey("resetType"))
            resetType = hm.get("resetType").toString().toLowerCase().trim();

        redis.clearCache(id, key, resetType);
    }
}
