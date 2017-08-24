package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.EmployeePermission;
import com.broadcom.wbi.repository.mysql.EmployeePermissionRepository;
import com.broadcom.wbi.service.jpa.EmployeePermissionService;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EmployeePermissionServiceImpl implements EmployeePermissionService {
    @Resource
    private EmployeePermissionRepository repo;

    @Override
    @Transactional
    public List<EmployeePermission> saveBulk(List<EmployeePermission> permissions) {
        return repo.save(permissions);
    }

    @Override
    public List<EmployeePermission> listAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
    public void delete(Integer id) {
        repo.delete(id);
//        EntityManager em = emf.createEntityManager();
//        em.getTransaction().begin();
//        em.remove(em.find(EmployeePermission.class, id));
//        em.getTransaction().commit();
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
    public Page<EmployeePermission> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<EmployeePermission> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    @Transactional(rollbackFor = {IDNotFoundException.class})
    public EmployeePermission saveOrUpdate(EmployeePermission object) {
        return repo.saveAndFlush(object);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePermission findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePermission> findByEmployee(Employee e) {
        return repo.findDistinctByEmployeeOrderByPermissionAsc(e);
    }

}
