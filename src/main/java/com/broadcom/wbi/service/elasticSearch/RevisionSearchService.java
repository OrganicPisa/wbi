package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.util.ProjectConstant.EnumProgramType;

import java.util.List;

public interface RevisionSearchService extends CRUDService<RevisionSearch> {

    List<RevisionSearch> findBySegment(String segment);

    List<RevisionSearch> findBySegment(List<Segment> segments);

    List<RevisionSearch> findBySegment(String segment, String type);

    List<RevisionSearch> findBySegment(String segment, String type, Boolean isActive);

    List<RevisionSearch> findByProgram(int pid);

    List<RevisionSearch> findByProgram(int pid, Boolean isActive);

    RevisionSearch findByProgram(Integer program, String string);

    List<RevisionSearch> findByType(String type);

    List<RevisionSearch> findByProgram(String pname);

    List<RevisionSearch> searchByProgram(String pname);

    List<RevisionSearch> findByProgramType(EnumProgramType ptype, Boolean isActive);
}
