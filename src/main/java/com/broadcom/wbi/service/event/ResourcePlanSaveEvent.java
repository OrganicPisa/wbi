package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class ResourcePlanSaveEvent extends ApplicationEvent {
    public ResourcePlanSaveEvent(Object source) {
        super(source);
    }
}

