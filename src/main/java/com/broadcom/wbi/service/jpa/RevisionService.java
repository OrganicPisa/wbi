package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;
import java.util.List;

public interface RevisionService extends CRUDService<Revision> {

    List<Revision> findByProgram(Program program, DateTime dt);

    Revision findByProgramName(Program program, String name);

//    List<Integer> findBySegmentGroup(String segmentGroup, ProjectConstant.EnumProgramStatus stat);
//
//    List<Integer> findBySegment(Segment segment, ProjectConstant.EnumProgramType ptype, ProjectConstant.EnumProgramStatus stat);
//
//    List<Integer> findBySegment(Segment seg, String name, ProjectConstant.EnumProgramType type);

    List<Integer> findByEmployee(Employee empl);

    boolean isEmployeeBookmark(Revision rev, Employee empl);

    List<Revision> findByUpdateTime(DateTime dt);

    Revision createNewRevision(final HashMap map, final Program program);

    @Async
    void deleteRecordFromDB(Revision revision);

}
