package com.broadcom.wbi.controller;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.elasticSearch.SkuSearch;
import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.elasticSearch.SkuSearchService;
import com.broadcom.wbi.service.elasticSearch.TemplateSearchService;
import com.broadcom.wbi.service.event.CacheClearEventPublisher;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.*;
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
import javax.servlet.http.HttpServletResponse;
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

    private final RedisCacheRepository redisCacheRepository;
    private final SkuSearchService skuSearchService;
    private final ProgramService programService;
    private final RevisionService revisionService;
    private final RevisionSearchService revisionSearchService;
    private final IndicatorService indicatorService;
    private final SkuService skuService;
    private final SegmentService segmentService;
    private final HeadlineService headlineService;
    private final RevisionOutlookService revisionOutlookService;
    private final LinkService linkService;

    private final RevisionContactService revisionContactService;
    private final RevisionInformationService revisionInformationService;
    private final TemplateSearchService templateSearchService;

    @Autowired
    private CacheClearEventPublisher cacheClearEventPublisher;


    @Autowired
    public ProgramController(RedisCacheRepository redisCacheRepository, SkuSearchService skuSearchService, ProgramService programService,
                             RevisionService revisionService, RevisionSearchService revisionSearchService, IndicatorService indicatorService, SkuService skuService,
                             SegmentService segmentService, HeadlineService headlineService, RevisionOutlookService revisionOutlookService, LinkService linkService,
                             RevisionContactService revisionContactService, RevisionInformationService revisionInformationService, TemplateSearchService templateSearchService) {
        this.redisCacheRepository = redisCacheRepository;
        this.skuSearchService = skuSearchService;
        this.programService = programService;
        this.revisionService = revisionService;
        this.revisionSearchService = revisionSearchService;
        this.indicatorService = indicatorService;
        this.skuService = skuService;
        this.segmentService = segmentService;
        this.headlineService = headlineService;
        this.revisionOutlookService = revisionOutlookService;
        this.linkService = linkService;
        this.revisionContactService = revisionContactService;
        this.revisionInformationService = revisionInformationService;
        this.templateSearchService = templateSearchService;
    }


    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
    @RequestMapping(value = {"/checkExist"}, method = {RequestMethod.GET})
    public Callable<HashMap> checkExistProgram(HttpServletRequest req,
                                               @RequestParam(value = "program", defaultValue = "") final String pname) {
        return new Callable<HashMap>() {
            public HashMap call() {
                HashMap hm = new HashMap();
                hm.put("ret", false);
                if (!pname.trim().isEmpty()) {
                    List<SkuSearch> skulist = skuSearchService.findByPrefixSkuNum(pname.toLowerCase());
                    if (skulist != null && !skulist.isEmpty()) {
                        hm.put("ret", true);
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
                if (reload == 1)
                    redisCacheRepository.delete(redisKey);
                if (!redisCacheRepository.hasKey(redisKey)) {
                    List<RevisionSearch> revs = revisionSearchService.findByProgram(pid);
                    if ((revs != null) && (!revs.isEmpty())) {
                        ExecutorService executor = Executors.newFixedThreadPool(3);
                        final List ret = Collections.synchronizedList(new ArrayList());
                        for (final RevisionSearch rev : revs) {
                            executor.submit(new Runnable() {
                                public void run() {
                                    HashMap hm = new HashMap();
                                    if (rev.getProgram_type().equalsIgnoreCase("ip")
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
                            }
                        } else {
                            if (hm.containsKey("isNew")) {
                                isNew = (Boolean) hm.get("isNew");
                                if (isNew) {
                                    sku = new Sku();
                                }
                            }
                            sku.setProgram(program);

                            if (hm.containsKey("aka")) {
                                sku.setAka(hm.get("aka").toString());
                            }
                            if (hm.containsKey("num")) {
                                sku.setSkuNum(hm.get("num").toString());
                            }
                            if (hm.containsKey("frequency")) {
                                sku.setFrequency(hm.get("frequency").toString());
                            }
                            if (hm.containsKey("desc")) {
                                sku.setDescription(hm.get("desc").toString());
                            }
                            if (hm.containsKey("io")) {
                                sku.setIoCapacity(hm.get("io").toString());
                            }
                            if (hm.containsKey("dateAvailable")) {
                                sku.setDateAvailable(hm.get("dateAvailable").toString());
                            }
                            if (hm.containsKey("serdes")) {
                                sku.setNumOfSerdes(hm.get("serdes").toString());
                            }
                            if (hm.containsKey("portConfig")) {
                                sku.setPortConfig(hm.get("portConfig").toString());
                            }
                            if (hm.containsKey("itemp")) {
                                sku.setItemp(hm.get("itemp").toString());
                            }
                            skuService.saveOrUpdate(sku);
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

    /***********************************************************************
     * Add New Program
     *********************************************************************/
    @PreAuthorize("hasAnyRole('IPM', 'IPPM', 'CPM', 'PM', 'ADMIN')")
    @RequestMapping(value = {"/new"}, method = {RequestMethod.POST})
    @ResponseBody
    public WebAsyncTask<ResponseEntity> addNewProgram(HttpServletRequest req, HttpServletResponse res,
                                                      @RequestBody final HashMap reqMap) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            @Override
            public ResponseEntity call() throws Exception {
                Map ret = new HashMap();
                if (reqMap.containsKey("data")) {
                    final HashMap map = (HashMap) reqMap.get("data");
                    final String createtypestring = (String) reqMap.get("type");
                    Program p = programService.createNewProgram(map, createtypestring);
                    if (p != null)
                        ret.put("pid", p.getId());
                    else
                        ret.put("error", "Can't create program. Please report");
                }
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(1800000, callable);
    }


    @PreAuthorize("hasAnyRole('ADMIN')")
    @RequestMapping(value = {"/delete"}, method = {RequestMethod.POST})
    @ResponseBody
    public WebAsyncTask<ResponseEntity> deleteProgram(HttpServletRequest req, HttpServletResponse res,
                                                      @RequestParam(value = "pid", defaultValue = "0") final int pid) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            @Override
            public ResponseEntity call() throws Exception {
                Map ret = new HashMap();
                ret.put("url", "deleted");
                if (pid < 1)
                    return null;

                Program program = programService.findById(pid);
                if (program == null)
                    return null;
                List<Revision> revisionList = revisionService.findByProgram(program, null);
                if (revisionList != null && !revisionList.isEmpty()) {
                    for (Revision revision : revisionList) {
                        List<Link> linkList = linkService.findByRevision(revision);
                        if (linkList != null && !linkList.isEmpty()) {
                            for (Link link : linkList)
                                linkService.delete(link.getId());
                        }
                        List<RevisionContact> revisionContactList = revisionContactService.findByRevision(revision);
                        if (revisionContactList != null && !revisionContactList.isEmpty()) {
                            for (RevisionContact revisionContact : revisionContactList)
                                revisionContactService.delete(revisionContact.getId());
                        }
                        List<RevisionInformation> revisionInformationList = revisionInformationService.findByRevision(revision);
                        if (revisionInformationList != null && !revisionInformationList.isEmpty()) {
                            for (RevisionInformation revisionInformation : revisionInformationList)
                                revisionInformationService.delete(revisionInformation.getId());
                        }
                        List<Headline> headlineList = headlineService.findByRevision(revision, null);

                    }
                }
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(1800000, callable);
    }

}
