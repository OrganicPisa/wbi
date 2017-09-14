package com.broadcom.wbi.service.event;

import com.broadcom.wbi.exception.CustomGenericException;
import com.broadcom.wbi.model.elasticSearch.IndicatorDateSearch;
import com.broadcom.wbi.model.elasticSearch.IndicatorGroupSearch;
import com.broadcom.wbi.model.elasticSearch.IndicatorTaskSearch;
import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.*;
import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class CreateNewIndicatorEventHandler implements ApplicationListener<CreateNewIndicatorEvent> {

    public static final DateTime blankdt = new DateTime().withYear(1980).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
    public static final DateTime emptydt = new DateTime().withYear(1980).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
    public static final DateTime tbddt = new DateTime().withYear(1960).dayOfYear().withMinimumValue().withTimeAtStartOfDay();
    public static final DateTime nadt = new DateTime().withYear(1950).dayOfYear().withMinimumValue().withTimeAtStartOfDay();

    public static final DateTime checkEmptydt = new DateTime().withYear(2003).withMonthOfYear(3).withDayOfMonth(3).withTimeAtStartOfDay();
    public static final DateTime checkTBDdt = new DateTime().withYear(2002).withMonthOfYear(2).withDayOfMonth(2).withTimeAtStartOfDay();
    public static final DateTime checkNAdt = new DateTime().withYear(2001).dayOfYear().withMinimumValue().withTimeAtStartOfDay();

    private final RevisionService revisionService;
    private final RevisionSearchService revisionSearchService;
    private final IGroupService iGroupService;
    private final IGroupHistoryService iGroupHistoryService;
    private final IndicatorGroupSearchService indicatorGroupSearchService;
    private final ITaskService iTaskService;
    private final ITaskHistoryService iTaskHistoryService;
    private final IndicatorTaskSearchService indicatorTaskSearchService;
    private final IDateService iDateService;
    private final IDateHistoryService iDateHistoryService;
    private final IndicatorDateSearchService indicatorDateSearchService;
    private final IndicatorService indicatorService;
    private final RedisCacheRepository redis;
    private final TemplateSearchService templateSearchService;

    @Autowired
    public CreateNewIndicatorEventHandler(IGroupHistoryService iGroupHistoryService, RevisionService revisionService, RedisCacheRepository redis, IndicatorService indicatorService,
                                          RevisionSearchService revisionSearchService, IGroupService iGroupService, IndicatorGroupSearchService indicatorGroupSearchService,
                                          ITaskService iTaskService, ITaskHistoryService iTaskHistoryService, IndicatorTaskSearchService indicatorTaskSearchService,
                                          IDateService iDateService, IDateHistoryService iDateHistoryService, IndicatorDateSearchService indicatorDateSearchService,
                                          TemplateSearchService templateSearchService) {
        this.iGroupHistoryService = iGroupHistoryService;
        this.revisionService = revisionService;
        this.redis = redis;
        this.indicatorService = indicatorService;
        this.revisionSearchService = revisionSearchService;
        this.iGroupService = iGroupService;
        this.indicatorGroupSearchService = indicatorGroupSearchService;
        this.iTaskService = iTaskService;
        this.iTaskHistoryService = iTaskHistoryService;
        this.indicatorTaskSearchService = indicatorTaskSearchService;
        this.iDateService = iDateService;
        this.iDateHistoryService = iDateHistoryService;
        this.indicatorDateSearchService = indicatorDateSearchService;
        this.templateSearchService = templateSearchService;
    }

    @Override
    public void onApplicationEvent(CreateNewIndicatorEvent createNewIndicatorEvent) {
        Map hm = (HashMap) createNewIndicatorEvent.getSource();
        if (hm.containsKey("")) {

        }

    }

    //copy from 1 to 2
    private void cloneRevision(final Revision rev1, final Revision rev2) {
        if ((rev1 == null) || (rev2 == null))
            return;
        DateTime currentdt = new DateTime();
        List<IGroup> iGroupList = iGroupService.findByRevision(rev1, null);
        ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;
        if (iGroupList != null && !iGroupList.isEmpty()) {
            for (IGroup ig : iGroupList) {
                IGroup g = new IGroup();
                g.setName(ig.getName());
                g.setOrderNum(ig.getOrderNum());
                g.setRevision(rev2);
                g = iGroupService.saveOrUpdate(g);

                IGroupHistory gh = new IGroupHistory();
                gh.setIGroup(g);
                gh.setRemark("");
                gh.setStatus(status);
                gh = iGroupHistoryService.saveOrUpdate(gh);

                IndicatorGroupSearch new_igs = new IndicatorGroupSearch();
                new_igs.setId(Integer.toString(gh.getId().intValue()));
                new_igs.setIgroup_name(g.getName().toLowerCase());
                new_igs.setIgroup_id(g.getId());
                new_igs.setOrder_num(g.getOrderNum());
                new_igs.setRevision_id(rev2.getId());
                new_igs.setRevision_name(rev2.getName().toLowerCase());
                new_igs.setStatus(status.toString().toLowerCase());
                new_igs.setRemark("");
                new_igs.setLast_updated_date(gh.getLastUpdatedDate());
                new_igs = indicatorGroupSearchService.saveOrUpdate(new_igs);
                cloneCategoryMilestone(rev2, ig, g);
            }
        }

    }

    private void cloneTemplate(Revision rev) {

    }

    private void cloneCategoryMilestone(final Revision rev, final IGroup g1, final IGroup g2) {
        if (g1 == null || g2 == null) return;
        List<ITask> iTaskList = iTaskService.findByGroup(g1, null);
        if (iTaskList != null && !iTaskList.isEmpty()) {
            final List<IndicatorDateSearch> indicatorDateSearchList = Collections.synchronizedList(new ArrayList<>());

            final ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;
            ExecutorService executor = Executors.newFixedThreadPool(20);
            for (final ITask iTask : iTaskList) {
                executor.submit(new Runnable() {
                    public void run() {
                        ITaskHistory iTaskHistory = iTaskHistoryService.findByTask(iTask);
                        ITask t = new ITask();
                        t.setIGroup(g2);
                        t.setName(iTask.getName());
                        t.setOrderNum(iTask.getOrderNum());
                        t = iTaskService.saveOrUpdate(t);

                        ITaskHistory th = new ITaskHistory();
                        th.setITask(t);
                        if (iTaskHistory != null)
                            th.setNote(iTaskHistory.getNote());
                        else
                            th.setNote("");
                        th.setStatus(status);
                        th = iTaskHistoryService.saveOrUpdate(th);

                        IndicatorTaskSearch new_its = new IndicatorTaskSearch();
                        new_its.setId(Integer.toString(th.getId().intValue()));
                        new_its.setTask_id(t.getId());
                        new_its.setOrder_num(t.getOrderNum());
                        new_its.setRevision_id(rev.getId());
                        new_its.setRevision_name(rev.getName().toLowerCase());
                        new_its.setIgroup_name(g2.getName().toLowerCase());
                        new_its.setIgroup_id(g2.getId());
                        new_its.setLast_updated_date(th.getLastUpdatedDate());
                        new_its.setStatus(status.toString().toLowerCase());
                        new_its.setNote(th.getNote());
                        new_its.setTask_name(t.getName().toLowerCase());
                        new_its = indicatorTaskSearchService.saveOrUpdate(new_its);

                        for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                            for (ProjectConstant.EnumIndicatorEndingDateType etype : ProjectConstant.EnumIndicatorEndingDateType.values()) {
                                IDate idate = iDateService.findByTaskAndType(t, ttype, etype);
                                DateTime vdt = emptydt;
                                if (idate != null) {
                                    IDateHistory idh = iDateHistoryService.findByDate(idate);
                                    if (idh != null) {
                                        vdt = new DateTime(idh.getValue()).withTimeAtStartOfDay();
                                        if (vdt.getYear() == 1950)
                                            vdt = nadt;
                                        else if ((vdt.getYear() > 1950) && (vdt.getYear() < 2000))
                                            vdt = emptydt;
                                    }
                                }
                                IDate d = new IDate();
                                d.setEtype(etype);
                                d.setITask(t);
                                d.setTtype(ttype);
                                d = iDateService.saveOrUpdate(d);

                                IDateHistory dh = new IDateHistory();
                                dh.setIDate(d);
                                dh.setComment("");
                                dh.setStatus(status);
                                dh.setValue(vdt.toDate());
                                dh = iDateHistoryService.saveOrUpdate(dh);

                                String datename = d.getTtype().toString().toLowerCase() + "_" + d.getEtype().toString().toLowerCase();
                                IndicatorDateSearch new_ids = new IndicatorDateSearch();
                                new_ids.setDate_id(d.getId());
                                new_ids.setDate_name(datename);
                                new_ids.setGroup_id(g2.getId());
                                new_ids.setGroup_name(g2.getName().toLowerCase());
                                new_ids.setTask_id(t.getId());
                                new_ids.setTask_name(t.getName().toLowerCase());
                                new_ids.setComment("");
                                new_ids.setValue(vdt.toDate());
                                new_ids.setLast_updated_date(dh.getLastUpdatedDate());
                                new_ids.setStatus(status.toString().toLowerCase());
                                indicatorDateSearchList.add(new_ids);
                            }
                        }
                    }

                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                throw new CustomGenericException(e.getMessage());
            }
            if (indicatorDateSearchList != null && !indicatorDateSearchList.isEmpty())
                indicatorDateSearchService.saveBulk(indicatorDateSearchList);
        }
    }
}
