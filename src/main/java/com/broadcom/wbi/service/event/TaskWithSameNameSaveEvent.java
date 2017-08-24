package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationEvent;

public class TaskWithSameNameSaveEvent extends ApplicationEvent {

    public TaskWithSameNameSaveEvent(Object source) {
        super(source);
    }
}
