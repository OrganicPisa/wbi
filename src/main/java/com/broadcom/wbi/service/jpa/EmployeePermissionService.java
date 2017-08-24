package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.EmployeePermission;

import java.util.List;

public interface EmployeePermissionService extends CRUDService<EmployeePermission> {

    List<EmployeePermission> findByEmployee(Employee e);

}
