package com.broadcom.wbi.service.indicator;

import com.broadcom.wbi.exception.CustomGenericException;
import com.broadcom.wbi.model.elasticSearch.*;
import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.jpa.*;
import com.broadcom.wbi.util.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class IndicatorServiceImpl implements IndicatorService {
    @Autowired
    private RevisionService revisionService;
    @Autowired
    private RevisionIPService revisionIPService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private RevisionInformationSearchService revisionInformationSearchService;
    @Autowired
    private RevisionContactSearchService revisionContactSearchService;
    @Autowired
    private IndicatorTaskSearchService indicatorTaskSearchService;
    @Autowired
    private HeadlineSearchService headlineSearchService;
    @Autowired
    private IndicatorDateSearchService indicatorDateSearchService;
    @Autowired
    private IndicatorGroupSearchService indicatorGroupSearchService;
    @Autowired
    private TemplateSearchService templateSearchService;
    @Autowired
    private IGroupService iGroupService;
    @Autowired
    private IGroupHistoryService iGroupHistoryService;
    @Autowired
    private ITaskService iTaskService;
    @Autowired
    private ITaskHistoryService iTaskHistoryService;
    @Autowired
    private IDateService iDateService;
    @Autowired
    private IDateHistoryService iDateHistoryService;

    /***************************************************************************
     *
     * Get Indicator Milestone
     *
     ***************************************************************************/
    @Override
    public List getIndicatorByCategory(final int gid, final DateTime dt) {
        IndicatorGroupSearch igs = indicatorGroupSearchService.findByGroupId(gid);
        if (igs == null)
            return null;
        IndicatorTaskSearch fcs = null;
        final DateTime currentdt = new DateTime().withTimeAtStartOfDay();
        RevisionSearch rs = revisionSearchService.findById(Integer.toString(igs.getRevision_id()));
        if ((igs.getIgroup_name().toLowerCase().indexOf("project") == 0) && (rs.getProgram_type().equalsIgnoreCase("customer"))) {
            try {
                IndicatorGroupSearch fcsgs = indicatorGroupSearchService.findByRevision(igs.getRevision_id(), "fcs", dt);
                if (fcsgs != null) {
                    List<IndicatorTaskSearch> fcstsl = indicatorTaskSearchService.findByIndicatorGroup(fcsgs.getIgroup_id(), dt);
                    IndicatorDateSearch lastfcsds = null;
                    if (fcstsl != null) {
                        for (IndicatorTaskSearch fcsts : fcstsl) {
                            IndicatorDateSearch ids = indicatorDateSearchService.findByIndicatorTask(fcsts.getTask_id(), "current_end");
                            if (ids != null) {
                                DateTime fcsdt = new DateTime(ids.getValue()).withTimeAtStartOfDay();
                                if (fcsdt.getMillis() > currentdt.getMillis()) {
                                    if (lastfcsds != null) {
                                        DateTime lastfcsdt = new DateTime(lastfcsds.getValue()).withTimeAtStartOfDay();
                                        if (lastfcsdt.getMillis() > fcsdt.getMillis()) {
                                            fcs = fcsts;
                                            lastfcsds = ids;
                                        }
                                    } else {
                                        fcs = fcsts;
                                        lastfcsds = ids;
                                    }
                                } else {
                                    if (lastfcsds != null) {
                                        DateTime lastfcsdt = new DateTime(lastfcsds.getValue()).withTimeAtStartOfDay();
                                        if (lastfcsdt.getMillis() < fcsdt.getMillis()) {
                                            fcs = fcsts;
                                            lastfcsds = ids;
                                        }
                                    } else {
                                        fcs = fcsts;
                                        lastfcsds = ids;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }// end fcstask

        final IndicatorTaskSearch fcsts = fcs;

        List<IndicatorTaskSearch> itsl = indicatorTaskSearchService.findByIndicatorGroup(gid, dt);
        if (itsl != null) {
            ExecutorService executor = Executors.newFixedThreadPool(20);
            final List ret = Collections.synchronizedList(new ArrayList());
            final DateTime lastResetDate = DateResetUtil.getResetDate(dt);
            final LinkedHashMap taskmap = new LinkedHashMap();
            for (final IndicatorTaskSearch task : itsl) {
                executor.submit(new Runnable() {
                    public void run() {
                        if (task.getTask_name().toLowerCase().indexOf("sdk lga") != -1)
                            return;
                        HashMap hm = new HashMap();
                        IndicatorTaskSearch its = task;

                        int orderNum = its.getOrder_num();
                        if (task.getTask_name().trim().toLowerCase().indexOf("fcs") == 0 && fcsts != null) {
                            its = fcsts;
                            orderNum = 100;
                        }

                        if (taskmap.containsKey(its.getTask_name())) {
                            hm = (HashMap) taskmap.get(its.getTask_name());
                        }
                        hm.put("tname", its.getTask_name());
                        hm.put("tnameReport", its.getTask_name_in_report());
                        hm.put("id", its.getTask_id());
                        hm.put("torder", orderNum);
                        hm.put("tnote", "");
                        if (its.getNote() != null && !its.getNote().trim().isEmpty()) {
                            hm.put("tnote", "<span>" + its.getNote() + "</span>");
                        }
                        hm.put("tstatus", its.getStatus());
                        DateTime dttemp = new DateTime(its.getLast_updated_date());
                        if (dttemp.getMillis() < lastResetDate.getMillis()) {
                            hm.put("tstatus", "black");
                        }
                        List<IndicatorDateSearch> idsl = indicatorDateSearchService.findByIndicatorTask(its.getTask_id(), dt);
                        if (idsl != null && !idsl.isEmpty()) {
                            HashMap<String, List<String>> dateCheckMap = new HashMap<String, List<String>>();
                            List datelist = new ArrayList();
                            boolean oldActualDate = false;
                            dLoop:
                            for (IndicatorDateSearch ids : idsl) {
                                List<String> dateCheckList = new ArrayList<String>();
                                if (dateCheckMap.containsKey(ids.getDate_name())) {
                                    dateCheckList = dateCheckMap.get(ids.getDate_name());
                                }
                                HashMap tmpmap = new HashMap();
                                HashMap lastWeekSnapshot = new HashMap();
                                if (hm.containsKey(ids.getDate_name())) {
                                    tmpmap = (HashMap) hm.get(ids.getDate_name());
                                    if (tmpmap.containsKey("MILESTONE")) {
                                        datelist = (List) tmpmap.get("MILESTONE");
                                    }
                                    if (tmpmap.containsKey("LAST_SNAPSHOT")) {
                                        lastWeekSnapshot = (HashMap) tmpmap.get("LAST_SNAPSHOT");
                                    }
                                } else {
                                    datelist = new ArrayList();
                                    if (ids.getComment().trim().toLowerCase().length() > 0) {
                                        String html = ids.getComment().trim().replaceAll("\\<[^>]*>", "").trim();
                                        if (html.length() > 0) {
                                            tmpmap.put("hasComment", true);
                                        }
                                    }
                                }

                                DateTime ddt = new DateTime(ids.getValue()).withZone(DateTimeZone.forID("America/Los_Angeles"));

                                if (ddt.getHourOfDay() > 12) {
                                    ddt = ddt.plusDays(1);
                                }
                                Date dhts = ids.getLast_updated_date();
                                dttemp = new DateTime(dhts);
                                String dstatus = "text-black";

                                if (ddt.getMillis() < currentdt.getMillis()) {
                                    dstatus = "text-grey";
                                } else {
                                    if (ids.getDate_name().equalsIgnoreCase("current_end")) {
                                        dstatus = "text-" + ids.getStatus();
                                    }
                                }

                                if (dttemp.getMillis() < lastResetDate.getMillis()) {
                                    dstatus = "text-black";
                                    if (ddt.isBeforeNow()) {
                                        dstatus = "text-grey";
                                    }
                                }
                                String ds = ddt.toString(dfmt);
                                if (dateCheckList.size() > 0) {
                                    if (ds.equals(dateCheckList.get(dateCheckList.size() - 1))) {
                                        continue dLoop;
                                    }
                                }
                                dateCheckList.add(ds);
                                dateCheckMap.put(ids.getDate_name(), dateCheckList);
                                HashMap dmap = new HashMap();
                                dmap.put("value", DateUtil.toString(ds));
                                dmap.put("ts", dhts);
                                dmap.put("comment", HtmlUtils.htmlUnescape(ids.getComment()).trim());
                                dmap.put("dhstatus", dstatus);
                                datelist.add(dmap);
                                tmpmap.put("MILESTONE", datelist);
                                if (ids.getDate_name().equalsIgnoreCase("current_end")) {
                                    if (dttemp.getMillis() < lastResetDate.getMillis()) {
                                        //get last snapshot
                                        if (lastWeekSnapshot.keySet().size() > 0) {
                                            Date lwd = (Date) lastWeekSnapshot.get("ts");
                                            DateTime lwts = new DateTime(lwd);
                                            if (lwts.getMillis() < dttemp.getMillis()) {
                                                lastWeekSnapshot.putAll(dmap);
                                            }
                                        } else {
                                            lastWeekSnapshot.putAll(dmap);
                                        }
                                        tmpmap.put("LAST_SNAPSHOT", lastWeekSnapshot);
                                    }
                                } else if (ids.getDate_name().equalsIgnoreCase("actual_end")) {
                                    long diff = (currentdt.getMillis() - dttemp.getMillis()) / (1000 * 60 * 60 * 24);
                                    if (diff > 13) {
                                        oldActualDate = true;
                                    }
                                }
                                hm.put(ids.getDate_name(), tmpmap);
                            }
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
            if (ret != null) {
                return ret;
            }
        }
        return null;

    }

    @Override
    public List getFrontPageMilestone(int rid) {
        DateTime currentdt = new DateTime();
        IndicatorGroupSearch igs = indicatorGroupSearchService.findByRevision(rid, "project", currentdt);
        if (igs == null)
            return null;
        List hm = getIndicatorByCategory(igs.getIgroup_id(), currentdt);
        if (hm == null || hm.size() == 0)
            return null;
        List ret = new ArrayList();
        for (Object taskname : hm) {
            Map tmap;
            if (taskname instanceof LinkedHashMap)
                tmap = (LinkedHashMap) taskname;
            else if (taskname instanceof HashMap)
                tmap = (HashMap) taskname;
            else
                continue;
            StringBuilder sb = new StringBuilder();
            dateloop:
            for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                String dname = ttype.toString().toLowerCase() + "_end";
                if (tmap.containsKey(dname)) {
                    Map tmp_dmap;
                    if (tmap.get(dname) instanceof LinkedHashMap)
                        tmp_dmap = (LinkedHashMap) tmap.get(dname);
                    else if (tmap.get(dname) instanceof HashMap)
                        tmp_dmap = (HashMap) tmap.get(dname);
                    else
                        continue;
                    List datelist = new ArrayList();
                    if (tmp_dmap != null && tmp_dmap.containsKey("MILESTONE")) {
                        datelist = (ArrayList) tmp_dmap.get("MILESTONE");
                    }
                    if (datelist.size() > 0) {
                        dlist:
                        for (Object obj : datelist) {
                            Map dmap;
                            if (obj instanceof LinkedHashMap)
                                dmap = (LinkedHashMap) obj;
                            else if (obj instanceof HashMap)
                                dmap = (HashMap) obj;
                            else
                                continue;
                            if (dmap.get("value").toString().trim().isEmpty() || dmap.get("value").toString().indexOf("1980") != -1) {
                                if (dname.indexOf("actual") != -1)
                                    continue dateloop;
                                continue dlist;
                            }
                            sb.append("<span style=\"");
                            String color = CSSColorUtil.getColorClass(dmap.get("value").toString(), dmap.get("dhstatus").toString());
                            sb.append(CSSColorUtil.convertColorClassToCSSString(color) + "\" ");
                            sb.append("title=\"" + HtmlUtils.htmlUnescape(dmap.get("comment").toString()) + "\">");
                            sb.append(dmap.get("value").toString() + "</span>");

                            break dateloop;
                        }
                    }
                }
            }
            if (sb.length() < 2) {
                sb.append("<span>&nbsp;</span>");
            }
            HashMap tmp_hm = new HashMap();
            tmp_hm.put("key", TextUtil.formatName(tmap.get("tname").toString()));
            if (tmap.containsKey("tnameReport") && tmap.get("tnameReport") != null) {
                tmp_hm.put("nameReport", TextUtil.formatName(tmap.get("tnameReport").toString()));
            } else {
                tmp_hm.put("nameReport", TextUtil.formatName(tmap.get("tname").toString()));
            }
            tmp_hm.put("value", sb);
            tmp_hm.put("order", Integer.parseInt(tmap.get("torder").toString().replaceAll("[^0-9]", "")));
            ret.add(tmp_hm);
        }
        if (ret != null && ret.size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public List getFrontPageMilestone(List hm) {
        if (hm == null || hm.size() == 0)
            return null;
        List ret = new ArrayList();
        for (Object taskname : hm) {
            Map tmap;
            if (taskname instanceof LinkedHashMap)
                tmap = (LinkedHashMap) taskname;
            else if (taskname instanceof HashMap)
                tmap = (HashMap) taskname;
            else
                continue;
            StringBuilder sb = new StringBuilder();
            dateloop:
            for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                String dname = ttype.toString().toLowerCase() + "_end";
                if (tmap.containsKey(dname)) {
                    Map tmp_dmap;
                    if (tmap.get(dname) instanceof LinkedHashMap)
                        tmp_dmap = (LinkedHashMap) tmap.get(dname);
                    else if (tmap.get(dname) instanceof HashMap)
                        tmp_dmap = (HashMap) tmap.get(dname);
                    else
                        continue;
                    List datelist = new ArrayList();
                    if (tmp_dmap != null && tmp_dmap.containsKey("MILESTONE")) {
                        datelist = (ArrayList) tmp_dmap.get("MILESTONE");
                    }
                    if (datelist.size() > 0) {
                        dlist:
                        for (Object obj : datelist) {
                            Map dmap;
                            if (obj instanceof LinkedHashMap)
                                dmap = (LinkedHashMap) obj;
                            else if (obj instanceof HashMap)
                                dmap = (HashMap) obj;
                            else
                                continue dlist;

                            if (dmap.get("value").toString().trim().isEmpty() || dmap.get("value").toString().indexOf("1980") != -1) {
                                if (dname.indexOf("actual") != -1)
                                    continue dateloop;
                                continue dlist;
                            }
                            sb.append("<span style=\"");
                            String color = CSSColorUtil.getColorClass(dmap.get("value").toString(), dmap.get("dhstatus").toString());
                            sb.append(CSSColorUtil.convertColorClassToCSSString(color) + "\" ");
                            sb.append("title=\"" + HtmlUtils.htmlUnescape(dmap.get("comment").toString()) + "\">");
                            sb.append(dmap.get("value").toString() + "</span>");

                            break dateloop;
                        }
                    }
                }
            }
            if (sb.length() < 2) {
                sb.append("<span>&nbsp;</span>");
            }
            HashMap tmp_hm = new HashMap();
            tmp_hm.put("key", TextUtil.formatName(tmap.get("tname").toString()));
            if (tmap.containsKey("tnameReport") && tmap.get("tnameReport") != null) {
                tmp_hm.put("nameReport", TextUtil.formatName(tmap.get("tnameReport").toString()));
            } else {
                tmp_hm.put("nameReport", TextUtil.formatName(tmap.get("tname").toString()));
            }
            tmp_hm.put("value", sb);
            tmp_hm.put("order", Integer.parseInt(tmap.get("torder").toString().replaceAll("[^0-9]", "")));
            ret.add(tmp_hm);
        }
        if (ret != null && ret.size() > 0) {
            return ret;
        }
        return null;
    }

    /**************************************************************
     *Project Key Milestone
     *PC - T/O - PRA - ENG SAMPLE
     **************************************************************/
    @Override
    public Map<String, DateTime> getKeyProjectDate(int rid) {
        Map<String, DateTime> ret = new HashMap<String, DateTime>();
        RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
        ret = formatKeyProjectDate(rs);
        if (!rs.getRev_name().equalsIgnoreCase("a0")) {
            if (ret.containsKey("pc")) {
                if (ret.get("pc") == null) {
                    RevisionSearch a0rs = revisionSearchService.findByProgram(rs.getProgram_id(), "a0");
                    Map<String, DateTime> a0map = formatKeyProjectDate(a0rs);
                    if (a0map.containsKey("pc") && a0map.get("pc") != null) {
                        ret.put("pc", a0map.get("pc"));
                    }
                }
            }
        }
        return ret;
    }

    private Map<String, DateTime> formatKeyProjectDate(RevisionSearch rs) {
        Map<String, DateTime> ret = new HashMap<String, DateTime>();
        List frontPageMilestones = getFrontPageMilestone(Integer.parseInt(rs.getId()));
        if (frontPageMilestones != null && !frontPageMilestones.isEmpty()) {
            msloop:
            for (Object mstone : frontPageMilestones) {
                HashMap hm = (HashMap) mstone;
                if (rs.getProgram_type().equalsIgnoreCase("chip")) {
                    String key = "";
                    String value = "";
                    if (hm.containsKey("key")) {
                        key = hm.get("key").toString().toLowerCase();
                    }
                    if (hm.containsKey("value")) {
                        value = hm.get("value").toString().replaceAll("\\<[^>]*>", "");
                        if (value.equalsIgnoreCase("na") ||
                                value.equalsIgnoreCase("tbd") ||
                                value.equalsIgnoreCase("&nbsp;")) {
                            value = "";
                        }
                    }
                    if (!key.trim().isEmpty()) {
                        for (String keyTask : chipKeyMilestones) {
                            if (key.equalsIgnoreCase(keyTask)) {
                                DateTime dt = null;
                                if (!value.isEmpty()) {
                                    dt = DateUtil.toDate(value);
                                    if (dt.getYear() < 2000) dt = null;
                                }
                                ret.put(keyTask, dt);
                                continue msloop;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**************************************************************
     *Segment Page
     **************************************************************/
    @Override
    public HashMap getFrontPageRevisionInfo(int rid) {
        final DateTime currentdt = new DateTime();
        DateTime lastResetDate = DateResetUtil.getResetDate(null);
        RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
        HeadlineSearch hls = headlineSearchService.findByRevision(rid, currentdt);
        IndicatorGroupSearch igs = indicatorGroupSearchService.findByRevision(rid, "project", null);
        HashMap hm = new HashMap();
        hm.put("stage", "");
        hm.put("segment", rs.getSegment());
        hm.put("status", rs.getIs_active().toString());
        hm.put("rid", Integer.parseInt(rs.getId()));
        hm.put("pid", rs.getProgram_id());
        DateTime hlts = new DateTime();
        hm.put("prediction_flag", "black");

        if (hls != null) {
            hlts = new DateTime(hls.getLast_updated_date());
            hm.put("stage", TextUtil.formatName(hls.getStage().toString().replaceAll("_", " ")));
            hm.put("prediction_flag", hls.getPrediction_flag().toLowerCase());
            hm.put("hlts", "<i>Updated on " + hlts.toString(dfmt) + "</i>");
            hm.put("hlts1", "<i>" + hlts.toString(dfmt) + "</i>");
            long diff = (currentdt.getMillis() - hlts.getMillis()) / (1000 * 60 * 60 * 24);
            if (diff > 13) {
                hm.put("hlts", "<i class='text-danger'>Updated on " + hlts.toString(dfmt) + "</i>");
                hm.put("hlts1", "<i class='text-danger'>" + hlts.toString(dfmt) + "</i>");
            }
        } else {
            hm.put("stage", "Non Active");
            hm.put("prediction_flag", "grey");
            hm.put("hlts", "");
            hm.put("hlts1", "");
        }
        if (hlts.getMillis() < lastResetDate.getMillis()) {
            hm.put("prediction_flag", "black");
        }
        Boolean isSoftware = false;
        if (rs.getProgram_type().toLowerCase().indexOf("software") != -1) {
            isSoftware = true;
        }
        if (!rs.getIs_active()) {
            hm.put("status", rs.getIs_active().toString());
            hm.put("prediction_flag", "grey");
            hm.put("stage", "Non Active");
        }

        hm.put("schedule_flag", "black");
        if (igs != null) {
            hm.put("schedule_flag", igs.getStatus());
            DateTime dttemp = new DateTime(igs.getLast_updated_date());
            if (dttemp.getMillis() < lastResetDate.getMillis()) {
                hm.put("schedule_flag", "black");
            }
        }
        String headline = "";
        if (hls != null) {
            headline = hls.getHeadline().trim();
            if (headline.replaceAll("[0-9]", "").trim().length() < 2) {
                headline = "";
            }
            if (headline.length() > 0)
                headline = TextUtil.cleanHeadline(hls.getHeadline().trim());
        }
        if (isSoftware) {
            String hltext = hls.getHeadline().replaceAll("<[^>]*>", "");
            if (!hltext.trim().isEmpty()) {
                headline = hls.getHeadline().replaceAll("&nbsp;", "").replaceAll("<br>", "\\\n");
            }
            hm.put("schedule_flag", hls.getSchedule_flag().toString().toLowerCase());
        }
        if (headline.replaceAll("[0-9]", "").trim().length() < 2) {
            headline = "";
        }

        String viewState = "internalProgram";
        ///get view state for ui click
        if (rs.getProgram_type().equalsIgnoreCase("customer"))
            viewState = "customerProgram";
        else if (rs.getProgram_type().equalsIgnoreCase("software"))
            viewState = "softwareProgram";

        hm.put("headline", headline);
        hm.put("inReport", "y");

        hm.put("bookmark", false);
        hm.put("order", rs.getRev_order_num());
        hm.put("program", TextUtil.formatName(rs.getProgram_name()));
        hm.put("revision", TextUtil.formatName(rs.getRev_name()));
        hm.put("displayName", TextUtil.formatName(rs.getProgram_name().toLowerCase().replaceAll("^program", "").replaceAll("_hidden", "").trim())
                + " " + rs.getRev_name().toUpperCase());
        hm.put("reportName", TextUtil.formatName(rs.getProgram_name().toLowerCase().replaceAll("^program", "").replaceAll("_hidden", "").trim())
                + " " + rs.getRev_name().toUpperCase());
        hm.put("category", TextUtil.formatName(rs.getProgram_type().toString()));
        hm.put("base", TextUtil.formatName(rs.getBase_num()));
        hm.put("rev", generateRevisionURLforView(rs));
        if (rs.getProgram_type().equalsIgnoreCase("software")) {
            int order = rs.getProgram_order_num() * 100;
            order += rs.getRev_order_num();
            if (rs.getRev_name().toLowerCase().indexOf("program") != 0)
                order += 1;
            hm.put("order", order);
            hm.put("revision", rs.getRev_name());
            hm.put("reportName", TextUtil.formatName(rs.getRev_name().toLowerCase().replaceAll("^program", "").trim()));
            hm.put("displayName", generateRevisionURLforView(rs));

        } else if (rs.getProgram_type().equalsIgnoreCase("customer")) {
            hm.put("displayName", TextUtil.formatName(rs.getProgram_name().toLowerCase().replaceAll("^program", "").trim()));
            hm.put("rev", generateRevisionURLforView(rs));
            hm.put("reportName", TextUtil.formatName(rs.getProgram_name().toLowerCase().replaceAll("^program", "").trim()));
            hm.put("sdk_current", "");
            hm.put("sdk_upcoming_release", "");
            hm.put("sdk_fcs", "");
            hm.put("switch_chip", "");
            hm.put("fcs", "");
            List<RevisionInformationSearch> pil = revisionInformationSearchService.findByRevision(Integer.parseInt(rs.getId()), true);
            if (pil != null && !pil.isEmpty()) {
                for (RevisionInformationSearch pi : pil) {
                    if (pi.getName().equalsIgnoreCase("switch chip")) {
                        String chip = pi.getValue().trim();
                        chip = chip.replaceAll("\\([^\\(]*\\)", "");
                        chip = chip.replaceAll("\\s(and|&)\\s", "<br>");
                        chip = chip.replaceAll(",", "<br>");
                        chip = chip.replaceAll("/", "<br>");
                        hm.put("switch_chip", chip.toUpperCase());
                    } else if (pi.getName().equalsIgnoreCase("current sdk")) {
                        hm.put("sdk_current", pi.getValue().toUpperCase());
                    } else if (pi.getName().equalsIgnoreCase("sdk for upcoming release")) {
                        hm.put("sdk_upcoming_release", pi.getValue().toUpperCase());
                    } else if (pi.getName().equalsIgnoreCase("sdk for fcs")) {
                        hm.put("sdk_fcs", pi.getValue().toUpperCase());
                    }
                }
            }
            // get FCS date
            List frontpagemilestonelist = getFrontPageMilestone(rid);
            if (frontpagemilestonelist != null && frontpagemilestonelist.size() > 0) {
                milestoneloop:
                for (Object obj : frontpagemilestonelist) {
                    Map milestonehm = null;
                    if (obj instanceof LinkedHashMap)
                        milestonehm = (LinkedHashMap) obj;
                    else if (obj instanceof HashMap)
                        milestonehm = (HashMap) obj;
                    if (milestonehm.containsKey("key")
                            && milestonehm.get("key").toString().toLowerCase().indexOf("fcs") != -1) {
                        hm.put("fcs", milestonehm.get("value"));
                        break milestoneloop;
                    }
                }
            }
        } else if (rs.getProgram_type().equalsIgnoreCase("ip")) {
            int order = 1;
            if (rs.getRev_name().toLowerCase().indexOf("head") != 0)
                order = 0;
            hm.put("order", order);
            hm.put("revision", rs.getRev_name());
            List<RevisionInformationSearch> pil = revisionInformationSearchService.findByRevision(Integer.parseInt(rs.getId()), true);
            if (pil != null && !pil.isEmpty()) {
                for (RevisionInformationSearch pi : pil) {
                    hm.put(pi.getName().toLowerCase(), TextUtil.formatName(pi.getValue()));
                }
            }
            Revision iprev = revisionService.findById(rid);
            List<RevisionIP> riplist = revisionIPService.findByRevisionIP(iprev);
            if (riplist != null && !riplist.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (RevisionIP rip : riplist) {
                    Revision rev = rip.getRevision();
                    RevisionSearch rs1 = revisionSearchService.findById(Integer.toString(rev.getId()));
                    sb.append("<a ui-sref=\"internalProgram({pid:" + rs1.getProgram_id() + ", rid: " + rs1.getId() + ", page:'dashboard'})\">" +
                            TextUtil.formatName(rs1.getProgram_name()) + " " +
                            rs1.getRev_name().toUpperCase() + "</a>" + " (" + rip.getInstanceNum() + ")<br>");
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 4);
                    hm.put("chip usage", sb);
                }
            }

        } else if (rs.getProgram_type().equalsIgnoreCase("chip")) {
            Map<String, DateTime> tm = getKeyProjectDate(Integer.parseInt(rs.getId()));
            if (tm != null && tm.keySet().size() > 0) {
                StringBuilder sb = new StringBuilder();
                if (hls.getStage().equalsIgnoreCase("planning")
                        || hls.getStage().equalsIgnoreCase("design")) {
                    sb.append("<p><strong>PC</strong> : ");
                    if (tm.containsKey("pc") && tm.get("pc") != null) {
                        DateTime dt = tm.get("pc");
                        sb.append(DateUtil.toString(dt.toDate()) + "</p>");
                    } else {
                        sb.append("</p>");
                    }
                    sb.append("<p><strong>T/O</strong> : ");
                    if (tm.containsKey("t/o") && tm.get("t/o") != null) {
                        DateTime dt = tm.get("t/o");
                        sb.append(DateUtil.toString(dt.toDate()) + "</p>");
                    } else {
                        sb.append("</p>");
                    }
                } else {
                    sb.append("<p><strong>1st SI</strong> : ");
                    if (tm.containsKey("eng sample") && tm.get("eng sample") != null) {
                        DateTime dt = tm.get("eng sample");
                        sb.append(DateUtil.toString(dt.toDate()) + "</p>");
                    } else {
                        sb.append("</p>");
                    }
                    sb.append("<p><strong>Qual Complete</strong> : ");
                    if (tm.containsKey("qual complete") && tm.get("qual complete") != null) {
                        DateTime dt = tm.get("qual complete");
                        sb.append(DateUtil.toString(dt.toDate()) + "</p>");
                    } else {
                        sb.append("</p>");
                    }
                    sb.append("<p><strong>PRA</strong> : ");
                    if (tm.containsKey("pra") && tm.get("pra") != null) {
                        DateTime dt = tm.get("pra");
                        sb.append(DateUtil.toString(dt.toDate()) + "</p>");
                    } else {
                        sb.append("</p>");
                    }
                }
                hm.put("milestone", sb);
            }

        }
        // hm.put("color", rev.getStatus().toString().toLowerCase());
        if (!rs.getIs_active()) {
            hm.put("schedule_flag", "grey");
        }
        hm.put("revision_btn_color", hm.get("schedule_flag"));
        hm.put("escalation_btn_color", hm.get("prediction_flag"));
        if (hm.get("schedule_flag").toString().equalsIgnoreCase("black"))
            hm.put("revision_btn_color", "green");

        if (hm.get("prediction_flag").toString().equalsIgnoreCase("black"))
            hm.put("escalation_btn_color", "green");
        else if (hm.get("prediction_flag").toString().trim().isEmpty())
            hm.put("escalation_btn_color", "green");
        if (!rs.getProgram_type().equalsIgnoreCase("ip")) {
            hm.put("outlookts", new DateTime(rs.getLast_updated_outlook_date()).toString(dfmt));
            if (rs.getOutlook() != null) {
                hm.put("outlook", rs.getOutlook().replaceAll("\\[\\d{2}\\/\\d{2}\\/\\d{2,4}\\]", "")
                        .replaceAll("\\<[^>]*>", "")
                        .replaceAll("\"", ""));
            }
        }
        hm.put("pm", "NA");
        hm.put("reduced_pm", "");
        // get program manager name
        List<String> pmKeys = Arrays.asList("program manager", "cpm", "ipm", "pm");
        RevisionContactSearch pmp = null;
        List<RevisionContactSearch> pmpi = revisionContactSearchService.findByRevision(Integer.parseInt(rs.getId()));
        if (pmpi != null && !pmpi.isEmpty()) {
            pmloop:
            for (RevisionContactSearch pm : pmpi) {
                if (pmKeys.contains(pm.getName().toLowerCase())) {
                    pmp = pm;
                    break pmloop;
                }
            }
        }
        if (pmp != null) {
            StringBuilder reducepm = new StringBuilder();
            StringBuilder employeeStr = new StringBuilder();
            String[] earr = pmp.getValue().split(",");
            for (String st : earr) {
                String name = st.trim();
                String[] arr = name.split("\\s");
                String reducename = arr[0].trim();
                if (arr.length > 1)
                    employeeStr.append(TextUtil.formatName(arr[0].trim() + " " + arr[arr.length - 1].trim()) + "<br>");
                else
                    employeeStr.append(TextUtil.formatName(arr[0].trim()) + "<br>");

                if (reducename.length() > 8)
                    reducename = reducename.substring(0, 4);
                reducepm.append(TextUtil.formatName(reducename) + "<br>");
            }

            hm.put("pm", employeeStr.toString().replaceAll("<br>$", "").replaceAll("\\s+", " ").trim());
            hm.put("reduced_pm", reducepm.toString().replaceAll("<br>$", "").replaceAll("\\s+", " ").trim());
        }
        return hm;

    }

    private String generateRevisionURLforView(RevisionSearch rs) {
        String viewState = "internalProgram";
        ///get view state for ui click
        if (rs.getProgram_type().equalsIgnoreCase("customer"))
            viewState = "customerProgram";
        else if (rs.getProgram_type().equalsIgnoreCase("software"))
            viewState = "softwareProgram";
        else if (rs.getProgram_type().equalsIgnoreCase("ip"))
            viewState = "ipProgram";

        String ret = "<a ui-sref=\"" + viewState + "({pid:" + rs.getProgram_id() + ", rid: " + rs.getId() + ", page:'dashboard'})\">" + rs.getRev_name().toUpperCase() + "</a>";
        if (rs.getProgram_type().equalsIgnoreCase("software")) {
            if (rs.getProgram_name().toLowerCase().indexOf(rs.getRev_name().toLowerCase()) != -1) {
                ret = "<a ui-sref=\"" + viewState + "({pid:" + rs.getProgram_id() + ", rid: " + rs.getId() + ", page:'dashboard'})\">"
                        + TextUtil.formatName(rs.getProgram_name().toLowerCase().replaceAll("^program", "").trim())
                        + "</a>";
            } else if (rs.getRev_name().toLowerCase().indexOf(rs.getProgram_name().toLowerCase()) != -1) {
                ret = "<a ui-sref=\"" + viewState + "({pid:" + rs.getProgram_id() + ", rid: " + rs.getId() + ", page:'dashboard'})\">"
                        + TextUtil.formatName(rs.getRev_name().toLowerCase().replaceAll("^program", "").trim())
                        + "</a>";
            }
        } else if (rs.getProgram_type().equalsIgnoreCase("customer")) {
            ret = "<a ui-sref=\"" + viewState + "({pid:" + rs.getProgram_id() + ", rid: " +
                    rs.getId() + ", page:'dashboard'})\"'>" + TextUtil.formatName(rs.getProgram_name()) + "</a>";
        }
        return ret;
    }

    private String generateProgramURLforView(RevisionSearch rs) {
        return "<a ui-sref=\"internalProgram({pid:" + rs.getProgram_id() + ", rid: " + rs.getId() + ", page:'dashboard'})\">" +
                TextUtil.formatName(rs.getProgram_name()) + " " +
                rs.getRev_name().toUpperCase() + "</a>";
    }

    /**************************************************************
     * Software Indicator
     **************************************************************/
    @Override
    public List getSWHeadlineList(int pid) {
        final List ret = Collections.synchronizedList(new ArrayList());
        List<RevisionSearch> revs = revisionSearchService.findByProgram(pid);
        final DateTime currentdt = new DateTime();
        if (revs != null && !revs.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (final RevisionSearch rev : revs) {
                executor.submit(new Runnable() {
                    public void run() {
                        HeadlineSearch hl = headlineSearchService.findByRevision(Integer.parseInt(rev.getId()), null);
                        HashMap hm = new HashMap();
                        hm.put("id", rev.getId());
                        hm.put("pname", TextUtil.formatName(rev.getRev_name().trim()));
                        hm.put("rname", TextUtil.formatName(rev.getRev_name().trim()));
                        hm.put("color", "grey");
                        hm.put("includeReport", false);
                        hm.put("headProgram", false);
                        hm.put("headline", "");
                        hm.put("includeReport", false);
                        hm.put("color", "grey");
                        hm.put("ts", currentdt.toString(dfmt));
                        if (hl != null) {
                            String hltext = hl.getHeadline().replaceAll("<[^>]*>", "");
                            if (!hltext.trim().isEmpty()) {
                                hm.put("headline", hl.getHeadline().replaceAll("&nbsp;", "").replaceAll("<br>", "\\\n"));
                            }
                            if (rev.getIs_active()) {
                                hm.put("includeReport", true);
                                hm.put("color", hl.getSchedule_flag());
                            }
                            DateTime dt = new DateTime(hl.getLast_updated_date());
                            hm.put("ts", dt.toString(dfmt));
                        }
                        hm.put("order", 2);
                        if (rev.getRev_name().toLowerCase().startsWith("program")) {
                            hm.put("headProgram", true);
                            hm.put("order", 1);
                        }

                        hm.put("ea", "");
                        hm.put("ga", "");

                        if (rev.getIp_related() != null && !rev.getIp_related().trim().isEmpty()) {
                            String[] relatedProgs = rev.getIp_related().trim().split("<br>");
                            if (relatedProgs.length > 0) {
                                for (String rp : relatedProgs) {
                                    if (rp.indexOf(":") != -1) {
                                        String[] rpa = rp.split(":");
                                        if (rp.trim().toLowerCase().indexOf("ea") == 0) {
                                            hm.put("ea", rpa[1].trim());
                                        } else if (rp.trim().toLowerCase().indexOf("ga") == 0) {
                                            hm.put("ga", rpa[1].trim());
                                        }
                                    }
                                }
                            }
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

            if (ret != null && !ret.isEmpty())
                return ret;
        }
        return null;
    }

    @Override
    public void createNewIndicatorFromTemplate(Revision rev, String ptype) {
        List<TemplateSearch> templateSearchList = templateSearchService.findByTypeCategory(ptype.toLowerCase(), "indicator", null);
        if (templateSearchList != null && !templateSearchList.isEmpty()) {
            final HashMap<String, List<TemplateSearch>> templateMap = new HashMap<>();
            if ((templateSearchList != null) && (!templateSearchList.isEmpty())) {
                for (TemplateSearch templateSearch : templateSearchList) {
                    String gt = templateSearch.getGroup();
                    List<TemplateSearch> tmplist = new ArrayList<>();
                    if (templateMap.containsKey(gt)) {
                        tmplist = templateMap.get(gt);
                    }
                    if (!tmplist.contains(templateSearch))
                        tmplist.add(templateSearch);
                    templateMap.put(gt, tmplist);
                }
            }
            final ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;
            final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (final String gt : templateMap.keySet()) {
                executor.submit(new Runnable() {
                    public void run() {
                        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                        ctx.setAuthentication(currentAuthentication);
                        SecurityContextHolder.setContext(ctx);

                        List<TemplateSearch> templates = templateMap.get(gt);
                        if (templates != null && !templates.isEmpty()) {
                            IGroup g = new IGroup();
                            g.setName(gt);
                            int gorder = 1;
                            if (gt.toLowerCase().indexOf("project") == 0) {
                                gorder = 0;
                            }
                            g.setOrderNum(Integer.valueOf(gorder));
                            g.setRevision(rev);
                            g = iGroupService.saveOrUpdate(g);

                            IGroupHistory gh = new IGroupHistory();
                            gh.setIGroup(g);
                            gh.setRemark("");
                            gh.setStatus(status);
                            iGroupHistoryService.saveOrUpdate(gh);

                            for (TemplateSearch template : templates) {
                                ITask t = new ITask();
                                t.setIGroup(g);
                                t.setName(template.getName());
                                t.setNameInReport(template.getNameInReport());
                                t.setOrderNum(template.getOrderNum());
                                t = iTaskService.saveOrUpdate(t);

                                ITaskHistory th = new ITaskHistory();
                                th.setITask(t);
                                th.setNote("");
                                th.setStatus(status);
                                iTaskHistoryService.saveOrUpdate(th);

                                for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                                    for (ProjectConstant.EnumIndicatorEndingDateType etype : ProjectConstant.EnumIndicatorEndingDateType.values()) {
                                        IDate d = new IDate();
                                        d.setEtype(etype);
                                        d.setITask(t);
                                        d.setTtype(ttype);
                                        d = iDateService.saveOrUpdate(d);

                                        IDateHistory dh = new IDateHistory();
                                        dh.setComment("");
                                        dh.setIDate(d);
                                        dh.setStatus(status);
                                        dh.setValue(emptydt.toDate());
                                        iDateHistoryService.saveOrUpdate(dh);
                                    }
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
                throw new CustomGenericException(e.getMessage());
            }
        }
    }

    @Override
    public void cloneIndicatorFromAnotherRevision(Revision oldRev, Revision rev) {
        if ((oldRev == null) || (rev == null))
            return;
        List<IGroup> iGroupList = iGroupService.findByRevision(oldRev, null);
        if (iGroupList != null && !iGroupList.isEmpty()) {
            final ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;
            final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (final IGroup iGroup : iGroupList) {
                executor.submit(new Runnable() {
                    public void run() {
                        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                        ctx.setAuthentication(currentAuthentication);
                        SecurityContextHolder.setContext(ctx);

                        if (iGroup.getName().equalsIgnoreCase("project"))
                            cloneIndicatorFromAnotherCategory(iGroup, rev, currentAuthentication);
                        else
                            cloneAsyncIndicatorFromAnotherCategory(iGroup, rev, currentAuthentication);
                    }
                });

            }
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                throw new CustomGenericException(e.getMessage());
            }
        }
    }

    @Override
    public void cloneAsyncIndicatorFromAnotherCategory(IGroup iGroup, Revision rev, Authentication currentAuthentication) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(currentAuthentication);
        SecurityContextHolder.setContext(ctx);
        final ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;
        IGroup g = new IGroup();
        g.setName(iGroup.getName());
        g.setOrderNum(iGroup.getOrderNum());
        g.setRevision(rev);
        g = iGroupService.saveOrUpdate(g);

        IGroupHistory gh = new IGroupHistory();
        gh.setIGroup(g);
        gh.setRemark("");
        gh.setStatus(status);
        iGroupHistoryService.saveOrUpdate(gh);

        List<ITask> iTaskList = iTaskService.findByGroup(iGroup, null);
        if (iTaskList != null && !iTaskList.isEmpty()) {
            for (ITask iTask : iTaskList) {
                ITaskHistory iTaskHistory = iTaskHistoryService.findByTask(iTask);
                ITask t = new ITask();
                t.setIGroup(g);
                t.setName(iTask.getName());
                t.setNameInReport(iTask.getNameInReport());
                t.setOrderNum(iTask.getOrderNum());
                t = iTaskService.saveOrUpdate(t);

                ITaskHistory th = new ITaskHistory();
                th.setITask(t);
                if (iTaskHistory != null)
                    th.setNote(iTaskHistory.getNote());
                else
                    th.setNote("");
                th.setStatus(status);
                iTaskHistoryService.saveOrUpdate(th);

                for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                    for (ProjectConstant.EnumIndicatorEndingDateType etype : ProjectConstant.EnumIndicatorEndingDateType.values()) {
                        IDate iDate = iDateService.findByTaskAndType(iTask, ttype, etype);
                        DateTime vdt = emptydt;
                        if (iDate != null) {
                            IDateHistory iDateHistory = iDateHistoryService.findByDate(iDate);
                            if (iDateHistory != null) {
                                vdt = new DateTime(iDateHistory.getValue()).withTimeAtStartOfDay();
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
                        iDateHistoryService.saveOrUpdate(dh);
                    }
                }
            }
        }
    }

    @Override
    public void cloneIndicatorFromAnotherCategory(IGroup iGroup, Revision rev, Authentication authentication) {
        final ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(authentication);
        SecurityContextHolder.setContext(ctx);

        IGroup g = new IGroup();
        g.setName(iGroup.getName());
        g.setOrderNum(iGroup.getOrderNum());
        g.setRevision(rev);
        g = iGroupService.saveOrUpdate(g);

        IGroupHistory gh = new IGroupHistory();
        gh.setIGroup(g);
        gh.setRemark("");
        gh.setStatus(status);
        iGroupHistoryService.saveOrUpdate(gh);

        List<ITask> iTaskList = iTaskService.findByGroup(iGroup, null);
        if (iTaskList != null && !iTaskList.isEmpty()) {
            for (ITask iTask : iTaskList) {
                ITaskHistory iTaskHistory = iTaskHistoryService.findByTask(iTask);

                try {
                    ITask t = new ITask();
                    t.setIGroup(g);
                    t.setName(iTask.getName());
                    t.setNameInReport(iTask.getNameInReport());
                    t.setOrderNum(iTask.getOrderNum());
                    t = iTaskService.saveOrUpdate(t);

                    ITaskHistory th = new ITaskHistory();
                    th.setITask(t);
                    if (iTaskHistory != null)
                        th.setNote(iTaskHistory.getNote());
                    else
                        th.setNote("");
                    th.setStatus(status);
                    iTaskHistoryService.saveOrUpdate(th);

                    for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                        for (ProjectConstant.EnumIndicatorEndingDateType etype : ProjectConstant.EnumIndicatorEndingDateType.values()) {
                            IDate iDate = iDateService.findByTaskAndType(iTask, ttype, etype);
                            DateTime vdt = emptydt;
                            if (iDate != null) {
                                IDateHistory iDateHistory = iDateHistoryService.findByDate(iDate);
                                if (iDateHistory != null) {
                                    vdt = new DateTime(iDateHistory.getValue()).withTimeAtStartOfDay();
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
                            iDateHistoryService.saveOrUpdate(dh);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

}
