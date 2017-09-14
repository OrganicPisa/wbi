package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Employee findFirstByAccNtOrderByAccNtAsc(String acct);

    Employee findFirstByEmailOrderByEmailAsc(String email);

    Long countAllByCreatedDateBefore(Date dt);
}


