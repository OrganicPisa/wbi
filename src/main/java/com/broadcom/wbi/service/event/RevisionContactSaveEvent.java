package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class RevisionContactSaveEvent extends ApplicationEvent {
    public RevisionContactSaveEvent(Object source) {
        super(source);
    }
}

