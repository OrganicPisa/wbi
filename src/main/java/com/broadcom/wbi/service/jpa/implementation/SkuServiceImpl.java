package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Sku;
import com.broadcom.wbi.repository.mysql.SkuRepository;
import com.broadcom.wbi.service.elasticSearch.SkuSearchService;
import com.broadcom.wbi.service.jpa.SkuService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuSearchService skuSearchServ;

    @Resource
    private SkuRepository repo;

    @Override
    public Sku saveOrUpdate(Sku sku) {
        return repo.save(sku);
    }

    @Override
    public void delete(Integer id) {
        repo.delete(id);
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
    public Page<Sku> findAll(Integer init_num, Integer num) {
        Pageable pageable = new PageRequest(init_num, num);
        Page<Sku> ret = repo.findAll(pageable);
        if (ret != null && ret.getSize() > 0)
            return ret;
        return null;
    }

    @Override
    public List<Sku> listAll() {
        return repo.findAll();
    }


    @Override
    public List<Sku> saveBulk(List<Sku> skus) {
        return repo.save(skus);
    }

    @Override
    public Sku findById(Integer id) {
        return repo.findOne(id);
    }

    @Override
    public List<Sku> findByProgram(Program p) {
        return repo.findByProgramOrderByAkaAsc(p);
    }

    @Override
    public List<Sku> findByNumProgram(Program p, String skuNum) {
        return repo.findByProgramAndSkuNumOrderByAkaAsc(p, skuNum);
    }


}
