package com.broadcom.wbi.controller;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.elasticSearch.SkuSearch;
import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.Sku;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.elasticSearch.SkuSearchService;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.ProgramService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.service.jpa.RevisionService;
import com.broadcom.wbi.service.jpa.SkuService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.broadcom.wbi.service.indicator.IndicatorService.mapper;

@RestController
@RequestMapping("/api/program")
public class ProgramController {

    @Autowired
    private RedisCacheRepository redisCacheRepository;
    @Autowired
    private SkuSearchService skuSearchService;
    @Autowired
    private ProgramService programService;
    @Autowired
    private RevisionService revisionService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private SkuService skuService;


    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
    @RequestMapping(value = {"/checkExist"}, method = {RequestMethod.GET})
    public Callable<HashMap> checkExistProgram(HttpServletRequest req,
                                               @RequestParam(value = "program", defaultValue = "") final String pname) {
        return new Callable<HashMap>() {
            public HashMap call() {
                HashMap hm = new HashMap();
                hm.put("ret", true);
                if (!pname.trim().isEmpty()) {
                    List<SkuSearch> skulist = skuSearchService.findByPrefixSkuNum(pname.toLowerCase());
                    if (skulist != null && !skulist.isEmpty()) {
                        hm.put("ret", false);
                    }
                }
                return hm;
            }
        };
    }

    @RequestMapping(value = {"/getRevisionList"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getRevisionList(HttpServletRequest req,
                                              @RequestParam(value = "pid", defaultValue = "0") final int pid,
                                              @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            public List call() {
                if (pid < 1)
                    throw new IDNotFoundException(pid, "program");
                String redisKey  = pid+ "_revisionList";
                if (!redisCacheRepository.hasKey(redisKey) || reload==1) {
                    List<RevisionSearch> revs = revisionSearchService.findByProgram(pid);
                    if ((revs != null) && (!revs.isEmpty())) {
                        ExecutorService executor = Executors.newFixedThreadPool(3);
                        final List ret = Collections.synchronizedList(new ArrayList());
                        for (final RevisionSearch rev : revs) {
                            executor.submit(new Runnable() {
                                public void run() {
                                    HashMap hm = new HashMap();
                                    if (rev.getType().equalsIgnoreCase("ip")
                                            && rev.getRev_name().toLowerCase().indexOf("head_ip") == 0)
                                        return;
                                    hm = indicatorService.getFrontPageRevisionInfo(Integer.parseInt(rev.getId()));

                                    if (hm == null)
                                        hm = new HashMap();

                                    if (hm.containsKey("headline")) {
                                        String headline = hm.get("headline").toString();
                                        if (!headline.isEmpty()) {
                                            String[] hll = headline.split("<hr>");
                                            hm.put("headline", hll);
                                        }
                                    } else {
                                        hm.put("headline", "");
                                    }
                                    ret.add(hm);
                                }
                            });
                        }
                        executor.shutdown();
                        try {
                            executor.awaitTermination(5, TimeUnit.MINUTES);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            redisCacheRepository.put(redisKey, mapper.writeValueAsString(ret));
//                            redisCacheRepository.setExpire(redisKey, ProjectConstant.CacheTimeout.HOUR.getSecond());
                            return ret;
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisKey);
                try {
                    List ret = mapper.readValue(value, new TypeReference<ArrayList>() {
                    });
                    return ret;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(120000, callable);
    }

    @RequestMapping(value = {"/getSku"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getSku(HttpServletRequest req,
                                     @RequestParam(value = "pid", defaultValue = "0") final int pid,
                                     @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                if (pid < 1)
                    throw new IDNotFoundException(pid, "program");
                List<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
                List<SkuSearch> skus = skuSearchService.findByProgram(pid);
                if (skus != null && !skus.isEmpty()) {
                    for (SkuSearch sku : skus) {
                        HashMap hm = new HashMap();
                        hm.put("num", sku.getSkuNum());
                        hm.put("aka", sku.getAka());
                        hm.put("frequency", "");
                        hm.put("io", "");
                        hm.put("serdes", "");
                        hm.put("desc", "");
                        hm.put("portConfig", "");
                        hm.put("itemp", "");
                        hm.put("id", sku.getId());
                        hm.put("dateAvailable", "");

                        hm.put("id", sku.getId());
                        if (sku.getAka() != null) {
                            hm.put("aka", sku.getAka());
                        }
                        if (sku.getFrequency() != null) {
                            hm.put("frequency", sku.getFrequency());
                        }
                        if (sku.getIoCapacity() != null) {
                            hm.put("io", sku.getIoCapacity());
                        }
                        if (sku.getNumOfSerdes() != null) {
                            hm.put("serdes", sku.getNumOfSerdes());
                        }
                        if (sku.getDescription() != null) {
                            hm.put("desc", sku.getDescription());
                        }
                        if (sku.getPortConfig() != null) {
                            hm.put("portConfig", sku.getPortConfig());
                        }
                        if (sku.getItemp() != null) {
                            hm.put("itemp", sku.getItemp());
                        }
                        if (sku.getDateAvailable() != null) {
                            hm.put("dateAvailable", sku.getDateAvailable());
                        }
                        ret.add(hm);
                    }
                }
                if (ret != null && !ret.isEmpty()) {
                    return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'ADMIN')")
    @RequestMapping(value = {"/saveSku"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveSku(HttpServletRequest req, @RequestBody final HashMap skuMap) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map obj = new HashMap();
                if (!skuMap.containsKey("pid") || !skuMap.containsKey("data")) {
                    if (!skuMap.containsKey("pid")) {
                        obj.put("data", "Program is missing in request");
                    } else {
                        obj.put("data", "Data missing in request");
                    }
                    obj.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(obj);
                }
                int pid = Integer.parseInt(skuMap.get("pid").toString());
                final Program program = programService.findById(pid);
                final String username = SecurityContextHolder.getContext().getAuthentication().getName();
                if (program == null) {
                    obj.put("data", "Program Not found in database");
                    obj.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(obj);
                }
                List skulist = (ArrayList) skuMap.get("data");
                if (!skulist.isEmpty()) {
                    for (Object skuObj : skulist) {
                        HashMap hm = (HashMap) skuObj;
                        Sku sku = null;
                        if (hm.containsKey("id")) {
                            sku = skuService.findById(Integer.parseInt(hm.get("id").toString()));
                        }
                        boolean isDeleted = false;
                        boolean isNew = false;
                        if (hm.containsKey("isDeleted") && sku != null) {
                            isDeleted = (Boolean) hm.get("isDeleted");
                            if (isDeleted) {
                                skuService.delete(sku.getId());
                                List<SkuSearch> skusearch = skuSearchService.findBySkuNum(sku.getSkuNum());
                                if (skusearch != null && !skusearch.isEmpty()) {
                                    for (SkuSearch ss : skusearch) {
                                        skuSearchService.delete(ss.getId());
                                    }
                                }
                            }
                        } else {
                            SkuSearch ss = null;
                            if (hm.containsKey("isNew")) {
                                isNew = (Boolean) hm.get("isNew");
                                if (isNew) {
                                    sku = new Sku();
                                    ss = new SkuSearch();
                                    Revision rev = revisionService.findByProgramName(program, "A0");
                                    if (rev != null) {
                                        ss.setUrl("/program/" + program.getType().toString().toLowerCase() + "/"
                                                + program.getId() + "/" + rev.getId());
                                    }
                                }
                            } else {
                                if (sku != null) {
                                    ss = skuSearchService.findById(Integer.toString(sku.getId()));
                                }
                            }
                            sku.setProgram(program);
                            sku.setCreatedBy(username);

                            ss.setBaseNum(program.getBaseNum());
                            ss.setProgramDisplayName(program.getDisplayName());
                            ss.setProgramType(program.getType().toString());
                            ss.setProgram(program.getId());

                            if (hm.containsKey("aka")) {
                                ss.setAka(hm.get("aka").toString());
                                sku.setAka(hm.get("aka").toString());
                            }
                            if (hm.containsKey("num")) {
                                ss.setSkuNum(hm.get("num").toString());
                                sku.setSkuNum(hm.get("num").toString());
                            }
                            if (hm.containsKey("frequency")) {
                                ss.setFrequency(hm.get("frequency").toString());
                                sku.setFrequency(hm.get("frequency").toString());
                            }
                            if (hm.containsKey("desc")) {
                                ss.setDescription(hm.get("desc").toString());
                                sku.setDescription(hm.get("desc").toString());
                            }
                            if (hm.containsKey("io")) {
                                ss.setIoCapacity(hm.get("io").toString());
                                sku.setIoCapacity(hm.get("io").toString());
                            }
                            if (hm.containsKey("dateAvailable")) {
                                ss.setDateAvailable(hm.get("dateAvailable").toString());
                                sku.setDateAvailable(hm.get("dateAvailable").toString());
                            }
                            if (hm.containsKey("serdes")) {
                                ss.setNumOfSerdes(hm.get("serdes").toString());
                                sku.setNumOfSerdes(hm.get("serdes").toString());
                            }
                            if (hm.containsKey("portConfig")) {
                                ss.setPortConfig(hm.get("portConfig").toString());
                                sku.setPortConfig(hm.get("portConfig").toString());
                            }
                            if (hm.containsKey("itemp")) {
                                ss.setItemp(hm.get("itemp").toString());
                                sku.setItemp(hm.get("itemp").toString());
                            }
                            sku = skuService.saveOrUpdate(sku);
                            if (isNew)
                                ss.setId(Integer.toString(sku.getId()));
                            skuSearchService.saveOrUpdate(ss);
                        }
                    }
                }

                obj.put("code", HttpStatus.OK);
                // commonServ.clearCache(pid, "skuList", "program");
                obj.put("data", "Updated to db");
                return ResponseEntity.ok(obj);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);

    }

}
