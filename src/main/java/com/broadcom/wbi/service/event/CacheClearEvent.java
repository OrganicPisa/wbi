package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class CacheClearEvent extends ApplicationEvent {
    public CacheClearEvent(Object source) {
        super(source);
    }
}

