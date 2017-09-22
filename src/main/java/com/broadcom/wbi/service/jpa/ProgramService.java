package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.util.ProjectConstant;

import java.util.HashMap;
import java.util.List;

public interface ProgramService extends CRUDService<Program> {

    List<Program> findByName(String name);

    List<Program> checkExist(String name, String base, ProjectConstant.EnumProgramType type);

    List<Program> findBySegment(Segment segment, ProjectConstant.EnumProgramType type);

    Program createNewProgram(HashMap map, String createtypestring);


}
