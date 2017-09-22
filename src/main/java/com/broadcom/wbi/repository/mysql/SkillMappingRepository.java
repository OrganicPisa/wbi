package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.SkillMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface SkillMappingRepository extends JpaRepository<SkillMapping, Integer> {

    List<SkillMapping> findDistinctByProgramOrderByOrderNumAsc(Program program);

    Long countAllByCreatedDateBefore(Date dt);
}
