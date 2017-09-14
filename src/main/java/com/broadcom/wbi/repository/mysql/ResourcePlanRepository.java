package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.ResourcePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;


public interface ResourcePlanRepository extends JpaRepository<ResourcePlan, Integer> {
    List<ResourcePlan> findDistinctByProgram(Program program);

    Long countAllByCreatedDateBefore(Date date);

    @Query("SELECT MAX(rsp.month) FROM ResourcePlan rsp where rsp.program=?1")
    Date findMaxResourceDateByProgram(Program program);

    @Query("SELECT MIN(rsp.month) FROM ResourcePlan rsp where rsp.program=?1")
    Date findMinResourceDateByProgram(Program program);

    @Query("SELECT DISTINCT rsp.type FROM ResourcePlan rsp where rsp.program = ?1 order by rsp.type ASC")
    List<String> findDistinctTypeByProgram(Program program);

    @Query("SELECT DISTINCT rsp.plan_skill from ResourcePlan rsp where rsp.program =?1 and rsp.type like ?2 order by rsp.plan_skill ASC")
    List<String> findDistinctSkillByProgramAndType(Program program, String type);

}
