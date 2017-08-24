package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.util.ProjectConstant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface RevisionRepository extends JpaRepository<Revision, Integer> {

    List<Revision> findByCreatedDateAfterOrderByCreatedDateDesc(Date dt);

    List<Revision> findByProgramOrderByNameAsc(Program program);

    List<Revision> findByProgramAndCreatedDateAfter(Program program, Date dt);

    Revision findFirstByProgramAndNameOrderByNameAsc(Program program, String name);

    //    @Query("SELECT rev.id FROM Revision rev where LOCATE(rev.employees, ?1)>0 AND rev.isActive = ?2")
    List<Revision> findByEmployeesContainsAndAndIsActive(Employee employee, ProjectConstant.EnumProgramStatus is_active);

    List<Revision> findByEmployeesContainsOrderByIsActiveDesc(Employee employee);

    List<Revision> findByCreatedDateAfterOrderByNameAsc(Date dt);

    Long countAllByCreatedDateBefore(Date dt);
}
