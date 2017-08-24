package com.broadcom.wbi.service.elasticSearch;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Iterator;
import java.util.List;

public interface CRUDService<T> {

    final static DateTimeFormatter dfmt = DateTimeFormat.forPattern("yyyy-MM-dd");

    final static DateTimeFormatter dfmt2 = DateTimeFormat.forPattern("MM/dd/yy");

    void saveBulk(List<T> listOfDomainObject);

    Iterator<T> findAll();

    T findById(String id);

    T saveOrUpdate(T domainObject);

    void delete(String id);

    long count();

    void emptyData();

    void deleteIndex();

    List<T> findByDateTime(DateTime dt);


}
