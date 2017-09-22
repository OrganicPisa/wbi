package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.ResourceProgramClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ResourceProgramClassificationRepository extends JpaRepository<ResourceProgramClassification, Integer> {

    List<ResourceProgramClassification> findDistinctByTypeAndStatusOrderByCreatedDateAsc(String type, Boolean status);

    List<ResourceProgramClassification> findDistinctByNameOrderByCreatedDateDesc(String name);

    List<ResourceProgramClassification> findDistinctByTypeOrderByCreatedDateDesc(String type);

    List<ResourceProgramClassification> findDistinctByTypeAndNameOrderByCreatedDateDesc(String type, String name);

    Long countAllByCreatedDateBefore(Date date);
}
