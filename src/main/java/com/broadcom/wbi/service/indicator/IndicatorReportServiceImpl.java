package com.broadcom.wbi.service.indicator;

import com.broadcom.wbi.model.elasticSearch.*;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.jpa.SegmentService;
import com.broadcom.wbi.util.CSSColorUtil;
import com.broadcom.wbi.util.DateResetUtil;
import com.broadcom.wbi.util.ProjectConstant;
import com.broadcom.wbi.util.TextUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class IndicatorReportServiceImpl implements IndicatorReportService {

    @Autowired
    private TemplateSearchService templateSearchService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private HeadlineSearchService headlineSearchService;
    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private SegmentService segmentService;
    @Autowired
    private RevisionInformationSearchService revisionInformationSearchService;
    @Autowired
    private IndicatorGroupSearchService indicatorGroupSearchService;

    @Override
    public Map generateMilestoneReport(final ProjectConstant.EnumProgramType ptype, final String statusString) {
        final List<TemplateSearch> templateSearchs = templateSearchService.findByTypeCategory(ptype.toString().toLowerCase(),
                "indicator", "project");
        final Set<String> templates = new HashSet<String>();
        for (TemplateSearch tmpl : templateSearchs) {
            templates.add(tmpl.getName().toLowerCase());
        }
        final DateTime lastResetDate = DateResetUtil.getResetDate(null);
        Map ret = new TreeMap();
        final Map map = Collections.synchronizedMap(new TreeMap());
        final DateTime currentdt = new DateTime();
        List<RevisionSearch> revisions = new ArrayList<RevisionSearch>();
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
                        if (rs.getType().equalsIgnoreCase("ip") ||
                                rs.getType().equalsIgnoreCase("software")) {
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
                    if (rs.getType().equalsIgnoreCase("customer")) {
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
                    revMap.put("url", "http://wbi.broadcom.com/program/" + rs.getType() + "/"
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
                    if (rs.getType().equalsIgnoreCase("customer")) {
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
                    if (rs.getType().equalsIgnoreCase("ip")
                            && rs.getRev_name().toLowerCase().startsWith("head")) {
                        return;
                    }
                    if (ptype.toString().equalsIgnoreCase("internal") &&
                            rs.getType().equalsIgnoreCase("ip"))
                        return;
                    DateTime hlts = new DateTime();

                    HashMap hm = indicatorService.getFrontPageRevisionInfo(Integer.parseInt(rs.getId()));

                    if (!hm.containsKey("milestone")) {
                        hm.put("milestone", "");
                    }
                    hm.put("headProgram", false);
                    if (rs.getType().equalsIgnoreCase("software")) {
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
                    } else if (rs.getType().equalsIgnoreCase("ip")) {
//                        if (hls.getStage().equalsIgnoreCase("inactive")) {
//                            rs.setIs_active(false);
//                            revSearchServ.save(rs);
//                            return;
//                        }
                        if (rs.getRev_name().toLowerCase().startsWith("head")
                                || rs.getProgram_name().toLowerCase().endsWith("hidden")) {
                            hm.put("program", hm.get("program").toString().replaceAll("(?i)_hidden", ""));
                            String rname = hm.get("rev").toString().replaceAll("\\<[^>]*>", "")
                                    .replaceAll("(?i)head_", "");
                            if (rname.toLowerCase().equals("ip")) {
                                hm.put("reportName",
                                        hm.get("reportName").toString().replaceAll("(?i)head_ip", ""));
                                hm.put("displayName",
                                        hm.get("displayName").toString().replaceAll("(?i)head_ip", ""));
                                hm.put("revision",
                                        hm.get("revision").toString().replaceAll("(?i)head_ip", ""));
                                hm.put("rev", hm.get("rev").toString().replaceAll("(?i)head_", ""));
                            } else {
                                hm.put("reportName",
                                        hm.get("reportName").toString().replaceAll("(?i)head_", ""));
                                hm.put("displayName",
                                        hm.get("displayName").toString().replaceAll("(?i)head_", ""));
                                hm.put("revision",
                                        hm.get("revision").toString().replaceAll("(?i)head_", ""));
                                hm.put("rev", hm.get("rev").toString().replaceAll("(?i)head_", ""));
                            }
                            hm.put("headProgram", true);
                        }
                    }
                    String skey = "";
                    List hll = new ArrayList();
                    if (rs.getType().equalsIgnoreCase("ip")) {
                        skey = hm.get("category").toString().toUpperCase();
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
}
