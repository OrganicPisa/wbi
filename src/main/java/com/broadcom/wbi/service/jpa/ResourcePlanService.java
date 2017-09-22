package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.ResourcePlan;

import java.io.File;
import java.util.Date;
import java.util.List;


@SuppressWarnings("rawtypes")
public interface ResourcePlanService extends CRUDService<ResourcePlan> {

    List<ResourcePlan> findByProgram(Program program);

    Date findMaxResourceDate(Program program);

    Date findMinResourceDate(Program program);

    void doParse(Program program, File file);

    List<ResourcePlan> findByProgramAndType(Program p, String type);

    List<String> findDistinctResourceType(Program program);

    List<String> findDistinctTypeByProgram(Program program);

}
