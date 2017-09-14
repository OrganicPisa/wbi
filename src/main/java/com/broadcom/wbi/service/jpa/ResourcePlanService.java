package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.ResourcePlan;

import java.util.Date;
import java.util.List;


@SuppressWarnings("rawtypes")
public interface ResourcePlanService extends CRUDService<ResourcePlan> {

    List<ResourcePlan> findByProgram(Program program);

    Date findMaxResourceDate(Program program);

    Date findMinResourceDate(Program program);

//	List doParse(String f, Program program);

    List<String> findDistinctPlanSkill(Program p, String type);

    List<String> findDistinctResourceType(Program program);

}
