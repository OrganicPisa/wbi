package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class ProgramSaveEvent extends ApplicationEvent {
    public ProgramSaveEvent(Object source) {
        super(source);
    }
}

