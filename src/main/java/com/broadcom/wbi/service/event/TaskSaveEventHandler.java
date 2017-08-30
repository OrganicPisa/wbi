package com.broadcom.wbi.service.event;

import com.broadcom.wbi.model.elasticSearch.IndicatorDateSearch;
import com.broadcom.wbi.model.elasticSearch.IndicatorGroupSearch;
import com.broadcom.wbi.model.elasticSearch.IndicatorTaskSearch;
import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.service.elasticSearch.IndicatorDateSearchService;
import com.broadcom.wbi.service.elasticSearch.IndicatorGroupSearchService;
import com.broadcom.wbi.service.elasticSearch.IndicatorTaskSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.*;
import com.broadcom.wbi.util.CSSColorUtil;
import com.broadcom.wbi.util.DateUtil;
import com.broadcom.wbi.util.ProjectConstant;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class TaskSaveEventHandler implements ApplicationListener<TaskWithSameNameSaveEvent> {

    private static final Map<String, ProjectConstant.EnumHeadlineStage> stageTaskMap;

    static {
        Map<String, ProjectConstant.EnumHeadlineStage> map = new HashMap<String, ProjectConstant.EnumHeadlineStage>();
        map.put("PC", ProjectConstant.EnumHeadlineStage.DESIGN);
        map.put("T/O", ProjectConstant.EnumHeadlineStage.FABRICATION);
        map.put("ENG SAMPLE", ProjectConstant.EnumHeadlineStage.VER_QUAL);
        map.put("QUAL COMPLETE", ProjectConstant.EnumHeadlineStage.PRE_PRODUCTION);
        map.put("PRA", ProjectConstant.EnumHeadlineStage.PRA);
        stageTaskMap = Collections.unmodifiableMap(map);
    }

    @Autowired
    private RevisionService revisionService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private IGroupService iGroupService;
    @Autowired
    private IGroupHistoryService iGroupHistoryService;
    @Autowired
    private IndicatorGroupSearchService indicatorGroupSearchService;
    @Autowired
    private ITaskService iTaskService;
    @Autowired
    private ITaskHistoryService iTaskHistoryService;
    @Autowired
    private IndicatorTaskSearchService indicatorTaskSearchService;
    @Autowired
    private IDateService iDateService;
    @Autowired
    private IDateHistoryService iDateHistoryService;
    @Autowired
    private IndicatorDateSearchService indicatorDateSearchService;
    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private RedisCacheRepository redis;

    @Override
    public void onApplicationEvent(TaskWithSameNameSaveEvent taskWithSameNameSaveEvent) {
        Map hm = (HashMap) taskWithSameNameSaveEvent.getSource();
        int rid = 0;
        int tid = 0;
        if (hm.containsKey("rid")) {
            try {
                rid = Integer.parseInt(hm.get("rid").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (rid < 1) {
            return;
        }
        final Revision rev = revisionService.findById(rid);
        if (hm.containsKey("tid")) {
            try {
                tid = Integer.parseInt(hm.get("tid").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tid < 1) {
            return;
        }
        final Integer task_id = tid;
        ProjectConstant.EnumIndicatorStatus tstatus = ProjectConstant.EnumIndicatorStatus.BLACK;
        if (hm.containsKey("tstatus") && hm.get("tstatus").toString().trim().length() > 1) {
            tstatus = ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("tstatus").toString().replaceAll("^t+", "").toUpperCase());
        }
        final ProjectConstant.EnumIndicatorStatus task_status = tstatus;

        ProjectConstant.EnumIndicatorStatus gstatus = ProjectConstant.EnumIndicatorStatus.BLACK;
        if (hm.containsKey("gstatus") && hm.get("gstatus").toString().trim().length() > 1) {
            gstatus = ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("gstatus").toString().replaceAll("^t+", "").toUpperCase());
        }
        String note = "";
        if (hm.containsKey("note")) {
            note = hm.get("note").toString();
        }
        final String task_note = note;
        String name = "";
        if (hm.containsKey("name")) {
            name = hm.get("name").toString();
        }

        final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        List<IndicatorTaskSearch> itsl = indicatorTaskSearchService.findByRevision(rid, name);
        if (itsl != null && !itsl.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            for (final IndicatorTaskSearch its : itsl) {
                executor.submit(new Runnable() {
                    public void run() {
                        if (its.getTask_id() == task_id)
                            return;
                        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                        ctx.setAuthentication(currentAuthentication);
                        SecurityContextHolder.setContext(ctx);

                        ITask t = iTaskService.findById(its.getTask_id());
                        IGroup g = iGroupService.findById(its.getIgroup_id());

                        //save new task if note/status change only
                        if (!its.getNote().equalsIgnoreCase(task_note) ||
                                !its.getStatus().equalsIgnoreCase(task_status.toString().toLowerCase())) {
                            ITaskHistory th = new ITaskHistory();
                            th.setITask(t);
                            th.setNote(task_note);
                            th.setStatus(task_status);
                            th = iTaskHistoryService.saveOrUpdate(th);

                            IndicatorTaskSearch new_its = new IndicatorTaskSearch();
                            new_its.setId(Integer.toString(th.getId()));
                            new_its.setIgroup_id(its.getIgroup_id());
                            new_its.setIgroup_name(its.getIgroup_name());
                            new_its.setLast_updated_date(th.getLastUpdatedDate());
                            new_its.setNote(task_note);
                            new_its.setOrder_num(its.getOrder_num());
                            new_its.setRevision_id(its.getRevision_id());
                            new_its.setRevision_name(its.getRevision_name());
                            new_its.setStatus(task_status.toString().toLowerCase());
                            new_its.setTask_id(its.getTask_id());
                            new_its.setTask_name(its.getTask_name());
                            new_its = indicatorTaskSearchService.saveOrUpdate(new_its);
                        }
                        ProjectConstant.EnumHeadlineStage stage = null;
                        //save date
                        for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                            for (ProjectConstant.EnumIndicatorEndingDateType etype : ProjectConstant.EnumIndicatorEndingDateType.values()) {
                                String date_key = ttype.toString().toLowerCase() + WordUtils.capitalizeFully(etype.toString());
                                if (hm.containsKey(date_key)) {
                                    String datename = ttype.toString().toLowerCase() + "_" + etype.toString().toLowerCase();
                                    HashMap dmap = (HashMap) hm.get(date_key);
                                    String dvalue = dmap.get("value").toString().trim();
                                    String comment = dmap.get("comment").toString();
                                    DateTime ddt = DateUtil.toDate(dvalue).withTimeAtStartOfDay();

                                    ProjectConstant.EnumIndicatorStatus dhstatus = ProjectConstant.EnumIndicatorStatus.BLACK;
                                    ProjectConstant.EnumIndicatorStatus tstatus = task_status;

                                    IndicatorDateSearch ids = indicatorDateSearchService.findByIndicatorTask(t.getId(), datename);
                                    if (ttype.equals(ProjectConstant.EnumIndicatorTrackingDateType.CURRENT) &&
                                            etype.equals(ProjectConstant.EnumIndicatorEndingDateType.END)) {
                                        if (tstatus == null) {
                                            if (dhstatus.equals(ProjectConstant.EnumIndicatorStatus.GREY)) {
                                                tstatus = ProjectConstant.EnumIndicatorStatus.BLACK;
                                            } else {
                                                tstatus = dhstatus;
                                            }
                                        }
                                    }
                                    if (dmap.containsKey("dhstatus") && !dmap.get("dhstatus").toString().trim().isEmpty()) {
                                        dhstatus = ProjectConstant.EnumIndicatorStatus.valueOf(dmap.get("dhstatus")
                                                .toString().toUpperCase());
                                    }
                                    if (ids == null) {
                                        IDate idate = new IDate();
                                        idate.setITask(t);
                                        idate.setTtype(ttype);
                                        idate.setEtype(etype);
                                        idate = iDateService.saveOrUpdate(idate);

                                        IDateHistory idatehistory = new IDateHistory();
                                        idatehistory.setComment(comment);
                                        idatehistory.setIDate(idate);
                                        idatehistory.setStatus(dhstatus);
                                        idatehistory.setValue(ddt.toDate());
                                        idatehistory = iDateHistoryService.saveOrUpdate(idatehistory);

                                        ids = new IndicatorDateSearch();
                                        ids.setId(Integer.toString(idatehistory.getId()));
                                        ids.setDate_id(idate.getId());
                                        ids.setComment(comment);
                                        ids.setDate_name(datename);
                                        ids.setGroup_id(g.getId());
                                        ids.setGroup_name(g.getName().toLowerCase().trim());
                                        ids.setLast_updated_date(idatehistory.getLastUpdatedDate());
                                        ids.setStatus(dhstatus.toString().toLowerCase().trim());
                                        ids.setTask_id(t.getId());
                                        ids.setTask_name(t.getName().toLowerCase().trim());
                                        ids.setValue(ddt.toDate());
                                        indicatorDateSearchService.saveOrUpdate(ids);

                                    } else {
                                        IDate idate = iDateService.findById(ids.getDate_id());
                                        if (idate == null) {
                                            idate = new IDate();
                                            idate.setITask(t);
                                            idate.setTtype(ttype);
                                            idate.setEtype(etype);
                                            idate = iDateService.saveOrUpdate(idate);
                                        }
                                        DateTime d1 = new DateTime(ids.getValue()).withTimeAtStartOfDay();
                                        DateTime d2 = new DateTime(ddt).withTimeAtStartOfDay();
                                        //check to see if 2 day not equal
                                        //if date equal, either comment or status not the same
                                        if (d1.getMillis() != d2.getMillis() || (d1.getMillis() == d2.getMillis() &&
                                                (!ids.getComment().equalsIgnoreCase(comment) ||
                                                        !ids.getStatus().equalsIgnoreCase(dhstatus.toString().toLowerCase())))) {
                                            IDateHistory idatehistory = new IDateHistory();
                                            idatehistory.setComment(comment);
                                            idatehistory.setIDate(idate);
                                            idatehistory.setStatus(dhstatus);
                                            idatehistory.setValue(ddt.toDate());
                                            idatehistory = iDateHistoryService.saveOrUpdate(idatehistory);

                                            if (ids == null)
                                                ids = new IndicatorDateSearch();
                                            ids.setId(Integer.toString(idatehistory.getId()));
                                            ids.setDate_id(idate.getId());
                                            ids.setComment(comment);
                                            ids.setDate_name(datename);
                                            ids.setGroup_id(g.getId());
                                            ids.setGroup_name(g.getName().toLowerCase().trim());
                                            ids.setLast_updated_date(idatehistory.getLastUpdatedDate());
                                            ids.setStatus(dhstatus.toString().toLowerCase().trim());
                                            ids.setTask_id(t.getId());
                                            ids.setTask_name(t.getName().toLowerCase().trim());
                                            ids.setValue(ddt.toDate());
                                            indicatorDateSearchService.saveOrUpdate(ids);
                                        }
                                    }
                                }
                            }
                        }//end date loop

                        //calculate group color here
                        IndicatorGroupSearch igs = indicatorGroupSearchService.findByGroupId(its.getIgroup_id());
                        if (igs != null) {
                            List itsl = indicatorService.getIndicatorByCategory(its.getIgroup_id(), null);
                            ProjectConstant.EnumIndicatorStatus new_igs_status = null;
                            if (itsl != null && !itsl.isEmpty()) {
                                for (Object obj : itsl) {
                                    HashMap hm = (HashMap) obj;
                                    if (hm.containsKey("tstatus")) {
                                        new_igs_status = CSSColorUtil.compareColor(ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("tstatus").toString().toUpperCase()), task_status);
                                    }
                                }
                                if (!igs.getStatus().equalsIgnoreCase(new_igs_status.toString())) {
                                    IGroupHistory gh = new IGroupHistory();
                                    gh.setIGroup(g);
                                    gh.setStatus(new_igs_status);
                                    gh.setRemark(igs.getRemark());
                                    gh = iGroupHistoryService.saveOrUpdate(gh);

                                    IndicatorGroupSearch new_igs = new IndicatorGroupSearch();
                                    new_igs.setId(Integer.toString(gh.getId()));
                                    new_igs.setIgroup_id(igs.getIgroup_id());
                                    new_igs.setIgroup_name(igs.getIgroup_name());
                                    new_igs.setLast_updated_date(gh.getLastUpdatedDate());
                                    new_igs.setOrder_num(igs.getOrder_num());
                                    new_igs.setRemark(igs.getRemark());
                                    new_igs.setStatus(new_igs_status.toString().toLowerCase());
                                    new_igs.setRevision_id(igs.getRevision_id());
                                    new_igs.setRevision_name(igs.getRevision_name());
                                    indicatorGroupSearchService.saveOrUpdate(new_igs);
                                }
                            }

                        }
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Integer pid = 0;
        if (hm.containsKey("pid")) {
            try {
                pid = Integer.parseInt(hm.get("pid").toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String gname = "";
        if (hm.containsKey("group_name")) {
            gname = hm.get("group_name").toString();
        }
        if (gname.equalsIgnoreCase("project")) {
            if (pid > 0) {
                redis.clearCache(pid, "", "program");
            }
        }
    }
}
