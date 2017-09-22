package com.broadcom.wbi.controller;


import com.broadcom.wbi.exception.CustomGenericException;
import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.service.elasticSearch.IndicatorGroupSearchService;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.elasticSearch.TemplateSearchService;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.EmployeeService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.service.jpa.SegmentService;
import com.broadcom.wbi.service.report.ReportService;
import com.broadcom.wbi.util.EmailUtil;
import com.broadcom.wbi.util.ProjectConstant;
import com.broadcom.wbi.util.TextUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.WordUtils;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.xslf.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(value = "/api/report")
public class ReportController {

    static final DateTimeFormatter dfmt = org.joda.time.format.DateTimeFormat.forPattern("MM/dd/yy");
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ReportService reportService;
    private final RedisCacheRepository redisCacheRepository;
    private final EmployeeService employeeService;
    private final TemplateSearchService templateSearchService;
    private final RevisionSearchService revisionSearchService;
    private final IndicatorService indicatorService;
    private final IndicatorGroupSearchService indicatorGroupSearchService;
    private final SegmentService segmentService;

    @Autowired
    public ReportController(ReportService reportService, RedisCacheRepository redisCacheRepository, EmployeeService employeeService,
                            TemplateSearchService templateSearchService, RevisionSearchService revisionSearchService, IndicatorService indicatorService,
                            IndicatorGroupSearchService indicatorGroupSearchService, SegmentService segmentService) {
        this.reportService = reportService;
        this.redisCacheRepository = redisCacheRepository;
        this.employeeService = employeeService;
        this.templateSearchService = templateSearchService;
        this.revisionSearchService = revisionSearchService;
        this.indicatorService = indicatorService;
        this.indicatorGroupSearchService = indicatorGroupSearchService;
        this.segmentService = segmentService;
    }

    @RequestMapping(value = {"/milestone/{type}/collect"}, method = {RequestMethod.GET})
    public WebAsyncTask<Map> getMilestoneReport(HttpServletRequest req, @PathVariable final String type,
                                                @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                                @RequestParam(value = "status", defaultValue = "true") final String statusString) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                if (!type.equalsIgnoreCase("internal") && !type.equalsIgnoreCase("customer")
                        && !type.equalsIgnoreCase("email") && !type.equalsIgnoreCase("ip"))
                    return null;
                ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
                if (type.toLowerCase().indexOf("customer") == 0) {
                    ptype = ProjectConstant.EnumProgramType.CUSTOMER;
                } else if (type.toLowerCase().indexOf("ip") == 0) {
                    ptype = ProjectConstant.EnumProgramType.IP;
                }
                String redisk = ptype.toString().toLowerCase() + "_" + statusString.toLowerCase() + "_milestone_report";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map data = reportService.generateMilestoneReport(ptype, statusString);
                    TreeMap ret = new TreeMap(data);
                    if (ret.keySet().size() > 0) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return new TreeMap(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }

    @RequestMapping(value = {"/headline/{type}/collect"}, method = {RequestMethod.GET})
    public WebAsyncTask<Map> getHeadlineReport(HttpServletRequest req, @PathVariable String type,
                                               @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                               @RequestParam(value = "status", defaultValue = "true") final String statusString,
                                               @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                if (!type.equalsIgnoreCase("internal") && !type.equalsIgnoreCase("customer")
                        && !type.equalsIgnoreCase("ip"))
                    return null;
                ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
                if (type.toLowerCase().indexOf("customer") == 0) {
                    ptype = ProjectConstant.EnumProgramType.CUSTOMER;
                } else if (type.toLowerCase().indexOf("ip") == 0) {
                    ptype = ProjectConstant.EnumProgramType.IP;
                }
                String redisk = ptype.toString().toLowerCase() + "_" + statusString.toLowerCase() + "_headline_report";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map data = reportService.generateHeadlineReport(ptype, statusString);
                    TreeMap ret = new TreeMap(data);
                    if (ret.keySet().size() > 0) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return new TreeMap(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'ADMIN')")
    @RequestMapping(value = {"/headline/{type}/ppt/convert"}, method = {RequestMethod.POST})
    @ResponseBody
    public WebAsyncTask<HashMap> convertToPPT(HttpServletRequest req, @PathVariable String type,
                                              @RequestBody final HashMap hm) {
        Callable<HashMap> callable = new Callable<HashMap>() {
            @Override
            public HashMap call() throws Exception {
                HashMap ret = new HashMap();
                ret.put("file", "");
                try {
                    String template = "";
                    if (type.toLowerCase().indexOf("internal") == 0) {
                        template = "ChipSwitchGroupSeniorStaff.pptx";
                    } else if (type.toLowerCase().indexOf("customer") == 0) {
                        template = "CustomerSwitchGroupSeniorStaff.pptx";
                    } else if (type.toLowerCase().indexOf("ip") == 0) {
                        template = "IPSwitchGroupSeniorStaff.pptx";
                    }
                    if (template.isEmpty()) {
                        return null;
                    }
                    TreeMap tm = new TreeMap(hm);
                    String fname = convertToPPT(type, tm, template);
                    ret.put("file", fname);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return ret;
            }
        };
        return new WebAsyncTask<HashMap>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'ADMIN')")
    @RequestMapping(value = {"/headline/{type}/ppt/download"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<ResponseEntity<InputStreamResource>> downloadToPPT(HttpServletRequest req,
                                                                           @PathVariable String type) {
        Callable<ResponseEntity<InputStreamResource>> callable = new Callable<ResponseEntity<InputStreamResource>>() {
            @Override
            public ResponseEntity<InputStreamResource> call() throws Exception {
                String fname = System.getProperty("user.dir") + "\\" + type + "_exec_report_slide.pptx";
                File file = new File(fname);

                HttpHeaders respHeaders = new HttpHeaders();
                respHeaders.setContentType(
                        new MediaType("application", "vnd.openxmlformats-officedocument.presentationml.presentation"));
                respHeaders.setContentDispositionFormData("attachment", file.getName());
                respHeaders.setContentLength(file.length());
                InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
                return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
            }
        };
        return new WebAsyncTask<ResponseEntity<InputStreamResource>>(1800000, callable);
    }

    @SuppressWarnings("resource")
    private String convertToPPT(String ptype, TreeMap list, String template) throws IOException {
        List<HashMap<String, String>> listhl = new ArrayList();
        List<HashMap<String, String>> milestonelist = new ArrayList();
        // ClassPathResource templatePath = new
        // ClassPathResource("static/reportTemplate/"+template);
        // File file = templatePath.getFile();
        File file = new File(System.getProperty("user.dir") + "\\" + template);
        FileInputStream is = new FileInputStream(file);
        XMLSlideShow ppt = new XMLSlideShow(is);
        XSLFSlideLayout contentLayout = null;
        XSLFSlideLayout thankyouLayout = null;

        XSLFTableRow row = null;
        XSLFTableCell cell = null;
        XSLFTextParagraph p = null;
        XSLFTextRun rt = null;
        for (XSLFSlideMaster master : ppt.getSlideMasters()) {
            for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                if (layout.getName().toLowerCase().indexOf("content") != -1) {
                    contentLayout = layout;
                } else if (layout.getName().toLowerCase().indexOf("thank") != -1) {
                    thankyouLayout = layout;
                }
            }
        }
        XSLFTable table = null;
        XSLFSlide master_slide = ppt.getSlides().get(1);
        XSLFSlide slide = ppt.createSlide(contentLayout).importContent(master_slide);
        List<XSLFShape> sh = slide.getShapes();
        for (XSLFShape shape : sh) {
            if ((shape instanceof XSLFTable)) {
                table = (XSLFTable) shape;
            }
        }
        int numText = 0;
        int projectk_count = 0;
        if (table != null) {
            SortedSet<String> keys = new TreeSet<String>(list.keySet());
            projloop:
            for (Object projectk : keys) {
                projectk_count++;
                Map progMap = null;
                Object proj = list.get(projectk);
                if (proj instanceof LinkedHashMap) {
                    progMap = (LinkedHashMap) proj;
                } else if (proj instanceof HashMap) {
                    progMap = (HashMap) proj;
                }
                if (progMap == null)
                    continue projloop;

                String tmp_hl = progMap.get("headline").toString();
                int hl_length = tmp_hl.length();
                if (hl_length < 150) {
                    hl_length = 150;
                }
                if (numText > 1600 || projectk_count > 7) {
                    table = null;
                    slide = ppt.createSlide(contentLayout).importContent(master_slide);
                    for (XSLFShape shape : slide.getShapes()) {
                        if ((shape instanceof XSLFTable)) {
                            table = (XSLFTable) shape;
                        }
                    }
                    numText = 0;
                    row = null;
                    cell = null;
                    projectk_count = 0;
                }
                numText += hl_length;
                String project = projectk.toString();
                if (projectk.toString().indexOf("&") != -1) {
                    project = projectk.toString().split("&&")[1];
                }
                String[] custNames = project.split(" ");
                String status = progMap.get("status").toString().trim();
                row = table.addRow();

                cell = formatTableBorder(row.addCell());

                List<Integer> colors = convertCSSClassToColor(status);
                cell.setFillColor(new Color(colors.get(0), colors.get(1), colors.get(2)));

                // add PM column
                cell = formatTableBorder(row.addCell());
                if (cell.getTextParagraphs().size() > 0) {
                    p = cell.getTextParagraphs().get(0);
                } else {
                    p = cell.addNewTextParagraph();
                }
                p.setBullet(false);
                if (progMap.containsKey("pm")) {
                    rt = p.addNewTextRun();
                    rt.setFontFamily("Arial");
                    rt.setFontSize(10.0);
                    rt.setText(WordUtils.capitalizeFully(progMap.get("pm").toString().toLowerCase().trim()));
                }
                pbLoop:
                for (int i = cell.getTextParagraphs().size() - 1; i > -1; i--) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = tmp_p.getTextRuns().size() - 1; j > -1; j--) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pbLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pbLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }
                pfLoop:
                for (int i = 0; i < cell.getTextParagraphs().size(); i++) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = 0; j < tmp_p.getTextRuns().size(); j++) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pfLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pfLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }

                // add project name column
                cell = formatTableBorder(row.addCell());
                if (cell.getTextParagraphs().size() > 0) {
                    p = cell.getTextParagraphs().get(0);
                } else {
                    p = cell.addNewTextParagraph();
                }
                if (ptype.toLowerCase().indexOf("customer") != -1) {
                    p.setBullet(false);
                    p.setLeftMargin(1.0);
                    rt = p.addNewTextRun();
                    rt.setFontFamily("Arial");
                    rt.setFontSize(10.0);
                    rt.setText(formatCustomerName(custNames[0]).trim());

                    String[] projectNames = project.substring(project.indexOf(" ")).split("/");
                    for (String pr : projectNames) {
                        p = cell.addNewTextParagraph();
                        p.setBullet(true);
                        p.setLeftMargin(10.0);
                        p.setIndent(-10.0);
                        rt = p.addNewTextRun();
                        rt.setFontFamily("Arial");
                        rt.setFontSize(10.0);
                        if (pr.length() > 1) {
                            rt.setText(TextUtil.formatName(pr.trim()));
                        }
                    }

                    String fcs = progMap.get("fcs").toString().replaceAll("\\<.*?>", "").trim();
                    if (!fcs.isEmpty()) {
                        p = cell.addNewTextParagraph();
                        p.setBullet(false);
                        p.setLeftMargin(1.0);
                        rt = p.addNewTextRun();
                        rt.setFontFamily("Arial");
                        rt.setFontSize(10.0);
                        rt.setText("FCS: " + fcs);
                    }
                } else {
                    p.setBullet(false);
                    p.setLeftMargin(1.0);
                    rt = p.addNewTextRun();
                    rt.setFontFamily("Arial");
                    rt.setFontSize(10.0);
                    if (ptype.toLowerCase().indexOf("ip") != 0) {
                        rt.setText(TextUtil.formatName(project).trim());
                    } else {
                        rt.setText(project.replaceAll("(?i)^na", "").trim());
                    }
                    if (progMap.containsKey("ea")) {
                        p = cell.addNewTextParagraph();
                        p.setBullet(true);
                        p.setLeftMargin(10.0);
                        p.setIndent(-10.0);
                        rt = p.addNewTextRun();
                        rt.setFontFamily("Arial");
                        rt.setFontSize(10.0);
                        rt.setText(progMap.get("ea").toString().trim());
                    }
                    if (progMap.containsKey("ga")) {
                        p = cell.addNewTextParagraph();
                        p.setBullet(true);
                        p.setLeftMargin(10.0);
                        p.setIndent(-10.0);
                        rt = p.addNewTextRun();
                        rt.setFontFamily("Arial");
                        rt.setFontSize(10.0);
                        rt.setText(progMap.get("ga").toString().trim());
                    }
                }
                pbLoop:
                for (int i = cell.getTextParagraphs().size() - 1; i > -1; i--) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = tmp_p.getTextRuns().size() - 1; j > -1; j--) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pbLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pbLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }
                pfLoop:
                for (int i = 0; i < cell.getTextParagraphs().size(); i++) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = 0; j < tmp_p.getTextRuns().size(); j++) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pfLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pfLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }

                cell = formatTableBorder(row.addCell());
                if (cell.getTextParagraphs().size() > 0) {
                    p = cell.getTextParagraphs().get(0);
                } else {
                    p = cell.addNewTextParagraph();
                }
                // parse milestone
                if (ptype.toLowerCase().indexOf("chip") != -1 || ptype.toLowerCase().indexOf("internal") != -1) {
                    if (progMap.containsKey("milestone")) {
                        if ((progMap.get("milestone") instanceof String)) {
                            p.setBullet(false);
                            p.setLeftMargin(10.0);
                            p.setIndent(-10.0);
                            rt = p.addNewTextRun();
                            rt.setFontFamily("Arial");
                            rt.setFontSize(10.0);
                            String hl = progMap.get("milestone").toString();
                            hl = hl.replaceAll("\\<.*?>", "");
                            hl = hl.replaceAll("&nbsp;", "");
                            rt.setText(hl);
                        } else if ((progMap.get("milestone") instanceof ArrayList)) {
                            milestonelist = (ArrayList) progMap.get("milestone");
                            HashMap mp = milestonelist.get(0);
                            p.setBullet(false);
                            p.setLeftMargin(10.0);
                            p.setIndent(-10.0);
                            for (Object k : mp.keySet()) {
                                rt = p.addNewTextRun();
                                rt.setFontFamily("Arial");
                                rt.setFontSize(10.0);
                                rt.setText(k.toString() + ": ");
                                rt.setBold(true);

                                rt = p.addNewTextRun();
                                rt.setFontFamily("Arial");
                                rt.setFontSize(10.0);
                                rt.setText(mp.get(k).toString().trim() + "\n");
                            }
                        }
                    }
                } else if (ptype.toLowerCase().indexOf("ip") == 0) {
                    if (progMap.containsKey("stage")) {
                        if ((progMap.get("stage") instanceof String)) {
                            p.setBullet(false);
                            p.setLeftMargin(10.0);
                            p.setIndent(-10.0);
                            rt = p.addNewTextRun();
                            rt.setFontFamily("Arial");
                            rt.setFontSize(10.0);
                            String hl = progMap.get("stage").toString();
                            hl = hl.replaceAll("\\<.*?>", "");
                            hl = hl.replaceAll("&nbsp;", "");
                            rt.setText(hl);
                        }
                    }
                } else {
                    p.setBullet(false);
                    rt = p.addNewTextRun();
                    rt.setFontFamily("Arial");
                    rt.setFontSize(10.0);
                    rt.setText(progMap.get("chip").toString().trim());
                    String sdk = progMap.get("sdk_fcs").toString().toUpperCase().trim();
                    if (!sdk.isEmpty()) {
                        p = cell.addNewTextParagraph();
                        p.setBullet(false);
                        p.setLeftMargin(1.0);
                        rt = p.addNewTextRun();
                        rt.setFontFamily("Arial");
                        rt.setFontSize(10.0);
                        rt.setText("");

                        p = cell.addNewTextParagraph();
                        p.setBullet(false);
                        p.setLeftMargin(1.0);
                        rt = p.addNewTextRun();
                        rt.setFontFamily("Arial");
                        rt.setFontSize(10.0);
                        rt.setText("SDK " + sdk);
                    }
                }
                pbLoop:
                for (int i = cell.getTextParagraphs().size() - 1; i > -1; i--) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = tmp_p.getTextRuns().size() - 1; j > -1; j--) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pbLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pbLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }
                pfLoop:
                for (int i = 0; i < cell.getTextParagraphs().size(); i++) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = 0; j < tmp_p.getTextRuns().size(); j++) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pfLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pfLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }

                cell = formatTableBorder(row.addCell());
                if (cell.getTextParagraphs().size() > 0) {
                    p = cell.getTextParagraphs().get(0);
                } else {
                    p = cell.addNewTextParagraph();
                }

                System.out.println(projectk + "---" + progMap.get("headline"));
                // parse headline
                if (progMap.containsKey("headline")) {
                    if ((progMap.get("headline") instanceof String)) {
                        p.setBullet(false);
                        p.setLeftMargin(10.0);
                        p.setIndent(-10.0);
                        rt = p.addNewTextRun();
                        rt.setFontFamily("Arial");
                        rt.setFontSize(10.0);
                        String hl = progMap.get("headline").toString();
                        hl = hl.replaceAll("\\<.*?>", "");
                        hl = hl.replaceAll("&nbsp;", "");
                        rt.setText(hl);
                    } else if ((progMap.get("headline") instanceof ArrayList)) {
                        listhl = (ArrayList) progMap.get("headline");
                        int issue_count = 0;
                        for (Object obj : listhl) {
                            issue_count++;
                            HashMap mp = (HashMap) obj;
                            boolean hasIssue = false;
                            if ((mp.containsKey("issue"))
                                    && ((status.indexOf("green") == -1 && status.indexOf("black") == -1)
                                    || ptype.toLowerCase().indexOf("chip") == -1)) {
                                p.setBullet(false);
                                p.setLeftMargin(10.0);
                                p.setIndent(-10.0);
                                rt = p.addNewTextRun();
                                rt.setFontFamily("Arial");
                                rt.setFontSize(10.0);
                                if (issue_count > 1)
                                    rt.setText("\n\nIssue: ");
                                else
                                    rt.setText("Issue: ");
                                rt.setBold(true);
                                rt = p.addNewTextRun();
                                rt.setFontFamily("Arial");
                                rt.setFontSize(10.0);
                                if (mp.containsKey("chip affected")) {
                                    rt.setText("[" + mp.get("chip affected").toString().trim() + "] "
                                            + mp.get("issue").toString().trim());
                                } else {
                                    rt.setText(mp.get("issue").toString().trim());
                                }
                                hasIssue = true;
                            }
                            if (mp.containsKey("status") && !mp.get("status").toString().trim().isEmpty()) {
                                rt = p.addNewTextRun();
                                rt.setFontFamily("Arial");
                                rt.setFontSize(10.0);
                                if (hasIssue || issue_count > 1)
                                    rt.setText("\nStatus: ");
                                else
                                    rt.setText("Status: ");

                                rt.setBold(true);
                                rt = p.addNewTextRun();
                                rt.setFontFamily("Arial");
                                rt.setFontSize(10.0);
                                rt.setText(mp.get("status").toString().trim());
                            }
                            if ((mp.containsKey("next step"))
                                    && ((status.indexOf("green") == -1 && status.indexOf("black") == -1)
                                    || ptype.toLowerCase().indexOf("chip") == -1)) {
                                rt = p.addNewTextRun();
                                rt.setFontFamily("Arial");
                                rt.setFontSize(10.0);
                                rt.setText("\nNext Steps: ");
                                rt.setBold(true);
                                String headline = mp.get("next step").toString().trim();
                                String[] hlArr = headline.split("]");
                                for (String hl : hlArr) {
                                    hl = hl.trim();
                                    if (!hl.matches("[0-9a-zA-Z]")) {
                                        p = cell.addNewTextParagraph();
                                        p.setLeftMargin(10.0);
                                        p.setIndent(-10.0);
                                        p.setBullet(true);
                                        // p.setBulletCharacter("\u0075");
                                        for (int j = 0; j < p.getTextRuns().size(); j++) {
                                            p.getTextRuns().remove(j);
                                        }
                                        rt = p.addNewTextRun();
                                        rt.setFontFamily("Arial");
                                        rt.setFontSize(10.0);
                                        int sb_start_pos = hl.indexOf("[");
                                        int sb_end_pos = hl.indexOf("]");
                                        if (sb_end_pos == -1) {
                                            sb_end_pos = hl.length();
                                        }
                                        if ((sb_start_pos != -1) && (sb_end_pos != -1)) {
                                            rt.setText(hl.substring(0, sb_start_pos));
                                            rt = p.addNewTextRun();
                                            rt.setFontFamily("Arial");
                                            rt.setFontSize(10.0);
                                            rt.setBold(true);
                                            if (sb_end_pos == hl.length()) {
                                                rt.setText(hl.substring(sb_start_pos, sb_end_pos) + "]");
                                            } else if (sb_end_pos < hl.length()) {
                                                rt.setText(hl.substring(sb_start_pos, sb_end_pos + 1));
                                            }
                                            if (sb_end_pos + 1 < hl.length() - 1) {
                                                rt = p.addNewTextRun();
                                                rt.setFontFamily("Arial");
                                                rt.setFontSize(10.0);
                                                rt.setText(hl.substring(sb_end_pos + 1, hl.length() - 1));
                                            }
                                        } else {
                                            rt.setText(hl);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                pbLoop:
                for (int i = cell.getTextParagraphs().size() - 1; i > -1; i--) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = tmp_p.getTextRuns().size() - 1; j > -1; j--) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pbLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pbLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }
                pfLoop:
                for (int i = 0; i < cell.getTextParagraphs().size(); i++) {
                    XSLFTextParagraph tmp_p = cell.getTextParagraphs().get(i);
                    if (tmp_p.getTextRuns().size() > 0) {
                        for (int j = 0; j < tmp_p.getTextRuns().size(); j++) {
                            XSLFTextRun tmp_tr = tmp_p.getTextRuns().get(j);
                            if (!tmp_tr.toString().trim().isEmpty())
                                continue pfLoop;
                        }
                    } else {
                        if (!tmp_p.getText().trim().isEmpty())
                            continue pfLoop;
                    }
                    cell.getTextParagraphs().remove(i);
                }
            }
        }
        if (thankyouLayout != null) {
            ppt.createSlide(thankyouLayout);
        }
        ppt.removeSlide(1);
        File outputfile = new File(System.getProperty("user.dir") + "\\" + ptype + "_exec_report_slide.pptx");
        if (outputfile.exists())
            outputfile.delete();
        FileOutputStream out = new FileOutputStream(
                System.getProperty("user.dir") + "\\" + ptype + "_exec_report_slide.pptx");
        ppt.write(out);
        out.close();

        return System.getProperty("user.dir") + "\\" + ptype + "_exec_report_slide.pptx";
    }

    private XSLFTableCell formatTableBorder(XSLFTableCell cell) {
        cell.clearText();
        cell.setLeftInset(3.0);
        cell.setBorderWidth(TableCell.BorderEdge.bottom, 1);
        cell.setBorderWidth(TableCell.BorderEdge.top, 1);
        cell.setBorderWidth(TableCell.BorderEdge.left, 1);
        cell.setBorderWidth(TableCell.BorderEdge.right, 1);

        cell.setBorderColor(TableCell.BorderEdge.bottom, new Color(230, 230, 230));
        cell.setBorderColor(TableCell.BorderEdge.top, new Color(230, 230, 230));
        cell.setBorderColor(TableCell.BorderEdge.left, new Color(230, 230, 230));
        cell.setBorderColor(TableCell.BorderEdge.right, new Color(230, 230, 230));

        cell.setText("");
        return cell;
    }

    private String formatCustomerName(String cus) {
        String l = cus.replaceAll("\\d", "").replaceAll("\\.", "").trim();
        if (l.length() > 4) {
            return WordUtils.capitalizeFully(cus);
        }
        return cus.toUpperCase();
    }

    private List<Integer> convertCSSClassToColor(String css) {
        List<Integer> ret = new ArrayList();
        if (css.toLowerCase().indexOf("green") != -1 || css.toLowerCase().indexOf("black") != -1) {
            ret.add(Integer.valueOf(0));
            ret.add(Integer.valueOf(128));
            ret.add(Integer.valueOf(0));
        } else if (css.toLowerCase().indexOf("orange") != -1) {
            ret.add(Integer.valueOf(255));
            ret.add(Integer.valueOf(165));
            ret.add(Integer.valueOf(0));
        } else {
            ret.add(Integer.valueOf(255));
            ret.add(Integer.valueOf(0));
            ret.add(Integer.valueOf(0));
        }
        return ret;
    }

    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
    @RequestMapping(value = {"/sendEmailReport"}, method = {RequestMethod.POST})
    @ResponseBody
    public WebAsyncTask<HashMap> emailReport(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<HashMap> callable = new Callable<HashMap>() {
            @Override
            public HashMap call() throws Exception {
                HashMap ret = new HashMap();
                ret.put("title", "Unknown Error");
                ret.put("type", "error");
                try {
                    String content = hm.get("data").toString();
                    DateTime dt = new DateTime();
                    final String username = SecurityContextHolder.getContext().getAuthentication().getName();
                    Employee user = employeeService.findByAccountName(username);
                    if (user == null && username.matches(".*\\d+.*")) {
                        String id = username.substring(3);
                        try {
                            user = employeeService.findById(Integer.parseInt(id));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    if (user == null)
                        throw new CustomGenericException("User not found");

                    String title = "Indicator Report " + dt.getMonthOfYear() + "/" + dt.getDayOfMonth() + "/"
                            + dt.getYear();
                    EmailUtil.sendEmail(user.getEmail(), user.getEmail(), title, content);
                    ret.put("message", "Please check your email for report attachment");
                    ret.put("title", "Email Sent");
                    ret.put("type", "success");
                } catch (Exception e) {
                    e.printStackTrace();
                    ret.put("message", e.getMessage());
                }
                return ret;
            }
        };
        return new WebAsyncTask<HashMap>(1800000, callable);
    }


    @RequestMapping(value = {"/other/pra/collect"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> getPRAReport(HttpServletRequest req,
                                          @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                          @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                String redisk = "pra_milestoneReport";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map ret = reportService.generatePRAReport();
                    if (ret != null) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return new TreeMap(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }

    @RequestMapping(value = {"/other/htol/collect"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> getHTOLReport(HttpServletRequest req,
                                           @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                           @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                String redisk = "htol_milestoneReport";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map ret = reportService.generateHTOLReport();
                    if (ret != null) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return new TreeMap(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }

    @RequestMapping(value = {"/information/{type}/collect"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> getInformationReport(HttpServletRequest req, @PathVariable String type,
                                                  @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                if (!type.equalsIgnoreCase("internal") && !type.equalsIgnoreCase("customer"))
                    return null;
                ProjectConstant.EnumProgramType programType = ProjectConstant.EnumProgramType.CHIP;
                if (type.toLowerCase().indexOf("customer") != -1) {
                    programType = ProjectConstant.EnumProgramType.CUSTOMER;
                }
                final DateTime dt = new DateTime();
                String redisk = programType.toString().toLowerCase() + "_informationReport";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    Map ret = reportService.generateInformationReport(programType);
                    if (ret != null) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;

                } else {
                    String value = (String) redisCacheRepository.get(redisk);
                    try {
                        Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                        });
                        return ret;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
    @RequestMapping(value = {"/outlook/{type}/collect"}, method = {RequestMethod.GET})
    @ResponseBody
    public WebAsyncTask<Map> getOutlookReport(HttpServletRequest req, @PathVariable String type,
                                              @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                              @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<Map> callable = new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                if (!type.equalsIgnoreCase("internal") && !type.equalsIgnoreCase("customer"))
                    return null;
                ProjectConstant.EnumProgramType ptype = ProjectConstant.EnumProgramType.CHIP;
                if (type.toLowerCase().indexOf("customer") != -1) {
                    ptype = ProjectConstant.EnumProgramType.CUSTOMER;
                }
                String redisk = ptype.toString().toLowerCase() + "_outlookReport";
                if (reload == 1)
                    redisCacheRepository.delete(redisk);
                if (!redisCacheRepository.hasKey(redisk)) {
                    final Map ret = Collections.synchronizedMap(new TreeMap());

                    List<RevisionSearch> revisions = revisionSearchService.findByProgramType(ptype, true);
                    if (revisions == null)
                        return null;

                    rsloop:
                    for (final RevisionSearch rs : revisions) {
                        HashMap hm = new HashMap();
                        if (rs.getOutlook() == null)
                            continue rsloop;
                        DateTime dt = new DateTime(rs.getLast_updated_outlook_date());
                        hm.put("outlook", rs.getOutlook());
                        hm.put("outlookts", dt.toString(dfmt));
                        hm.put("pname", TextUtil.formatName(rs.getProgram_name()));
                        hm.put("rname", rs.getRev_name().toUpperCase());

                        List hll = new ArrayList();
                        String segmentString = rs.getSegment();
                        Segment seg = segmentService.findByName(segmentString);
                        if (ret.containsKey(seg.getOrderNum() + "_" + seg.getName().toUpperCase())) {
                            hll = (ArrayList) ret.get(seg.getOrderNum() + "_" + seg.getName().toUpperCase());
                        }
                        hll.add(hm);
                        ret.put(seg.getOrderNum() + "_" + seg.getName().toUpperCase(), hll);
                    }
                    if (ret.keySet().size() > 0) {
                        try {
                            redisCacheRepository.put(redisk, mapper.writeValueAsString(ret));
                            return ret;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
                String value = (String) redisCacheRepository.get(redisk);
                try {
                    Map ret = (HashMap) mapper.readValue(value, new TypeReference<HashMap>() {
                    });
                    return new TreeMap(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return new WebAsyncTask<Map>(1800000, callable);
    }

}
