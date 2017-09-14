package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class IndicatorGroupSaveEvent extends ApplicationEvent {
    public IndicatorGroupSaveEvent(Object source) {
        super(source);
    }
}

