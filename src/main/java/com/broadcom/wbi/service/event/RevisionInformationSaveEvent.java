package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class RevisionInformationSaveEvent extends ApplicationEvent {
    public RevisionInformationSaveEvent(Object source) {
        super(source);
    }
}

