package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Employee;

public interface EmployeeService extends CRUDService<Employee> {

    Employee findByAccountName(String acctname);

    Employee findByEmail(String email);


}
