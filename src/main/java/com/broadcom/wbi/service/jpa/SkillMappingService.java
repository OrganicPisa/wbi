package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.SkillMapping;

import java.util.List;

public interface SkillMappingService extends CRUDService<SkillMapping> {

    List<SkillMapping> findByProgram(Program program);
}
