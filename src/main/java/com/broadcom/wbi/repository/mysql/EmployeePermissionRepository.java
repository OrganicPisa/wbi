package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.EmployeePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface EmployeePermissionRepository extends JpaRepository<EmployeePermission, Integer> {

    List<EmployeePermission> findDistinctByEmployeeOrderByPermissionAsc(Employee employee);

    Long countAllByCreatedDateBefore(Date dt);

}
