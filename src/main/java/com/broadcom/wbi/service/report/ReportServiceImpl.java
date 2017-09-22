package com.broadcom.wbi.service.report;

import com.broadcom.wbi.model.elasticSearch.*;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.SegmentService;
import com.broadcom.wbi.util.*;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ReportServiceImpl implements ReportService {

    private final TemplateSearchService templateSearchService;
    private final RevisionSearchService revisionSearchService;
    private final HeadlineSearchService headlineSearchService;
    private final IndicatorService indicatorService;
    private final SegmentService segmentService;
    private final RevisionInformationSearchService revisionInformationSearchService;
    private final IndicatorGroupSearchService indicatorGroupSearchService;
    private final RevisionContactSearchService revisionContactSearchService;

    @Autowired
    public ReportServiceImpl(TemplateSearchService templateSearchService, RevisionSearchService revisionSearchService, HeadlineSearchService headlineSearchService,
                             IndicatorService indicatorService, SegmentService segmentService, RevisionInformationSearchService revisionInformationSearchService,
                             IndicatorGroupSearchService indicatorGroupSearchService, RevisionContactSearchService revisionContactSearchService) {
        this.templateSearchService = templateSearchService;
        this.revisionSearchService = revisionSearchService;
        this.headlineSearchService = headlineSearchService;
        this.indicatorService = indicatorService;
        this.segmentService = segmentService;
        this.revisionInformationSearchService = revisionInformationSearchService;
        this.indicatorGroupSearchService = indicatorGroupSearchService;
        this.revisionContactSearchService = revisionContactSearchService;
    }

    @Override
    public Map generateMilestoneReport(final ProjectConstant.EnumProgramType ptype, final String statusString) {
        final List<TemplateSearch> templateSearchs = templateSearchService.findByTypeCategory(ptype.toString().toLowerCase(), "indicator", "project");
        final Set<String> templates = new HashSet<>();
        for (TemplateSearch tmpl : templateSearchs) {
            templates.add(tmpl.getName().toLowerCase());
        }
        final DateTime lastResetDate = DateResetUtil.getResetDate(null);
        Map ret = new TreeMap();
        final Map map = Collections.synchronizedMap(new TreeMap());
        final DateTime currentdt = new DateTime();
        List<RevisionSearch> revisions = new ArrayList<>();
        if (statusString.equalsIgnoreCase("all")) {
            revisions = revisionSearchService.findByProgramType(ptype, null);
        } else {
            revisions = revisionSearchService.findByProgramType(ptype, new Boolean(statusString));
        }
        if (revisions == null)
            return null;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
                        if (rs.getProgram_type().equalsIgnoreCase("ip") ||
                                rs.getProgram_type().equalsIgnoreCase("software")) {
                            return;
                        }
                    } else if (ptype.equals(ProjectConstant.EnumProgramType.IP)) {
                        if (rs.getRev_name().toLowerCase().startsWith("head")) {
                            return;
                        }
                        HeadlineSearch hls = headlineSearchService.findByRevision(Integer.parseInt(rs.getId()), currentdt);
                        if (hls != null && hls.getStage().equalsIgnoreCase(ProjectConstant.EnumHeadlineStage.SUSTAINING.toString()))
                            return;
                    }

                    IndicatorGroupSearch igs = indicatorGroupSearchService.findByRevision(Integer.parseInt(rs.getId()), "project", currentdt);
                    if (igs == null)
                        return;
                    String segmentString = rs.getSegment();
                    Segment seg = segmentService.findByName(segmentString);
                    TreeMap revMap = new TreeMap();

                    String kpname = "";
                    if (rs.getProgram_type().equalsIgnoreCase("customer")) {
                        kpname = TextUtil.formatName(rs.getBase_num() + " " + rs.getProgram_name());
                    } else {
                        kpname = TextUtil.formatName(rs.getProgram_name());
                    }
                    String mapkey = seg.getOrderNum() + "_" + seg.getName().toUpperCase() + "&&" + kpname
                            + "&&" + rs.getRev_name().toUpperCase();

                    revMap.put("note",
                            "<td colspan='11' style='padding: 5px;  word-wrap: break-word; border: 1px solid #ddd; border-collapse: collapse;'>&nbsp;</td>");
                    if (igs.getRemark() != null && !igs.getRemark().trim().isEmpty()) {
                        revMap.put("note",
                                "<td colspan='11' style='padding: 5px; word-wrap: break-word; border: 1px solid #ddd; border-collapse: collapse;'>"
                                        + igs.getRemark() + "</td>");
                    }
                    String rcolor = igs.getStatus().toString().toLowerCase();
                    DateTime dttemp = new DateTime(igs.getLast_updated_date());
                    if (dttemp.getMillis() < lastResetDate.getMillis()) {
                        rcolor = "black";
                    }
                    revMap.put("color", rcolor);
                    revMap.put("url", "http://wbi.broadcom.com/program/" + rs.getProgram_type() + "/"
                            + rs.getProgram_id() + "/" + rs.getId() + "/dashboard");
                    List frontPageMilestones = indicatorService.getFrontPageMilestone(Integer.parseInt(rs.getId()));

                    if (frontPageMilestones != null && !frontPageMilestones.isEmpty()) {
                        HashMap milestones = new HashMap();
                        for (Object milestone : frontPageMilestones) {
                            Map tmap = null;
                            if (milestone instanceof LinkedHashMap)
                                tmap = (LinkedHashMap) milestone;
                            else if (milestone instanceof HashMap)
                                tmap = (HashMap) milestone;
                            if (tmap != null) {
                                String rk = tmap.get("key").toString().toLowerCase();
                                String k2 = tmap.get("nameReport").toString().toLowerCase();
                                List<String> kl = new ArrayList<String>();
                                if (k2.indexOf(",") != -1) {
                                    kl = Arrays.asList(k2.split(","));
                                } else {
                                    kl.add(rk);
                                }
                                if (!kl.isEmpty()) {
                                    mloop:
                                    for (String k : kl) {
                                        if (!templates.contains(k))
                                            continue mloop;
                                        String key = k.trim().toUpperCase().replaceAll("\\s", "_");
                                        if (key.toLowerCase().indexOf("fcs") != -1
                                                || key.toLowerCase().indexOf("release") != -1) {
                                            key = "FCS";
                                        }
                                        String value = tmap.get("value").toString();
                                        milestones.put(key, "<td style='padding: 5px; border-collapse: collapse; text-align: center; "
                                                + "vertical-align: middle; width: 7%; border: 1px solid #ddd;'>"
                                                + value + "</td>");
                                    }
                                }

                            }
                        }
                        revMap.put("milestone", milestones);
                    }
                    if (rs.getProgram_type().equalsIgnoreCase("customer")) {
                        revMap.put("sdk_current",
                                "<td style='padding: 5px; border-collapse: collapse; text-align: center; vertical-align: middle; "
                                        + "width: 7%; border: 1px solid #ddd;'>&nbsp;</td>");
                        revMap.put("sdk_fcs",
                                "<td style='padding: 5px;  border-collapse: collapse; text-align: center; vertical-align: middle; "
                                        + "width: 7%; border: 1px solid #ddd;'>&nbsp;</td>");
                        revMap.put("switch_chip",
                                "<td style='padding:1px ; border-collapse: collapse; text-align: left; vertical-align: middle; "
                                        + "width: 7%; border: 1px solid #ddd;'>&nbsp;</td>");
                        List<RevisionInformationSearch> pil = revisionInformationSearchService
                                .findByRevision(Integer.parseInt(rs.getId()));
                        if (pil != null && !pil.isEmpty()) {
                            for (RevisionInformationSearch pi : pil) {
                                if (pi.getName().toLowerCase().indexOf("switch chip") == 0) {
                                    String chip = pi.getValue().trim();
                                    chip = chip.replaceAll("\\([^\\(]*\\)", "");
                                    chip = chip.replaceAll("\\s(and|&)\\s", "<br>");
                                    chip = chip.replaceAll(",", "<br>");
                                    revMap.put("switch_chip",
                                            "<td style='padding: 5px; border-collapse: collapse; text-align: left; "
                                                    + "vertical-align: middle; width: 7%; border: 1px solid #ddd;'>"
                                                    + chip.toUpperCase() + "</td>");
                                } else if (pi.getName().toLowerCase().indexOf("current sdk") == 0) {
                                    revMap.put("sdk_current",
                                            "<td style='padding: 5px; border-collapse: collapse; text-align: center; "
                                                    + "vertical-align: middle; width: 7%; border: 1px solid #ddd;'>"
                                                    + pi.getValue().toUpperCase() + "</td>");
                                } else if (pi.getName().toLowerCase().indexOf("sdk for fcs") == 0) {
                                    revMap.put("sdk_fcs",
                                            "<td style='padding: 5px; border-collapse: collapse; text-align: center; "
                                                    + "vertical-align: middle; width: 7%; border: 1px solid #ddd;'>"
                                                    + pi.getValue().toUpperCase() + "</td>");
                                }
                            }
                        }
                    }
                    map.put(mapkey, revMap);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (map.keySet().size() > 0) {
            for (Object key : map.keySet()) {
                String[] karr = key.toString().split("&&");
                TreeMap segMap = new TreeMap();
                TreeMap progMap = new TreeMap();
                TreeMap progrevMap = new TreeMap();
                Map revMap = (TreeMap) map.get(key);
                if (karr.length == 3) {
                    String pcolor = "black";
                    if (ret.containsKey(karr[0])) {
                        segMap = (TreeMap) ret.get(karr[0]);
                    }
                    if (segMap.containsKey(karr[1])) {
                        progMap = (TreeMap) segMap.get(karr[1]);
                    }

                    if (progMap.containsKey("revision")) {
                        progrevMap = (TreeMap) progMap.get("revision");
                    }

                    String rcolor = revMap.get("color").toString();
                    revMap.put("color", CSSColorUtil.convertColorClassToCSSString(rcolor));
                    progrevMap.put(karr[2], revMap);
                    if (progMap.containsKey("color")) {
                        pcolor = progMap.get("color").toString();
                    }
                    if (!pcolor.equals("red")) {
                        if (rcolor.equals("red")) {
                            pcolor = rcolor;
                        } else {
                            if (pcolor.equals("orange")) {
                                if (!rcolor.equals("orange")) {
                                    pcolor = rcolor;
                                }
                            } else {
                                pcolor = rcolor;
                            }
                        }
                    }
                    progMap.put("revision", progrevMap);
                    progMap.put("color", CSSColorUtil.convertColorClassToCSSString(pcolor));
                    segMap.put(karr[1], progMap);
                    ret.put(karr[0], segMap);
                }
            }
        }
        return ret;

    }

    @Override
    public Map generateHeadlineReport(ProjectConstant.EnumProgramType ptype, String statusString) {
        final Map ret = Collections.synchronizedMap(new TreeMap());
        List<RevisionSearch> revisions = new ArrayList<RevisionSearch>();
        if (statusString.equalsIgnoreCase("all")) {
            revisions = revisionSearchService.findByProgramType(ptype, null);
        } else {
            revisions = revisionSearchService.findByProgramType(ptype, new Boolean(statusString));
        }
        DateTime lastResetDate = DateResetUtil.getResetDate(null);
        if (revisions == null)
            return null;
        final DateTime currentdt = new DateTime();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    if (rs.getProgram_type().equalsIgnoreCase("ip")
                            && rs.getRev_name().toLowerCase().startsWith("head")) {
                        return;
                    }
                    if (ptype.equals(ProjectConstant.EnumProgramType.CHIP) &&
                            rs.getSegment().equalsIgnoreCase("ip"))
                        return;
                    DateTime hlts = new DateTime();

                    HashMap hm = indicatorService.getFrontPageRevisionInfo(Integer.parseInt(rs.getId()));
                    if (!hm.containsKey("milestone")) {
                        hm.put("milestone", "");
                    }
                    hm.put("headProgram", false);
                    if (rs.getProgram_type().equalsIgnoreCase("software")) {
                        if (!rs.getRev_name().toLowerCase().startsWith("program")) {
                            if (!rs.getIp_related().trim().isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(hm.get("reportName"));
                                String[] relatedProgs = rs.getIp_related().trim().split("<br>");
                                if (relatedProgs.length > 0) {
                                    sb.append("<ul>");
                                    for (String rp : relatedProgs) {
                                        if (rp.indexOf(":") != -1) {
                                            String[] rpa = rp.split(":");
                                            if (rp.trim().toLowerCase().indexOf("ea") == 0) {
                                                sb.append("<li>EA: " + rpa[1].trim() + "</li>");
                                            } else if (rp.trim().toLowerCase().indexOf("ga") == 0) {
                                                sb.append("<li>GA: " + rpa[1].trim() + "</li>");
                                            }
                                        }
                                    }
                                }
                                if (sb.length() > rs.getRev_name().length()) {
                                    sb.append("</ul>");
                                }
                                hm.put("reportName", sb.toString().trim());
                            }
                        } else {
                            hm.put("headProgram", true);
                        }
                    } else if (rs.getProgram_type().equalsIgnoreCase("ip")) {
                        if (rs.getRev_name().toLowerCase().startsWith("head")
                                || rs.getProgram_name().toLowerCase().endsWith("hidden")) {
                            hm.put("program", hm.get("program").toString().replaceAll("(?i)_hidden", ""));
                            String rname = hm.get("rev").toString().replaceAll("\\<[^>]*>", "").replaceAll("(?i)head_", "");
                            if (rname.toLowerCase().equals("ip")) {
                                hm.put("reportName", hm.get("reportName").toString().replaceAll("(?i)head_ip", ""));
                                hm.put("displayName", hm.get("displayName").toString().replaceAll("(?i)head_ip", ""));
                                hm.put("revision", hm.get("revision").toString().replaceAll("(?i)head_ip", ""));
                                hm.put("rev", hm.get("rev").toString().replaceAll("(?i)head_", ""));
                            } else {
                                hm.put("reportName", hm.get("reportName").toString().replaceAll("(?i)head_", ""));
                                hm.put("displayName", hm.get("displayName").toString().replaceAll("(?i)head_", ""));
                                hm.put("revision", hm.get("revision").toString().replaceAll("(?i)head_", ""));
                                hm.put("rev", hm.get("rev").toString().replaceAll("(?i)head_", ""));
                            }
                            hm.put("headProgram", true);
                        }
                    }
                    String skey = "";
                    List hll = new ArrayList();
                    if (rs.getProgram_type().equalsIgnoreCase("ip")) {
                        skey = hm.get("category").toString().toUpperCase();
                        if (skey.trim().isEmpty()) return;
                        if (hm.get("category").toString().equalsIgnoreCase("ip")) {
                            skey = hm.get("program").toString().replaceAll("(?i)_hidden", "");
                        }
                    } else {
                        String segmentString = rs.getSegment();
                        Segment seg = segmentService.findByName(segmentString);
                        skey = seg.getOrderNum() + "_" + seg.getName().toUpperCase();
                    }
                    if (ret.containsKey(skey)) {
                        hll = (ArrayList) ret.get(skey);
                    }

                    hm.putAll(hm);
                    hll.add(hm);
                    ret.put(skey, hll);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ret.keySet().size() > 0) {
            return ret;
        }
        return null;

    }

    @Override
    public Map generatePRAReport() {
        ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
        final List<TemplateSearch> templateSearchs = templateSearchService
                .findByTypeCategory(ptype.toString().toLowerCase(), "indicator", "project");
        final Set<String> templates = new HashSet<String>();
        for (TemplateSearch tmpl : templateSearchs) {
            templates.add(tmpl.getName().toLowerCase());
        }
        final Map map = Collections.synchronizedMap(new TreeMap());
        List<RevisionSearch> revisions = revisionSearchService.findByProgramType(ptype, true);
        if (revisions == null)
            return null;
        final DateTime currentdt = new DateTime();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    if (rs.getProgram_type().equalsIgnoreCase("ip") || rs.getProgram_type().equalsIgnoreCase("software")) {
                        return;
                    }
                    TreeMap revMap = new TreeMap();
                    String kpname = TextUtil.formatName(rs.getProgram_name());
                    String mapkey = kpname + "&&" + rs.getRev_name().toUpperCase();
                    List frontPageMilestone = indicatorService.getFrontPageMilestone(Integer.parseInt(rs.getId()));
                    if (frontPageMilestone != null) {
                        HashMap milestones = new HashMap();
                        for (Object milestone : frontPageMilestone) {
                            Map tmap = null;
                            if (milestone instanceof LinkedHashMap)
                                tmap = (LinkedHashMap) milestone;
                            else if (milestone instanceof HashMap)
                                tmap = (HashMap) milestone;
                            if (tmap != null) {
                                if (tmap.containsKey("key")) {
                                    String k = tmap.get("key").toString();
                                    if (k.equalsIgnoreCase("pra") || k.equalsIgnoreCase("t/o")) {
                                        String value = tmap.get("value").toString();
                                        String v = value.replaceAll("<[^>]*>", "");
                                        String m = v;
                                        String y = v;
                                        String quarter = v;
                                        try {
                                            DateTime dt = dfmt.parseDateTime(v);
                                            int month = dt.getMonthOfYear();
                                            int year = dt.getYear();
                                            if (month > 10) {
                                                ++year;
                                                quarter = "Q1";
                                                --year;
                                            } else if (month < 2) {
                                                quarter = "Q1";
                                            } else if (month < 5)
                                                quarter = "Q2";
                                            else if (month < 8)
                                                quarter = "Q3";
                                            else if (month < 11)
                                                quarter = "Q4";
                                            m = Integer.toString(month);
                                            y = Integer.toString(year);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        String key = k.toUpperCase().replaceAll("\\s", "_");

                                        milestones.put(key, v);
                                        milestones.put(key + " Month", m);
                                        milestones.put(key + " Quarter", quarter);
                                        milestones.put(key + " Year", y);

                                    }
                                }
                            }
                        }
                        revMap.put("process", "");
                        List<RevisionInformationSearch> risl = revisionInformationSearchService.findByRevision(Integer.parseInt(rs.getId()), true);
                        if (risl != null && !risl.isEmpty()) {
                            for (RevisionInformationSearch ris : risl) {
                                if (ris.getName().equalsIgnoreCase("process node")
                                        && !ris.getValue().trim().isEmpty()) {
                                    revMap.put("process", ris.getValue().trim().toLowerCase());
                                } else if (ris.getName().equalsIgnoreCase("pra target")) {
                                    milestones.put("PRA Target", ris.getValue().trim().toLowerCase());
                                }
                            }
                        }
                        revMap.put("milestone", milestones);
                    }

                    map.put(mapkey, revMap);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map generateHTOLReport() {
        ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
        final Map map = Collections.synchronizedMap(new TreeMap());
        List<RevisionSearch> revisions = revisionSearchService.findByProgramType(ptype, true);
        if (revisions == null)
            return null;
        final DateTime currentdt = new DateTime();
        Map ret = new TreeMap();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    if (rs.getProgram_type().equalsIgnoreCase("ip") || rs.getProgram_type().equalsIgnoreCase("software")) {
                        return;
                    }
                    String segmentString = rs.getSegment();
                    Segment seg = segmentService.findByName(segmentString);
                    TreeMap revMap = new TreeMap();
                    String kpname = TextUtil.formatName(rs.getProgram_name());
                    String mapkey = seg.getOrderNum() + "_" + seg.getName().toUpperCase() + "&&" + kpname
                            + "&&" + rs.getRev_name().toUpperCase();

                    List frontPageMilestone = indicatorService.getFrontPageMilestone(Integer.parseInt(rs.getId()));
                    if (frontPageMilestone != null) {
                        revMap.put("t/o", "");
                        revMap.put("respin_t/o_target", "");
                        for (Object milestone : frontPageMilestone) {
                            Map tmap = null;
                            if (milestone instanceof LinkedHashMap)
                                tmap = (LinkedHashMap) milestone;
                            else if (milestone instanceof HashMap)
                                tmap = (HashMap) milestone;
                            if (tmap != null) {
                                if (tmap.get("key").toString().equalsIgnoreCase("t/o") || tmap.get("key")
                                        .toString().equalsIgnoreCase("respin t/o target")) {
                                    revMap.put(tmap.get("key").toString().toLowerCase().trim()
                                            .replaceAll("\\s", "_"), tmap.get("value").toString());
                                }
                            }
                        }
                        revMap.put("reduced_pm", "");

                        revMap.put("base_num",
                                rs.getBase_num().toString().replaceAll("(?i)bcm", "").toUpperCase());

                        // get program manager name
                        List<String> pmKeys = Arrays.asList("program manager", "cpm", "ipm", "pm");
                        RevisionContactSearch pmp = null;
                        List<RevisionContactSearch> pmpi = revisionContactSearchService
                                .findByRevision(Integer.parseInt(rs.getId()));
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
                                employeeStr.append(TextUtil.formatName(arr[0].trim() + " " + arr[arr.length - 1].trim()) + "<br>");
                                if (reducename.length() > 8)
                                    reducename = reducename.substring(0, 4);
                                reducepm.append(TextUtil.formatName(reducename) + "<br>");
                            }
                            revMap.put("reduced_pm", reducepm.toString().replaceAll("<br>$", "").replaceAll("\\s+", " ").trim());
                        }
                        HashMap<String, String> infoMap = revisionInformationSearchService
                                .findLatestByRevision(Integer.parseInt(rs.getId()));
                        for (String key : infoMap.keySet()) {
                            String k = key.replaceAll("\\(.+?\\)", "").trim();
                            if (k.equalsIgnoreCase("htolpower") || k.equalsIgnoreCase("processnode")
                                    || k.equalsIgnoreCase("package") || k.equalsIgnoreCase("maxpower")) {
                                revMap.put(k, infoMap.get(key));
                            }
                        }
                    }
                    map.put(mapkey, revMap);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (map.keySet().size() > 0) {
            for (Object key : map.keySet()) {
                String[] karr = key.toString().split("&&");
                TreeMap segMap = new TreeMap();
                TreeMap progMap = new TreeMap();
                TreeMap progrevMap = new TreeMap();
                Map revMap = (TreeMap) map.get(key);
                if (karr.length == 3) {
                    if (ret.containsKey(karr[0])) {
                        segMap = (TreeMap) ret.get(karr[0]);
                    }
                    if (segMap.containsKey(karr[1])) {
                        progMap = (TreeMap) segMap.get(karr[1]);
                    }

                    if (progMap.containsKey("revision")) {
                        progrevMap = (TreeMap) progMap.get("revision");
                    }

                    progrevMap.put(karr[2], revMap);
                    progMap.put("revision", progrevMap);
                    segMap.put(karr[1], progMap);
                    ret.put(karr[0], segMap);
                }
            }
        }

        if (ret.keySet().size() > 0) {
            return ret;
        }
        return null;
    }

    @Override
    public Map generateInformationReport(ProjectConstant.EnumProgramType ptype) {
        final List dataList = Collections.synchronizedList(new ArrayList());
        List<RevisionSearch> revisions = revisionSearchService.findByProgramType(ptype, true);
        if (revisions == null)
            return null;
        LinkedHashSet<String> headerSet = new LinkedHashSet();
        if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
            headerSet.add("ca");
            headerSet.add("pc");
            headerSet.add("ecr1");
            headerSet.add("ecr2");
            headerSet.add("ecr3");
        }
        headerSet.add("current");
        if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
            headerSet.add("to/final");
        }
        List keyList = new ArrayList();
        HashMap hm = new HashMap();
        Set<String> keys = new LinkedHashSet<String>();
        if (ptype.equals(ProjectConstant.EnumProgramType.CUSTOMER)) {
            keys.add("customer");
            hm = new HashMap();
            hm.put("field", "customer");
            hm.put("name", "customer");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("program");
            hm = new HashMap();
            hm.put("field", "program");
            hm.put("name", "program");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);
        } else if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
            keys.add("program");
            hm = new HashMap();
            hm.put("field", "program");
            hm.put("name", "chip");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("revision");
            hm = new HashMap();
            hm.put("field", "revision");
            hm.put("name", "rev");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "3%");
            keyList.add(hm);

            keys.add("base die");
            hm = new HashMap();
            hm.put("field", "basedie");
            hm.put("name", "base die");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("status");
            hm = new HashMap();
            hm.put("field", "status");
            hm.put("name", "s");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");

            keyList.add(hm);

            keys.add("ca");
            hm = new HashMap();
            hm.put("field", "ca");
            hm.put("name", "ca");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);

            keys.add("pc");
            hm = new HashMap();
            hm.put("field", "pc");
            hm.put("name", "pc");
            hm.put("cellTemplate", "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
            hm.put("width", "5%");
            keyList.add(hm);
        }
        RevisionSearch rev = revisions.get(0);
        if (rev != null) {
            List<RevisionInformationSearch> riList = revisionInformationSearchService.findByRevision(Integer.parseInt(rev.getId()));
            for (RevisionInformationSearch ris : riList) {
                String key = ris.getName().toLowerCase().trim().replaceAll("\\(.*\\)", "").trim();
                if (!keys.contains(key) && !key.equalsIgnoreCase("code name")
                        && !key.equalsIgnoreCase("program aka")) {
                    keys.add(key);
                    HashMap map = new HashMap();
                    map.put("field", key.replaceAll("\\s", ""));
                    map.put("name", key);
                    map.put("cellTemplate",
                            "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>");
                    if (key.equalsIgnoreCase("to date"))
                        hm.put("width", "5%");
                    keyList.add(map);
                }
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    LinkedHashMap revhm = new LinkedHashMap();
                    HashMap hm = new HashMap();
                    if (ptype.equals(ProjectConstant.EnumProgramType.CUSTOMER)) {
                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getBase_num()));
                        revhm.put("customer", hm);
                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getProgram_name()));
                        revhm.put("program", hm);
                    } else if (ptype.equals(ProjectConstant.EnumProgramType.CHIP)) {
                        String status = "Inactive";
                        hm.put("program", TextUtil.formatName(rs.getProgram_name()));
                        hm.put("revision", rs.getRev_name().toUpperCase());
                        hm.put("basedie", TextUtil.formatName(rs.getBase_num()));

                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getBase_num()));
                        revhm.put("basedie", hm);
                        hm = new HashMap();
                        hm.put("latest", rs.getRev_name().toUpperCase());
                        revhm.put("revision", hm);
                        hm = new HashMap();
                        hm.put("latest", TextUtil.formatName(rs.getProgram_name()));
                        revhm.put("program", hm);

                        HeadlineSearch hls = headlineSearchService.findByRevision(Integer.parseInt(rs.getId()), null);
                        if (hls != null) {
                            if (hls.getStatus().toLowerCase().indexOf("active") == 0) {
                                status = "Active";
                            }
                        }
                        hm = new HashMap();
                        hm.put("latest", status);
                        revhm.put("status", hm);

                        List frontPageMilestone = indicatorService.getFrontPageMilestone(Integer.parseInt(rs.getId()));
                        if (frontPageMilestone != null) {
                            for (Object milestone : frontPageMilestone) {
                                Map tmap = null;
                                if (milestone instanceof LinkedHashMap)
                                    tmap = (LinkedHashMap) milestone;
                                else if (milestone instanceof HashMap)
                                    tmap = (HashMap) milestone;
                                if (tmap != null) {
                                    if (tmap.get("key").toString().equalsIgnoreCase("t/o")) {
                                        String value = tmap.get("key").toString().replaceAll("\\<[^>]*>", "");
                                        hm = new HashMap();
                                        hm.put("latest", value);
                                        revhm.put(tmap.get("key").toString(), hm);
                                    }
                                }
                            }
                        }
                    }

                    hm = new HashMap();
                    hm.put("latest", rs.getSegment().toUpperCase());
                    revhm.put("segment", hm);

                    LinkedHashMap infomap = revisionInformationSearchService.getRevisionInformationReport(Integer.parseInt(rs.getId()), "detail");
                    if (infomap.containsKey("data")) {
                        HashMap map = (HashMap) infomap.get("data");
                        Boolean ecr1 = Boolean.valueOf(infomap.get("displayecr1").toString());
                        Boolean ecr2 = Boolean.valueOf(infomap.get("displayecr2").toString());
                        Boolean ecr3 = Boolean.valueOf(infomap.get("displayecr3").toString());
                        for (Object k : map.keySet()) {
                            String key = k.toString().toLowerCase().replaceAll("\\(.*\\)", "")
                                    .replaceAll("\\s", "").trim();
                            hm = new HashMap();
                            if (revhm.containsKey(key))
                                hm = (HashMap) revhm.get(key);
                            HashMap fieldmap = (HashMap) map.get(k);
                            floop:
                            for (Object f : fieldmap.keySet()) {
                                String field = f.toString().toLowerCase();
                                HashMap fielddata = (HashMap) fieldmap.get(f);
                                String value = fielddata.get("value").toString().trim();
                                if (field.indexOf("ecr") != -1) {
                                    if (ecr1 || ecr2 || ecr3) {
                                        field = "ecr";
                                    } else
                                        continue floop;
                                }
                                if (!hm.containsKey(field) || (hm.containsKey(field) && !value.isEmpty()))
                                    hm.put(field, value);
                                if (!hm.containsKey("latest")
                                        || (hm.containsKey("latest") && !value.isEmpty()))
                                    hm.put("latest", value);
                            }
                            revhm.put(key, hm);
                        }
                        dataList.add(revhm);
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

        if (dataList.size() > 0) {
            Map ret = new HashMap();
            ret.put("title", keyList);
            ret.put("data", dataList);
            ret.put("keys", keys);
            return ret;
        }
        return null;

    }

    @Override
    public void generateCustomerHeadlineWeekendEmail(String includeCustomer, String excludeCustomer, String email) {
        ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CUSTOMER;
        final Map ret = Collections.synchronizedMap(new TreeMap());
        List<RevisionSearch> revisions = revisionSearchService.findByProgramType(ptype, true);
        if (revisions == null)
            return;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    if (includeCustomer != null &&
                            !includeCustomer.isEmpty() &&
                            rs.getBase_num().toLowerCase().indexOf(includeCustomer.trim().toLowerCase()) == -1) {
                        return;
                    }
                    if (excludeCustomer != null &&
                            !excludeCustomer.isEmpty() &&
                            rs.getBase_num().toLowerCase().indexOf(excludeCustomer.trim().toLowerCase()) != -1) {
                        return;
                    }
                    List stageList = new ArrayList();
                    HashMap hm = indicatorService.getFrontPageRevisionInfo(Integer.parseInt(rs.getId()));
                    if (hm.containsKey("stage")) {
                        String stagekey = getCorrectStageHeadlineName(WordUtils.capitalizeFully(hm.get("stage").toString()));
                        if (ret.containsKey(stagekey)) {
                            stageList = (ArrayList) ret.get(stagekey);
                        }
                        String hl = "";
                        if (hm.containsKey("headline")) {
                            hl = hm.get("headline").toString().replaceAll("\\<[^>]*>", "");
                        }
                        stageList.add("<li><b>" + TextUtil.formatName(rs.getBase_num() + " " + rs.getProgram_name())
                                + " </b>[" + hm.get("hlts") + "]: " + hl + "</li>");
                        Collections.sort(stageList);
                        ret.put(stagekey, stageList);
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
        if (ret.keySet().size() > 0) {
            SortedSet<String> keys = new TreeSet(ret.keySet());
            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                String[] arrKey = key.split("\\_");
                sb.append("<u>" + arrKey[1] + ": </u><ul>");
                List list = (ArrayList) ret.get(key);
                if ((list != null) && (!list.isEmpty())) {
                    for (Object l : list) {
                        sb.append(l.toString());
                    }
                }
                sb.append("</ul>");
            }
            String title = WordUtils.capitalizeFully(ptype.toString()) + " Manager Headline Report";
            if (includeCustomer != null && !includeCustomer.isEmpty())
                title = WordUtils.capitalizeFully(includeCustomer + " " + ptype.toString()) + " Manager Headline Report";
            else if (excludeCustomer != null && !excludeCustomer.isEmpty())
                title = WordUtils.capitalizeFully("Non " + excludeCustomer + " " + ptype.toString()) + " Manager Headline Report";


            email = "vinh.tran@broadcom.com";
            EmailUtil.sendEmail(email, email, title, sb.toString());
        }
    }

    @Override
    public void generateInternalHeadlineWeekendEmail(String email) {
        ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
        final Map ret = Collections.synchronizedMap(new TreeMap());
        List<RevisionSearch> revisions = revisionSearchService.findByProgramType(ptype, true);
        if (revisions == null)
            return;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (final RevisionSearch rs : revisions) {
            executor.submit(new Runnable() {
                public void run() {
                    List stageList = new ArrayList();
                    if (rs.getProgram_type().equalsIgnoreCase("ip") && rs.getRev_name().toLowerCase().startsWith("head")) {
                        return;
                    }
                    HashMap hm = indicatorService.getFrontPageRevisionInfo(Integer.parseInt(rs.getId()));
                    if (hm.containsKey("stage")) {
                        String stagekey = getCorrectStageHeadlineName(WordUtils.capitalizeFully(hm.get("stage").toString()));
                        if (rs.getProgram_type().equalsIgnoreCase("ip")) {
                            stagekey = "9_IP";
                        }
                        if (ret.containsKey(stagekey)) {
                            stageList = (ArrayList) ret.get(stagekey);
                        }
                        String hl = "";
                        if (hm.containsKey("headline")) {
                            hl = hm.get("headline").toString().replaceAll("\\<[^>]*>", "");
                        }
                        stageList.add("<li><b>" + TextUtil.formatName(rs.getProgram_name()) + " ("
                                + rs.getRev_name().toUpperCase() + ") </b>[" + hm.get("hlts") + "]: " + hl
                                + "</li>");
                        Collections.sort(stageList);
                        ret.put(stagekey, stageList);
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
        if (ret.keySet().size() > 0) {
            SortedSet<String> keys = new TreeSet(ret.keySet());
            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                String[] arrKey = key.split("\\_");
                sb.append("<u>" + arrKey[1] + ": </u><ul>");
                List list = (ArrayList) ret.get(key);
                if ((list != null) && (!list.isEmpty())) {
                    for (Object l : list) {
                        sb.append(l.toString());
                    }
                }
                sb.append("</ul>");
            }
            String title = WordUtils.capitalizeFully(ptype.toString()) + " Manager Headline Report";


            email = "vinh.tran@broadcom.com";
            EmailUtil.sendEmail(email, email, title, sb.toString());
        }
    }

    private String getCorrectStageHeadlineName(String name) {
        if (name.toLowerCase().indexOf("planning") != -1) {
            name = "1_Planning";
        } else if (name.toLowerCase().indexOf("design") != -1) {
            name = "2_Design";
        } else if (name.toLowerCase().indexOf("qual") != -1) {
            name = "3_Ver/Qual";
        } else if (name.toLowerCase().indexOf("fab") != -1) {
            name = "4_Fabrication";
        } else if (name.toLowerCase().indexOf("pre") != -1) {
            name = "5_Pre Production";
        } else if (name.toLowerCase().indexOf("pra") != -1) {
            name = "6_PRA";
        } else if (name.toLowerCase().indexOf("customer") != -1) {
            name = "7_Customer";
        } else if (name.toLowerCase().indexOf("software") != -1) {
            name = "8_Software";
        } else if (name.toLowerCase().indexOf("ip") == 0) {
            name = "9_IP";
        }
        return name;
    }
}
