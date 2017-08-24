package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

    @Query("SELECT p FROM Program  p where LOWER(p.name) like LOWER(CONCAT('%', ?1, '%')) OR  " +
            "LOWER(p.baseNum) like LOWER(CONCAT('%', ?1, '%')) ORDER BY p.name ASC ")
    List<Program> findByName(String name);

    @Query("SELECT p FROM Program  p where LOWER(p.name) like LOWER(CONCAT('%', ?1, '%')) AND  " +
            "LOWER(p.type) like LOWER(CONCAT('%', ?2, '%')) ORDER BY p.name ASC ")
    List<Program> findByNameAndType(String name, String type);

    @Query("SELECT p FROM Program  p where LOWER(p.name) like LOWER(CONCAT('%', ?1, '%')) AND  " +
            "LOWER(p.baseNum) like LOWER(CONCAT('%', ?2, '%')) ORDER BY p.name ASC ")
    List<Program> findByNameAndBaseNum(String name, String baseNum);

    @Query("SELECT p FROM Program  p where LOWER(p.name) like LOWER(CONCAT('%', ?1, '%')) AND  " +
            "LOWER(p.baseNum) like LOWER(CONCAT('%', ?2, '%')) AND " +
            "LOWER(p.type) like LOWER(CONCAT('%', ?3, '%')) ORDER BY p.name ASC ")
    List<Program> findByNameAndBaseNumAndType(String name, String baseNum, String type);

    @Query("SELECT p FROM Program  p where " +
            "LOWER(p.baseNum) like LOWER(CONCAT('%', ?1, '%')) AND " +
            "LOWER(p.type) like LOWER(CONCAT('%', ?2, '%')) ORDER BY p.name ASC ")
    List<Program> findByBaseNumAndType(String baseNum, String type);

    List<Program> findBySegmentsContainsAndTypeOrderByOrderNumAsc(Segment segment, String type);

    Long countAllByCreatedDateBefore(Date dt);
}
