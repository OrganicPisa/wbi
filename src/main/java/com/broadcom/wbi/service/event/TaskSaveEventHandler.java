package com.broadcom.wbi.service.event;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TaskSaveEventHandler implements ApplicationListener<TaskWithSameNameSaveEvent> {
    @Override
    public void onApplicationEvent(TaskWithSameNameSaveEvent taskWithSameNameSaveEvent) {

        Map map = (HashMap) taskWithSameNameSaveEvent.getSource();
        System.out.println(map);
        //
    }
}
