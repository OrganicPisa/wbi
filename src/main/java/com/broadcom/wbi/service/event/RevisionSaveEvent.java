package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class RevisionSaveEvent extends ApplicationEvent {
    public RevisionSaveEvent(Object source) {
        super(source);
    }
}

