package com.broadcom.wbi.service.elasticSearch;

import com.broadcom.wbi.model.elasticSearch.EmployeeSearch;

import java.util.List;

public interface EmployeeSearchService extends CRUDService<EmployeeSearch> {

    List<EmployeeSearch> findByName(String name);

    List<EmployeeSearch> findByFirstNameAndLastName(String fname, String lname);

    List<EmployeeSearch> findByManager(String managerName);

    List<EmployeeSearch> findBySegment(String segment);

    List<EmployeeSearch> findBySegment(String segment, String profit_center);


}
