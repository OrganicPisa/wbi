package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Sku;

import java.util.List;

public interface SkuService extends CRUDService<Sku> {


    List<Sku> findByProgram(Program p);

    List<Sku> findByNumProgram(Program p, String skuNum);


}
