package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.SkillMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;


public interface SkillMappingRepository extends JpaRepository<SkillMapping, Integer> {

    List<SkillMapping> findDistinctByProgramOrderByOrderNumAsc(Program program);

    Long countAllByCreatedDateBefore(Date dt);
}
