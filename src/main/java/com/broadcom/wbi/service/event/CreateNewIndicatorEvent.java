package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class CreateNewIndicatorEvent extends ApplicationEvent {

    public CreateNewIndicatorEvent(Object source) {
        super(source);
    }
}
