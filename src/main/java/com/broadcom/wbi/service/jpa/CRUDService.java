package com.broadcom.wbi.service.jpa;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CRUDService<T> {

    final static DateTimeFormatter dfmt = DateTimeFormat.forPattern("yyyy-MM-dd");


    final static DateTimeFormatter dfmt2 = DateTimeFormat.forPattern("MM/dd/yy");

    List<T> saveBulk(List<T> listOfDomainObject);

    List<T> listAll();

    T findById(Integer id);

    T saveOrUpdate(T domainObject);

    void delete(Integer id);

    Long count();

    Long count(DateTime dt);

    Page<T> findAll(Integer init_num, Integer num);
}
