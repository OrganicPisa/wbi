package com.broadcom.wbi.service.indexing;

import com.broadcom.wbi.model.elasticSearch.*;
import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.jpa.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class IndexSearchServiceImpl implements IndexSearchService {

    final Integer THREAD_POOL_SIZE = 1000;

    final Long MYSQL_PAGE_SIZE = new Long(10000);

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final SkuService skuService;
    private final ProgramService programService;
    private final SkuSearchService skuSearchService;
    private final RevisionService revisionService;
    private final RevisionSearchService revisionSearchService;
    private final RevisionOutlookService revisionOutlookService;
    private final RevisionInformationService revisionInformationService;
    private final RevisionInformationSearchService revisionInformationSearchService;
    private final RevisionContactService revisionContactService;
    private final RevisionContactSearchService revisionContactSearchService;
    private final IGroupService iGroupService;
    private final IGroupHistoryService iGroupHistoryService;
    private final IndicatorGroupSearchService indicatorGroupSearchService;
    private final ITaskService iTaskService;
    private final ITaskHistoryService iTaskHistoryService;
    private final IndicatorTaskSearchService indicatorTaskSearchService;
    private final IDateService iDateService;
    private final IDateHistoryService iDateHistoryService;
    private final IndicatorDateSearchService indicatorDateSearchService;
    private final HeadlineService headlineService;
    private final HeadlineSearchService headlineSearchService;
    private final TemplateService templateService;
    private final TemplateSearchService templateSearchService;
    private final ResourcePlanService resourcePlanService;
    private final ResourcePlanSearchService resourcePlanSearchService;

    @Autowired
    public IndexSearchServiceImpl(ElasticsearchTemplate elasticsearchTemplate, SkuService skuService, SkuSearchService skuSearchService,
                                  RevisionService revisionService, RevisionSearchService revisionSearchService, TemplateService templateService,
                                  RevisionOutlookService revisionOutlookService, HeadlineSearchService headlineSearchService, IndicatorGroupSearchService indicatorGroupSearchService,
                                  ITaskHistoryService iTaskHistoryService, RevisionInformationService revisionInformationService, IndicatorDateSearchService indicatorDateSearchService,
                                  TemplateSearchService templateSearchService, HeadlineService headlineService, RevisionInformationSearchService revisionInformationSearchService,
                                  RevisionContactService revisionContactService, IGroupHistoryService iGroupHistoryService, ITaskService iTaskService, IDateHistoryService iDateHistoryService,
                                  IDateService iDateService, RevisionContactSearchService revisionContactSearchService,
                                  IndicatorTaskSearchService indicatorTaskSearchService, IGroupService iGroupService, ResourcePlanService resourcePlanService, ResourcePlanSearchService resourcePlanSearchService, ProgramService programService) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.skuService = skuService;
        this.skuSearchService = skuSearchService;
        this.revisionService = revisionService;
        this.revisionSearchService = revisionSearchService;
        this.templateService = templateService;
        this.revisionOutlookService = revisionOutlookService;
        this.headlineSearchService = headlineSearchService;
        this.indicatorGroupSearchService = indicatorGroupSearchService;
        this.iTaskHistoryService = iTaskHistoryService;
        this.revisionInformationService = revisionInformationService;
        this.indicatorDateSearchService = indicatorDateSearchService;
        this.templateSearchService = templateSearchService;
        this.headlineService = headlineService;
        this.revisionInformationSearchService = revisionInformationSearchService;
        this.revisionContactService = revisionContactService;
        this.iGroupHistoryService = iGroupHistoryService;
        this.iTaskService = iTaskService;
        this.iDateHistoryService = iDateHistoryService;
        this.iDateService = iDateService;
        this.revisionContactSearchService = revisionContactSearchService;
        this.indicatorTaskSearchService = indicatorTaskSearchService;
        this.iGroupService = iGroupService;
        this.resourcePlanService = resourcePlanService;
        this.resourcePlanSearchService = resourcePlanSearchService;
        this.programService = programService;
    }


    @Override
    public Future<Boolean> indexAllSku(DateTime dt) {
        if (elasticsearchTemplate.indexExists(SkuSearch.class)) {
            elasticsearchTemplate.deleteIndex(SkuSearch.class);
        }
        elasticsearchTemplate.createIndex(SkuSearch.class);
        elasticsearchTemplate.putMapping(SkuSearch.class);
        Long count = revisionService.count();
        System.out.println("Indexing SKU " + count);
        List<Sku> skuList = skuService.listAll();
        final List<SkuSearch> ssl = Collections.synchronizedList(new ArrayList<SkuSearch>());
        if (skuList != null && !skuList.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            for (final Sku sku : skuList) {
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            Program program = sku.getProgram();
                            if (program == null) {
                                return;
                            }
                            SkuSearch ss = new SkuSearch();
                            ss.setId(Integer.toString(sku.getId()));
                            ss.setAka(sku.getAka().toLowerCase());
                            ss.setOtherName(sku.getAka().replaceAll("\\+", "plus").toLowerCase());
                            ss.setBaseNum(program.getBaseNum().toLowerCase());
                            ss.setDescription(sku.getDescription().toLowerCase());
                            ss.setFrequency(sku.getFrequency());
                            ss.setIoCapacity(sku.getIoCapacity());
                            ss.setNumOfSerdes(sku.getNumOfSerdes());
                            ss.setPortConfig(sku.getPortConfig());
                            ss.setProgramDisplayName(program.getDisplayName().toLowerCase());
                            ss.setProgramName(program.getDisplayName().toLowerCase());
                            ss.setProgramType(program.getType().toString().toLowerCase());
                            ss.setSkuNum(sku.getSkuNum());
                            ss.setProgram(program.getId());
                            ss.setDateAvailable(sku.getDateAvailable());
                            ss.setItemp(sku.getItemp());
                            ss.setFrequency(sku.getFrequency());
                            if (program != null) {
                                Revision a0 = revisionService.findByProgramName(program, "a0");
                                if (a0 != null) {
                                    ss.setUrl("/program/" + program.getType().toString().toLowerCase() + "/"
                                            + program.getId() + "/" + a0.getId() + "/dashboard");
                                }
                            }
                            ssl.add(ss);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
            executor.shutdown();
            try {
                executor.awaitTermination(30, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Done collecting\nStart to insert to ES");
            skuSearchService.saveBulk(ssl);
            System.out.println("Done Inserting Sku");
        }
        return new AsyncResult<Boolean>(true);
    }

    @Override
    public Future<Boolean> indexAllRevision(DateTime dt) {
        Long count = new Long(0);
        try {
            if (dt == null) {
                if (elasticsearchTemplate.indexExists(RevisionSearch.class)) {
                    elasticsearchTemplate.deleteIndex(RevisionSearch.class);
                    elasticsearchTemplate.createIndex(RevisionSearch.class);
                    elasticsearchTemplate.putMapping(RevisionSearch.class);
                }
                count = revisionService.count();
            } else {
                List<RevisionSearch> rsl = new ArrayList<>();
                rsl = revisionSearchService.findByDateTime(dt);
                if (rsl != null && !rsl.isEmpty()) {
                    for (RevisionSearch rs : rsl) {
                        revisionSearchService.delete(rs.getId());
                    }
                }
                count = revisionService.count(dt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Indexing revision " + count);
        Long stop = new Long(0);
        if (count > 0) {
            int index = 0;
            do {
                List<Revision> revs = null;
                if (dt == null) {
                    Page<Revision> revlist = revisionService.findAll(index, MYSQL_PAGE_SIZE.intValue());
                    if (revlist != null && revlist.getSize() > 0)
                        revs = revlist.getContent();
                } else {
                    revs = revisionService.findByUpdateTime(dt);
                }
                if (revs == null)
                    return null;
                final List<RevisionSearch> rsl = Collections.synchronizedList(new ArrayList<RevisionSearch>());
                if (revs != null && !revs.isEmpty()) {
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    for (final Revision rev : revs) {
                        executor.submit(new Runnable() {
                            public void run() {
                                try {
                                    Program p = rev.getProgram();
                                    Set<Segment> segments = p.getSegments();
                                    RevisionSearch rs = new RevisionSearch();
                                    rs.setId(Integer.toString(rev.getId()));
                                    rs.setInclude_in_report(rev.getIsRevisionIncludeInReport());
                                    rs.setIp_related(rev.getIpRelated());
                                    rs.setProgram_name(p.getDisplayName().toLowerCase().replace(".", "").replaceAll("\\+", "plus").trim());
                                    rs.setBase_num(p.getBaseNum());
                                    if (rev.getIsActive().toString().equalsIgnoreCase("active")) {
                                        rs.setIs_active(true);
                                    } else {
                                        rs.setIs_active(false);
                                    }
                                    rs.setIs_protected(rev.getIsProtected());
                                    rs.setRev_name(rev.getName().toLowerCase());
                                    rs.setRev_order_num(rev.getOrderNum());
                                    rs.setProgram_order_num(p.getOrderNum());
                                    rs.setProgram_id(p.getId());
                                    rs.setSegment(segments.iterator().next().getName().toLowerCase());
                                    rs.setProgram_type(p.getType().toString().toLowerCase());
                                    RevisionOutlook outlook = revisionOutlookService.findByRevision(rev);
                                    if (outlook != null) {
                                        rs.setOutlook(outlook.getContent());
                                        rs.setLast_updated_outlook_date(outlook.getCreatedDate());
                                    }
                                    rsl.add(rs);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(120, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Start to insert revision to ES");
                    revisionSearchService.saveBulk(rsl);
                }
                if (dt == null) {
                    stop += MYSQL_PAGE_SIZE;
                    index++;
                } else {
                    stop = count;
                }
            } while (stop < count);
            System.out.println("Done Inserting revision");
        }
        return new AsyncResult<Boolean>(true);
    }

    @Override
    public Future<Boolean> indexAllRevisionInformation(int reload) {
        if (reload == 1) {
            if (elasticsearchTemplate.indexExists(RevisionInformationSearch.class)) {
                elasticsearchTemplate.deleteIndex(RevisionInformationSearch.class);
                elasticsearchTemplate.createIndex(RevisionInformationSearch.class);
                elasticsearchTemplate.putMapping(RevisionInformationSearch.class);
            }
        }
        Long count = revisionInformationService.count();
        System.out.println("Indexing all information " + count);
        Long stop = new Long(0);
        if (count > 0) {
            int index = 0;
            do {
                Page<RevisionInformation> infos = revisionInformationService.findAll(index, MYSQL_PAGE_SIZE.intValue());
                if (infos != null && infos.getSize() > 0) {
                    final List<RevisionInformationSearch> revisionInformationSearchList = Collections.synchronizedList(new ArrayList<RevisionInformationSearch>());
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    for (final RevisionInformation info : infos) {
                        executor.submit(new Runnable() {
                            public void run() {
                                Revision rev = info.getRevision();
                                RevisionInformationSearch revisionInformationSearch = new RevisionInformationSearch();
                                revisionInformationSearch.setId(Integer.toString(info.getId()));
                                revisionInformationSearch.setCreated_date(info.getCreatedDate());
                                revisionInformationSearch.setOrderNum(info.getOrderNum());
                                revisionInformationSearch.setLast_updated_date(info.getLastUpdatedDate());
                                revisionInformationSearch.setOnDashboard(info.getOnDashboard());
                                revisionInformationSearch.setPhase(info.getPhase().toLowerCase().trim());
                                revisionInformationSearch.setName("");
                                revisionInformationSearch.setValue("");
                                if (info.getName() != null && !info.getName().trim().isEmpty())
                                    revisionInformationSearch.setName(info.getName().toLowerCase().trim());
                                if (info.getValue() != null && !info.getValue().trim().isEmpty())
                                    revisionInformationSearch.setValue(info.getValue().toLowerCase().trim());

                                revisionInformationSearch.setIsUserEditable(info.getIsUserEditable());
                                revisionInformationSearch.setIsRestrictedView(info.getIsRestrictedView());
                                revisionInformationSearch.setRevision(rev.getId());
                                revisionInformationSearchList.add(revisionInformationSearch);
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(3, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Start to insert to ES from " + index);
                    if (revisionInformationSearchList != null && !revisionInformationSearchList.isEmpty()) {
                        revisionInformationSearchService.saveBulk(revisionInformationSearchList);
                    }
                }
                stop += MYSQL_PAGE_SIZE;
                index++;
            } while (stop < count);
        }

        System.out.println("Done Inserting Information");
        return new AsyncResult<Boolean>(true);
    }

    @Override
    public Future<Boolean> indexProgramInformationByRevision(int rid) {
        Revision rev = revisionService.findById(rid);
        List<RevisionInformationSearch> risl = revisionInformationSearchService.findByRevision(rid);
        if (risl != null && !risl.isEmpty()) {
            for (RevisionInformationSearch ris : risl)
                revisionInformationSearchService.delete(ris.getId());
        }
        List<RevisionInformation> infos = revisionInformationService.findByRevision(rev);
        if (infos != null && !infos.isEmpty()) {
            final List<RevisionInformationSearch> revisionInformationSearchList = Collections.synchronizedList(new ArrayList<RevisionInformationSearch>());
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            for (final RevisionInformation info : infos) {
                executor.submit(new Runnable() {
                    public void run() {
                        Revision rev = info.getRevision();
                        RevisionInformationSearch revisionInformationSearch = new RevisionInformationSearch();
                        revisionInformationSearch.setId(Integer.toString(info.getId()));
                        revisionInformationSearch.setCreated_date(info.getCreatedDate());
                        revisionInformationSearch.setOrderNum(info.getOrderNum());
                        revisionInformationSearch.setLast_updated_date(info.getLastUpdatedDate());
                        revisionInformationSearch.setOnDashboard(info.getOnDashboard());
                        revisionInformationSearch.setPhase(info.getPhase().toLowerCase().trim());
                        revisionInformationSearch.setName("");
                        revisionInformationSearch.setValue("");
                        if (info.getName() != null && !info.getName().trim().isEmpty())
                            revisionInformationSearch.setName(info.getName().toLowerCase().trim());
                        if (info.getValue() != null && !info.getValue().trim().isEmpty())
                            revisionInformationSearch.setValue(info.getValue().toLowerCase().trim());
                        revisionInformationSearch.setIsUserEditable(info.getIsUserEditable());
                        revisionInformationSearch.setIsRestrictedView(info.getIsRestrictedView());
                        revisionInformationSearch.setRevision(rev.getId());
                        revisionInformationSearchList.add(revisionInformationSearch);
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(3, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Start to insert to ES");
            if (revisionInformationSearchList != null && !revisionInformationSearchList.isEmpty()) {
                revisionInformationSearchService.saveBulk(revisionInformationSearchList);
            }
        }
        System.out.println("Done Inserting Information");
        return new AsyncResult<Boolean>(true);


    }

    @Override
    public Future<Boolean> indexAllResourcePlan(DateTime dt) {
        if (elasticsearchTemplate.indexExists(ResourcePlanSearch.class)) {
            elasticsearchTemplate.deleteIndex(ResourcePlanSearch.class);
            elasticsearchTemplate.createIndex(ResourcePlanSearch.class);
            elasticsearchTemplate.putMapping(ResourcePlanSearch.class);
        }
        List<ResourcePlan> resources = resourcePlanService.listAll();
        final List<ResourcePlanSearch> rpl = Collections.synchronizedList(new ArrayList<ResourcePlanSearch>());
        if (resources != null && !resources.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            for (final ResourcePlan resource : resources) {
                executor.submit(new Runnable() {
                    public void run() {
                        ResourcePlanSearch rp = new ResourcePlanSearch();
                        rp.setCount(resource.getCount());
                        rp.setInclude_contractor(resource.getInclude_contractor());
                        rp.setMonth(resource.getMonth());
                        rp.setPlan_type(resource.getType().toLowerCase().trim());
                        rp.setProgram(resource.getProgram().getId());
                        rp.setSkill(resource.getPlan_skill().toLowerCase().trim());
                        rpl.add(rp);
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(3, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Done collecting...\nStart to insert to ES");
            resourcePlanSearchService.saveBulk(rpl);
            System.out.println("Done Inserting Resource Plan");
        }
        return new AsyncResult<Boolean>(true);
    }


    @Override
    public Future<Boolean> indexAllRevisionContact(int reload) {
        if (reload == 1) {
            if (elasticsearchTemplate.indexExists(RevisionContactSearch.class)) {
                elasticsearchTemplate.deleteIndex(RevisionContactSearch.class);
                elasticsearchTemplate.createIndex(RevisionContactSearch.class);
                elasticsearchTemplate.putMapping(RevisionContactSearch.class);
            }
        }
        System.out.println("Indexing contact");
        List<RevisionContact> contacts = revisionContactService.listAll();
        List<RevisionContactSearch> csl = Collections.synchronizedList(new ArrayList<RevisionContactSearch>());
        if (contacts != null && !contacts.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            for (final RevisionContact info : contacts) {
                executor.submit(new Runnable() {
                    public void run() {
                        String value = info.getValue();
                        RevisionContactSearch pc = new RevisionContactSearch();
                        pc.setId(Integer.toString(info.getId()));
                        pc.setCreated_date(info.getCreatedDate());
                        pc.setLast_updated_date(info.getLastUpdatedDate());
                        pc.setName(info.getName().toLowerCase().trim());
                        pc.setOnDashboard(true);
                        pc.setRevision(info.getRevision().getId());
                        pc.setValue(value.trim().toLowerCase());
                        csl.add(pc);
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(3, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Done Parsing...\nStart to insert to ES \n");
            revisionContactSearchService.saveBulk(csl);
        }
        System.out.println("Done Inserting Program Contact");

        return new AsyncResult<Boolean>(true);
    }

    @Override
    public Future<Boolean> indexAllIndicatorGroup(DateTime dt) {
        Long count = new Long(0);
        if (dt == null) {
            if (elasticsearchTemplate.indexExists(IndicatorGroupSearch.class)) {
                elasticsearchTemplate.deleteIndex(IndicatorGroupSearch.class);
                elasticsearchTemplate.createIndex(IndicatorGroupSearch.class);
                elasticsearchTemplate.putMapping(IndicatorGroupSearch.class);
            }
            count = iGroupHistoryService.count();
        } else {
            List<IndicatorGroupSearch> deligsl = new ArrayList<IndicatorGroupSearch>();
            deligsl = indicatorGroupSearchService.findByDateTime(dt);
            if (deligsl != null && !deligsl.isEmpty()) {
                for (IndicatorGroupSearch igs : deligsl) {
                    indicatorGroupSearchService.delete(igs.getId());
                }
            }
            count = iGroupHistoryService.count(dt);
        }
        System.out.println("Indexing indicator group");
        System.out.println(count);
        Long stop = new Long(0);
        if (count > 0) {
            int index = 0;
            do {
                List<IGroupHistory> ighl = null;
                if (dt == null) {
                    Page<IGroupHistory> ighp = iGroupHistoryService.findAll(index, MYSQL_PAGE_SIZE.intValue());
                    if (ighp != null && ighp.getSize() > 0)
                        ighl = ighp.getContent();
                } else {
                    ighl = iGroupHistoryService.findByUpdateDate(dt);
                }
                if (ighl == null || ighl.isEmpty())
                    return null;
                if (ighl != null && !ighl.isEmpty()) {
                    final List<IndicatorGroupSearch> igsl = Collections.synchronizedList(new ArrayList<IndicatorGroupSearch>());
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    for (final IGroupHistory igh : ighl) {
                        executor.submit(new Runnable() {
                            public void run() {
                                try {
                                    IndicatorGroupSearch igs = new IndicatorGroupSearch();
                                    igs.setId(Integer.toString(igh.getId()));
                                    IGroup ig = igh.getIGroup();
                                    Revision rev = ig.getRevision();
                                    igs.setIgroup_name(ig.getName().toLowerCase());
                                    igs.setIgroup_id(ig.getId());
                                    igs.setOrder_num(ig.getOrderNum());
                                    igs.setRevision_id(rev.getId());
                                    igs.setRevision_name(rev.getName());
                                    igs.setStatus(igh.getStatus().toString().toLowerCase());
                                    igs.setRemark(igh.getRemark());
                                    igs.setLast_updated_date(igh.getCreatedDate());
                                    igsl.add(igs);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(12, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Start to insert indicator group to ES from " + index);
                    indicatorGroupSearchService.saveBulk(igsl);
                }

                if (dt == null) {
                    stop += MYSQL_PAGE_SIZE;
                    index++;
                } else {
                    stop = count;
                }
            } while (stop < count);
            System.out.println("Done Inserting Group");
        }
        return new AsyncResult<Boolean>(true);

    }

    @Override
    public Future<Boolean> indexAllIndicatorTask(DateTime dt) {

        Long count = new Long(0);
        if (dt == null) {
            if (elasticsearchTemplate.indexExists(IndicatorTaskSearch.class)) {
                elasticsearchTemplate.deleteIndex(IndicatorTaskSearch.class);
                elasticsearchTemplate.createIndex(IndicatorTaskSearch.class);
                elasticsearchTemplate.putMapping(IndicatorTaskSearch.class);
            }
            count = iTaskHistoryService.count();
        } else {
            List<IndicatorTaskSearch> delitsl = new ArrayList<IndicatorTaskSearch>();
            delitsl = indicatorTaskSearchService.findByDateTime(dt);
            if (delitsl != null && !delitsl.isEmpty()) {
                for (IndicatorTaskSearch its : delitsl) {
                    System.out.println(its.getRevision_id() + " " + its.getIgroup_name());
                    indicatorTaskSearchService.delete(its.getId());
                }
            }
            count = iTaskHistoryService.count(dt);
        }

        System.out.println("Indexing indicator task " + count);
        Long stop = new Long(0);
        if (count > 0) {
            int index = 0;
            do {
                List<ITaskHistory> ithl = null;
                if (dt == null) {
                    Page<ITaskHistory> ithp = iTaskHistoryService.findAll(index, MYSQL_PAGE_SIZE.intValue());
                    if (ithp != null && ithp.getSize() > 0)
                        ithl = ithp.getContent();
                } else {
                    ithl = iTaskHistoryService.findByUpdateDate(dt);
                }
                if (ithl == null || ithl.isEmpty())
                    return null;

                final List<IndicatorTaskSearch> itsl = Collections.synchronizedList(new ArrayList<IndicatorTaskSearch>());
                if (ithl != null && !ithl.isEmpty()) {
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    for (final ITaskHistory ith : ithl) {
                        executor.submit(new Runnable() {
                            public void run() {
                                try {
                                    ITask itask = ith.getITask();
                                    IGroup ig = itask.getIGroup();
                                    Revision rev = ig.getRevision();
                                    IndicatorTaskSearch its = new IndicatorTaskSearch();
                                    its.setId(Integer.toString(ith.getId()));
                                    its.setRevision_id(rev.getId());
                                    its.setRevision_name(rev.getName());
                                    its.setTask_id(itask.getId());
                                    its.setTask_name(itask.getName().toLowerCase().trim());
                                    if (itask.getNameInReport() == null || itask.getNameInReport().trim().isEmpty()) {
                                        its.setTask_name_in_report(itask.getName().toLowerCase().trim());
                                        itask.setNameInReport(itask.getName());
                                        itask = iTaskService.saveOrUpdate(itask);
                                    } else {
                                        its.setTask_name_in_report(itask.getNameInReport().toLowerCase().trim());
                                    }
                                    its.setIgroup_name(ig.getName().toLowerCase());
                                    its.setIgroup_id(ig.getId());
                                    its.setOrder_num(itask.getOrderNum());
                                    its.setLast_updated_date(ith.getCreatedDate());
                                    its.setNote(ith.getNote());
                                    its.setStatus(ith.getStatus().toString().toLowerCase());
                                    itsl.add(its);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(12, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Start to insert indicator task to ES from " + index);
                    indicatorTaskSearchService.saveBulk(itsl);
                }

                if (dt == null) {
                    stop += MYSQL_PAGE_SIZE;
                    index++;
                } else {
                    stop = count;
                }
            } while (stop < count);
            System.out.println("Done Inserting Task");
        }
        return new AsyncResult<Boolean>(true);

    }

    @Override
    public Future<Boolean> indexAllIndicatorDate(DateTime dt) {
        Long count = new Long(0);
        if (dt == null) {
            if (elasticsearchTemplate.indexExists(IndicatorDateSearch.class)) {
                elasticsearchTemplate.deleteIndex(IndicatorDateSearch.class);
                elasticsearchTemplate.createIndex(IndicatorDateSearch.class);
                elasticsearchTemplate.putMapping(IndicatorDateSearch.class);
            }
            count = iDateHistoryService.count();
        } else {
            List<IndicatorDateSearch> delitsl = new ArrayList<IndicatorDateSearch>();
            delitsl = indicatorDateSearchService.findByDateTime(dt);
            if (delitsl != null && !delitsl.isEmpty()) {
                for (IndicatorDateSearch its : delitsl) {
                    System.out.println(its.getTask_name() + " " + its.getDate_name());
                    indicatorDateSearchService.delete(its.getId());
                }
            }
            count = iDateHistoryService.count(dt);
        }

        System.out.println("Indexing indicator date " + count);
        Long stop = new Long(0);
        if (count > 0) {
            int index = 0;
            do {
                List<IDateHistory> idhl = null;
                if (dt == null) {
                    Page<IDateHistory> idhp = iDateHistoryService.findAll(index, MYSQL_PAGE_SIZE.intValue());
                    if (idhp != null && idhp.getSize() > 0)
                        idhl = idhp.getContent();
                } else {
                    idhl = iDateHistoryService.findByUpdateDate(dt);
                }
                if (idhl == null || idhl.isEmpty())
                    return null;

                final List<IndicatorDateSearch> idsl = Collections
                        .synchronizedList(new ArrayList<IndicatorDateSearch>());
                if (idhl != null && !idhl.isEmpty()) {
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    for (final IDateHistory idh : idhl) {
                        executor.submit(new Runnable() {
                            public void run() {
                                try {
                                    IDate idate = idh.getIDate();
                                    ITask itask = idate.getITask();
                                    IGroup ig = itask.getIGroup();
                                    IndicatorDateSearch ids = new IndicatorDateSearch();
                                    ids.setId(Integer.toString(idh.getId()));
                                    String name = idate.getTtype().toString().toLowerCase() + "_" + idate.getEtype().toString().toLowerCase();
                                    ids.setDate_name(name);
                                    ids.setGroup_id(ig.getId());
                                    ids.setGroup_name(ig.getName());
                                    ids.setDate_id(idate.getId());
                                    ids.setTask_id(itask.getId());
                                    ids.setTask_name(itask.getName().toLowerCase());
                                    ids.setComment(idh.getComment());
                                    ids.setValue(idh.getValue());
                                    ids.setLast_updated_date(idh.getCreatedDate());
                                    ids.setStatus(idh.getStatus().toString().toLowerCase());
                                    idsl.add(ids);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(12, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Start to insert indicator date to ES from " + index);
                    indicatorDateSearchService.saveBulk(idsl);
                }

                if (dt == null) {
                    stop += MYSQL_PAGE_SIZE;
                    index++;
                } else {
                    stop = count;
                }
            } while (stop < count);
            System.out.println("Done Inserting Date");
        }
        return new AsyncResult<Boolean>(true);

    }

    @Override
    public Future<Boolean> indexAllHeadline(DateTime dt) {
        Long count = new Long(0);
        if (dt == null) {
            if (elasticsearchTemplate.indexExists(HeadlineSearch.class)) {
                elasticsearchTemplate.deleteIndex(HeadlineSearch.class);
                elasticsearchTemplate.createIndex(HeadlineSearch.class);
                elasticsearchTemplate.putMapping(HeadlineSearch.class);
            }
            count = headlineService.count();
        } else {
            List<HeadlineSearch> headlineSearchList = new ArrayList<HeadlineSearch>();
            headlineSearchList = headlineSearchService.findByDateTime(dt);
            if (headlineSearchList != null && !headlineSearchList.isEmpty()) {
                for (HeadlineSearch its : headlineSearchList) {
                    headlineSearchService.delete(its.getId());
                }
            }
            count = headlineService.count(dt);
        }
        System.out.println("Indexing headline " + count);
        Long stop = new Long(0);
        if (count > 0) {
            int index = 0;
            do {
                List<Headline> hll = null;
                if (dt == null) {
                    Page<Headline> hls = headlineService.findAll(index, MYSQL_PAGE_SIZE.intValue());
                    if (hls != null && hls.getSize() > 0)
                        hll = hls.getContent();
                } else {
                    hll = headlineService.findByUpdateDate(dt);
                }
                if (hll == null || hll.isEmpty())
                    return null;
                final List<HeadlineSearch> hlsl = Collections.synchronizedList(new ArrayList<HeadlineSearch>());
                if (hll != null && !hll.isEmpty()) {
                    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                    for (final Headline hl : hll) {
                        executor.submit(new Runnable() {
                            public void run() {
                                try {
                                    Revision rev = hl.getRevision();
                                    HeadlineSearch hls = new HeadlineSearch();
                                    hls.setId(Integer.toString(hl.getId()));
                                    hls.setBudget_flag(hl.getBudget_flag().toString().toLowerCase());
                                    hls.setPrediction_flag(hl.getPrediction_flag().toString().toLowerCase());
                                    hls.setResource_flag(hl.getResource_flag().toString().toLowerCase());
                                    hls.setSchedule_flag(hl.getSchedule_flag().toString().toLowerCase());
                                    hls.setStatus(hl.getIsActive().toString().toLowerCase().trim());
                                    hls.setStage(hl.getStage().toString().toLowerCase());
                                    hls.setRevision_name(rev.getName());
                                    hls.setRevision_id(rev.getId());
                                    hls.setLast_updated_date(hl.getCreatedDate());
                                    hls.setHeadline(hl.getHeadline());
                                    hlsl.add(hls);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    executor.shutdown();
                    try {
                        executor.awaitTermination(120, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Start to insert indicator headline to ES from " + index);
                    headlineSearchService.saveBulk(hlsl);
                }
                if (dt == null) {
                    stop += MYSQL_PAGE_SIZE;
                    index++;
                } else {
                    stop = count;
                }
            } while (stop < count);
            System.out.println("Done Inserting headline");
        }
        return new AsyncResult<Boolean>(true);

    }

    @Override
    public Future<Boolean> indexAllTemplate(DateTime dt) {
        if (elasticsearchTemplate.indexExists(TemplateSearch.class)) {
            elasticsearchTemplate.deleteIndex(TemplateSearch.class);
            elasticsearchTemplate.createIndex(TemplateSearch.class);
            elasticsearchTemplate.putMapping(TemplateSearch.class);
        }

        List<Template> templates = templateService.listAll();
        if (templates != null && !templates.isEmpty()) {
            List<TemplateSearch> tsl = Collections.synchronizedList(new ArrayList<TemplateSearch>());
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            for (final Template template : templates) {
                executor.submit(new Runnable() {
                    public void run() {
                        TemplateSearch ts = new TemplateSearch();
                        ts.setCreated_date(template.getCreatedDate());
                        ts.setId(Integer.toString(template.getId()));
                        ts.setLast_updated_date(template.getLastUpdatedDate());
                        ts.setName(template.getName().toLowerCase().trim());
                        if (template.getNameInReport().trim().isEmpty()) {
                            ts.setNameInReport(template.getName().toLowerCase().trim());
                        } else {
                            ts.setNameInReport(template.getNameInReport().toLowerCase().trim());
                        }
                        ts.setOnDashboard(template.getOnDashboard());
                        ts.setAvailableCA(template.getAvailableCA());
                        ts.setAvailableECR(template.getAvailableECR());
                        ts.setAvailablePC(template.getAvailablePC());
                        ts.setAvailableTO(template.getAvailableTO());
                        ts.setAvailableCurrent(template.getAvailableCurrent());
                        ts.setIsRestrictedView(template.getIsRestrictedView());
                        ts.setOrderNum(template.getOrderNum());
                        if (template.getType() != null)
                            ts.setType(template.getType().toLowerCase().trim());
                        if (template.getCategory() != null)
                            ts.setCategory(template.getCategory().toLowerCase().trim());
                        if (template.getGroup() != null)
                            ts.setGroup(template.getGroup().toLowerCase().trim());

                        if (template.getGroup().equalsIgnoreCase("project")) {
                            System.out.println(template.getName() + " " + template.getType() + " " + template.getCategory() + " " + template.getGroup());
                        }
                        tsl.add(ts);
                        //System.out.println("Collecting " + template.getId());
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(3, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Done Parsing...\nStart to insert to ES \n");
            templateSearchService.saveBulk(tsl);
        }

        System.out.println("Done Inserting Template");
        return new AsyncResult<Boolean>(true);
    }

    @Override
    public Future<Boolean> checkAndAuditTemplate(String type, String category, String group, Authentication currentAuthentication) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<TemplateSearch> templateSearchList = templateSearchService.findByTypeCategory(type, category, group);
        if (templateSearchList != null && !templateSearchList.isEmpty()) {
            List<RevisionSearch> revisionSearchList = revisionSearchService.findByType(type);
            if (revisionSearchList != null && !revisionSearchList.isEmpty()) {
                for (final RevisionSearch revisionSearch : revisionSearchList) {
                    executor.submit(new Runnable() {
                        public void run() {
                            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                            ctx.setAuthentication(currentAuthentication);
                            SecurityContextHolder.setContext(ctx);
                            Revision revision = revisionService.findById(Integer.parseInt(revisionSearch.getId()));
                            if (revision == null) return;
//                            List<RevisionInformationSearch> revisionInformationSearchList = revisionInformationSearchService.findByRevisionPhaseName(Integer.parseInt(revisionSearch.getId()), "current", "sdk for upcoming release");
//                            for(RevisionInformationSearch revisionInformationSearch : revisionInformationSearchList){
//                                RevisionInformation revisionInformation = revisionInformationService.findById(Integer.parseInt(revisionInformationSearch.getId()));
//                                if(revisionInformation != null)
//                                    revisionInformationService.delete(revisionInformation.getId());
//                                else
//                                    revisionInformationSearchService.delete(revisionInformationSearch.getId());
//                            }
//                            System.out.println(revisionSearch.toString() + " "+revisionInformationSearchList.size());
//                            List<RevisionInformationSearch> revisionInformationSearchList = revisionInformationSearchService.findByRevisionPhaseName(Integer.parseInt(revisionSearch.getId()), "current", "sdk for next fcs");
//                            if(revisionInformationSearchList != null && revisionInformationSearchList.size()>0){
//                                for (RevisionInformationSearch revisionInformationSearch : revisionInformationSearchList){
//                                    RevisionInformation revisionInformation = revisionInformationService.findById(Integer.parseInt(revisionInformationSearch.getId()));
//                                    if(revisionInformation != null){
//                                        revisionInformation.setName("sdk for upcoming release");
//                                        revisionInformationService.saveOrUpdate(revisionInformation);
//                                    }
//                                }
//                            }
//                            System.out.println(revisionSearch.toString() + " "+ revisionInformationSearchList.size());

                            for (TemplateSearch templateSearch : templateSearchList) {
                                List<RevisionInformationSearch> revisionInformationSearchList = revisionInformationSearchService.findByRevisionPhaseName(Integer.parseInt(revisionSearch.getId()), "current", templateSearch.getName());
                                if (revisionInformationSearchList == null || revisionInformationSearchList.isEmpty()) {
                                    System.out.println(revisionSearch.toString());
//                                    try {
//                                        RevisionInformation revisionInformation = new RevisionInformation();
//                                        revisionInformation.setOrderNum(templateSearch.getOrderNum());
//                                        revisionInformation.setName(templateSearch.getName());
//                                        revisionInformation.setValue("");
//                                        revisionInformation.setRevision(revision);
//                                        revisionInformation.setPhase("current");
//                                        revisionInformation.setOnDashboard(templateSearch.getOnDashboard());
//                                        revisionInformation.setIsUserEditable(templateSearch.getAvailableCurrent());
//                                        revisionInformation.setIsRestrictedView(templateSearch.getIsRestrictedView());
//                                        revisionInformation = revisionInformationService.saveOrUpdate(revisionInformation);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
                                } else {
                                    if (revisionInformationSearchList.size() > 1)
                                        System.out.println(revisionSearch.toString());
                                    else {
                                        for (RevisionInformationSearch revisionInformationSearch : revisionInformationSearchList) {
                                            RevisionInformation revisionInformation = revisionInformationService.findById(Integer.parseInt(revisionInformationSearch.getId()));
                                            if (revisionInformation != null) {
                                                revisionInformation.setOrderNum(templateSearch.getOrderNum());
                                                revisionInformationService.saveOrUpdate(revisionInformation);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(3, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        System.out.println("Done Updating Template for " + type);
        return new AsyncResult<Boolean>(true);

    }

}
