package com.broadcom.wbi.repository.mysql;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Integer> {

    List<Sku> findByProgramOrderByAkaAsc(Program program);

    List<Sku> findByProgramAndSkuNumOrderByAkaAsc(Program program, String skuNum);

    Long countAllByCreatedDateBefore(Date dt);
}
