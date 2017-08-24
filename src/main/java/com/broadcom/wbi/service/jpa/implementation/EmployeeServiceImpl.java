package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.repository.mysql.EmployeeRepository;
import com.broadcom.wbi.service.jpa.EmployeeService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    @Resource
    private EmployeeRepository repo;

    @Override
    @Transactional
    public List<Employee> saveBulk(List<Employee> employees) {
        return repo.save(employees);
    }

    @Override
    public List<Employee> listAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
    public void delete(Integer id) {
//		EntityManager em = emf.createEntityManager();
//		em.getTransaction().begin();
//		em.remove(em.find(Employee.class, id));
//		em.getTransaction().commit();
        repo.delete(id);

    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
    public Employee saveOrUpdate(Employee object) {
//		EntityManager em = emf.createEntityManager();
//		em.getTransaction().begin();
//
//		Employee savedObject = em.merge(object);
//		em.getTransaction().commit();
//
//		return savedObject;
        return repo.save(object);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee findById(Integer id) {
//		EntityManager em = emf.createEntityManager();
//		return em.find(Employee.class, id);
        return repo.findOne(id);
    }

    @Override
    public Employee findByAccountName(String name) {
        return repo.findFirstByAccNtOrderByAccNtAsc(name);
    }

    @Override
    public Employee findByEmail(String email) {
        return repo.findFirstByEmailOrderByEmailAsc(email);
    }

    @Override
    public Long count() {
        return repo.count();
    }

    @Override
    public Long count(DateTime dt) {
        return repo.countAllByCreatedDateBefore(dt.toDate());
    }

    @Override
    public Page<Employee> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Employee> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }


}
