package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class IndicatorDateSaveEvent extends ApplicationEvent {
    public IndicatorDateSaveEvent(Object source) {
        super(source);
    }
}

