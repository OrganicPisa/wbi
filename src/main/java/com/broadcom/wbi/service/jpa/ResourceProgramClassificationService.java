package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.ResourceProgramClassification;

import java.util.List;

public interface ResourceProgramClassificationService extends CRUDService<ResourceProgramClassification> {

    List<ResourceProgramClassification> findByType(String type, Boolean status);

    List<ResourceProgramClassification> findByNameType(String type, String name);

}
