package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class HeadlineSaveEvent extends ApplicationEvent {
    public HeadlineSaveEvent(Object source) {
        super(source);
    }
}

