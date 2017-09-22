package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Integer> {

    List<Template> findByTypeOrderByCreatedDateDesc(String type);

    List<Template> findByTypeAndCategoryOrderByCreatedDateDesc(String type, String category);

    List<Template> findByTypeAndCategoryAndGroupOrderByCreatedDateDesc(String type, String category, String group);

    List<Template> findByTypeAndCategoryAndGroupAndOnDashboardIsTrueOrderByCreatedDateDesc(String type, String category, String group);

    Long countAllByCreatedDateBefore(Date dt);

}
