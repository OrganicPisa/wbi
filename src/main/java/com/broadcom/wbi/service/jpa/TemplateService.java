package com.broadcom.wbi.service.jpa;


import com.broadcom.wbi.model.mysql.Template;

import java.util.List;

public interface TemplateService extends CRUDService<Template> {

    List<Template> findByType(String type);

    List<Template> findByTypeCategory(String type, String category);

    List<Template> findByTypeCategoryGroup(String type, String category, String group);

    List<Template> findByTypeCategoryGroupOndashboard(String type, String category, String group);
}
