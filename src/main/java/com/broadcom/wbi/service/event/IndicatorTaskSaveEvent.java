package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class IndicatorTaskSaveEvent extends ApplicationEvent {
    public IndicatorTaskSaveEvent(Object source) {
        super(source);
    }
}

