package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.ResourcePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ResourcePlanRepository extends JpaRepository<ResourcePlan, Integer> {
    List<ResourcePlan> findDistinctByProgram(Program program);


}
