package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class SkuSaveEvent extends ApplicationEvent {
    public SkuSaveEvent(Object source) {
        super(source);
    }
}

