package com.broadcom.wbi.controller;

import com.broadcom.wbi.exception.CustomGenericException;
import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.model.elasticSearch.*;
import com.broadcom.wbi.model.mysql.*;
import com.broadcom.wbi.service.elasticSearch.*;
import com.broadcom.wbi.service.event.*;
import com.broadcom.wbi.service.indicator.IndicatorService;
import com.broadcom.wbi.service.jpa.*;
import com.broadcom.wbi.util.DateResetUtil;
import com.broadcom.wbi.util.DateUtil;
import com.broadcom.wbi.util.ProjectConstant;
import com.broadcom.wbi.util.TextUtil;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/revision")
public class RevisionController {

    final static DateTimeFormatter dfmt = org.joda.time.format.DateTimeFormat.forPattern("MM/dd/yy");
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
    private RevisionInformationSearchService revisionInformationSearchService;
    @Autowired
    private RevisionInformationService revisionInformationService;
    @Autowired
    private RevisionService revisionService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private ProgramService programService;
    @Autowired
    private RevisionContactService revisionContactService;
    @Autowired
    private RevisionIPService revisionIPService;
    @Autowired
    private RevisionContactSearchService revisionContactSearchService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private RevisionOutlookService revisionOutlookService;
    @Autowired
    private LinkService linkService;
    @Autowired
    private HeadlineService headlineService;
    @Autowired
    private HeadlineSearchService headlineSearchService;
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
    private TaskSaveEventPublisher taskSaveEventPublisher;
    @Autowired
    private HeadlineSaveEventPublisher headlineSaveEventPublisher;
    @Autowired
    private CacheClearEventPublisher cacheClearEventPublisher;
    @Autowired
    private RedisCacheRepository redisCacheRepository;

    @RequestMapping(value = {"/getInformation"}, method = {RequestMethod.GET})
    public WebAsyncTask<LinkedHashMap> getInformation(HttpServletRequest req,
                                                      @RequestParam(value = "rid", defaultValue = "0") final int rid,
                                                      @RequestParam(value = "type", defaultValue = "dashboard") final String infoType,
                                                      @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<LinkedHashMap> callable = new Callable<LinkedHashMap>() {
            @Override
            public LinkedHashMap call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                return revisionInformationSearchService.getRevisionInformationReport(rid, infoType);
            }
        };
        return new WebAsyncTask<LinkedHashMap>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveInformation"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveInformation(HttpServletRequest req, @RequestBody final HashMap map) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (map.keySet().size() == 0) {
                    ret.put("data", "Data missing in request");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                Revision rev = null;
                if (map.containsKey("rid")) {
                    try {
                        Integer rid = Integer.parseInt(map.get("rid").toString());
                        rev = revisionService.findById(rid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                final Revision revision = rev;
                final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

                RevisionSearch rs = revisionSearchService.findById(Integer.toString(revision.getId()));
                if (map.containsKey("type")) {
                    String type = map.get("type").toString();
                    ExecutorService executor = Executors.newFixedThreadPool(20);
                    if (type.equalsIgnoreCase("dashboard")) {
                        List list = new ArrayList();
                        if (map.containsKey("data")) {
                            list = (ArrayList) map.get("data");
                        }
                        if (list == null || list.size() == 0)
                            return null;
                        for (final Object obj : list) {
                            executor.submit(new Runnable() {
                                public void run() {
                                    HashMap map = (HashMap) obj;
                                    if (map.containsKey("editable")) {
                                        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                                        ctx.setAuthentication(currentAuthentication);
                                        SecurityContextHolder.setContext(ctx);

                                        Boolean isEditable = (Boolean) map.get("editable");
                                        if (isEditable) {
                                            String value = map.get("value").toString().trim();
                                            try {
                                                Integer id = Integer.parseInt(map.get("id").toString());
                                                if (id > 0) {
                                                    RevisionInformation ri = revisionInformationService.findById(id);
                                                    if (ri != null && !ri.getValue().equalsIgnoreCase(value.trim())) {
                                                        ri.setValue(value);
                                                        ri = revisionInformationService.saveOrUpdate(ri);
                                                    }

                                                    RevisionInformationSearch ris = revisionInformationSearchService.findById(Integer.toString(id));
                                                    if (ris != null && !ris.getValue().equalsIgnoreCase(value.trim())) {
                                                        ris.setValue(value.trim().toLowerCase());
                                                        ri.setValue(value);
                                                        revisionInformationSearchService.saveOrUpdate(ris);
                                                    }
                                                } else {
                                                    if (revision != null) {
                                                        String name = map.get("key").toString().trim();
                                                        Integer orderNum = Integer.parseInt(map.get("order").toString().trim());
                                                        RevisionInformation ri = new RevisionInformation();
                                                        ri.setIsRestrictedView(false);
                                                        ri.setIsUserEditable(true);
                                                        ri.setName(name);
                                                        ri.setOnDashboard(true);
                                                        ri.setOrderNum(orderNum);
                                                        ri.setPhase("current");
                                                        ri.setRevision(revision);
                                                        ri.setValue(value);
                                                        ri = revisionInformationService.saveOrUpdate(ri);

                                                        RevisionInformationSearch ris = new RevisionInformationSearch();
                                                        ris.setCreated_date(ri.getCreatedDate());
                                                        ris.setId(Integer.toString(ri.getId()));
                                                        ris.setOrderNum(ri.getOrderNum());
                                                        ris.setLast_updated_date(ri.getLastUpdatedDate());
                                                        ris.setName(ri.getName().toLowerCase().trim());
                                                        ris.setOnDashboard(ri.getOnDashboard());
                                                        ris.setPhase(ri.getPhase().toLowerCase().trim());
                                                        ris.setValue(ri.getValue().toLowerCase().trim());
                                                        ris.setIsUserEditable(ri.getIsUserEditable());
                                                        ris.setIsRestrictedView(ri.getIsRestrictedView());
                                                        ris.setRevision(revision.getId());

                                                        revisionInformationSearchService.saveOrUpdate(ris);
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                e.printStackTrace();
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
                    } else if (type.equalsIgnoreCase("detail")) {
                        HashMap submap = new HashMap();
                        if (map.containsKey("data")) {
                            submap = (HashMap) map.get("data");
                        }
                        if (submap.keySet().size() == 0)
                            return null;
                        final HashMap data = submap;
                        for (final Object title : data.keySet()) {
                            executor.submit(new Runnable() {
                                public void run() {
                                    HashMap hm = (HashMap) data.get(title);
                                    if (hm.keySet().size() > 0) {
                                        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                                        ctx.setAuthentication(currentAuthentication);
                                        SecurityContextHolder.setContext(ctx);
                                        for (Object key : hm.keySet()) {
                                            HashMap map = (HashMap) hm.get(key);
                                            if (map.containsKey("editable")) {
                                                Boolean isEditable = (Boolean) map.get("editable");
                                                if (isEditable) {
                                                    String value = map.get("value").toString().trim();
                                                    try {
                                                        Integer id = Integer.parseInt(map.get("id").toString());
                                                        if (title.toString().equalsIgnoreCase("base die") && !value.trim().isEmpty()) {
                                                            Program p = revision.getProgram();
                                                            p.setBaseNum(value);
                                                            p = programService.saveOrUpdate(p);
                                                            List<RevisionSearch> rsl = revisionSearchService.findByProgram(p.getId());
                                                            if (rsl != null && !rsl.isEmpty()) {
                                                                for (RevisionSearch rs : rsl) {
                                                                    rs.setBase_num(value);
                                                                    revisionSearchService.saveOrUpdate(rs);
                                                                }
                                                            }
                                                        }
                                                        if (id > 0) {
                                                            RevisionInformation ri = revisionInformationService.findById(id);
                                                            if (ri != null && !ri.getValue().equalsIgnoreCase(value.trim())) {

                                                                System.out.println(ri.getName() + " : " + value + "---" + ri.getValue());
                                                                ri.setValue(value);
                                                                revisionInformationService.saveOrUpdate(ri);
                                                            }
                                                            RevisionInformationSearch ris = revisionInformationSearchService.findById(Integer.toString(id));
                                                            if (ris != null && !ris.getValue().equalsIgnoreCase(value.trim())) {
                                                                ris.setValue(value.trim().toLowerCase());
                                                                ris.setLast_updated_date(new Date());
                                                                revisionInformationSearchService.saveOrUpdate(ris);
                                                            }
                                                        } else {
                                                            if (revision != null) {
                                                                if (map.containsKey("key") && !map.get("key").toString().trim().isEmpty()) {
                                                                    String name = map.get("key").toString().trim();
                                                                    Integer orderNum = 0;
                                                                    if (map.containsKey("order"))
                                                                        orderNum = Integer.parseInt(map.get("order").toString());
                                                                    else
                                                                        orderNum = 1000;
                                                                    RevisionInformation ri = new RevisionInformation();
                                                                    ri.setIsRestrictedView(false);
                                                                    ri.setIsUserEditable(true);
                                                                    ri.setName(name);
                                                                    ri.setOnDashboard(false);
                                                                    ri.setOrderNum(orderNum);
                                                                    ri.setPhase("current");
                                                                    ri.setRevision(revision);
                                                                    ri.setValue(value);
                                                                    ri = revisionInformationService.saveOrUpdate(ri);

                                                                    RevisionInformationSearch ris = new RevisionInformationSearch();
                                                                    ris.setCreated_date(ri.getCreatedDate());
                                                                    ris.setId(Integer.toString(ri.getId()));
                                                                    ris.setOrderNum(ri.getOrderNum());
                                                                    ris.setLast_updated_date(ri.getLastUpdatedDate());
                                                                    ris.setName(ri.getName().toLowerCase().trim());
                                                                    ris.setOnDashboard(ri.getOnDashboard());
                                                                    ris.setPhase(ri.getPhase().toLowerCase().trim());
                                                                    ris.setValue(ri.getValue().toLowerCase().trim());
                                                                    ris.setIsUserEditable(ri.getIsUserEditable());
                                                                    ris.setIsRestrictedView(ri.getIsRestrictedView());
                                                                    ris.setRevision(revision.getId());

                                                                    revisionInformationSearchService.saveOrUpdate(ris);
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
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
                            executor.awaitTermination(120, TimeUnit.MINUTES);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    /***********************************************************************
     * Contact
     * *********************************************************************/
    @RequestMapping(value = {"/getContact"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getContact(HttpServletRequest req,
                                         @RequestParam(value = "rid", defaultValue = "0") final int rid,
                                         @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                List ret = new ArrayList();
                List<RevisionContactSearch> pcsl = revisionContactSearchService.findByRevision(rid);
                if (pcsl != null && !pcsl.isEmpty()) {
                    for (RevisionContactSearch pcs : pcsl) {
                        String[] earr = pcs.getValue().split(",");
                        String key = TextUtil.formatName(pcs.getName());
                        HashMap map = new HashMap();
                        map.put("key", key);
                        map.put("id", pcs.getId());
                        StringBuilder sb = new StringBuilder();
                        for (String st : earr) {
                            sb.append(TextUtil.formatName(st.trim()) + ", ");
                        }
                        if (sb.length() > 0) {
                            map.put("value", sb.toString().trim().replaceAll(",$", "").replaceAll("\\s+", " ").trim());
                        }
                        ret.add(map);
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

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveContact"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveContact(HttpServletRequest req, @RequestBody final HashMap reqMap) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                HashMap ret = new HashMap();
                if (!reqMap.containsKey("data") || !reqMap.containsKey("rid")) {
                    if (!reqMap.containsKey("rid")) {
                        ret.put("data", "Revision is missing in request");
                    } else {
                        ret.put("data", "Data missing in request");
                    }
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                List dataList = (ArrayList) reqMap.get("data");
                final Revision rev = revisionService.findById(Integer.parseInt(reqMap.get("rid").toString()));
                if (rev == null) {
                    ret.put("data", "Revision Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rev.getId()));
                if (dataList.size() > 0) {
                    contactloop:
                    for (Object contactObj : dataList) {
                        HashMap hm = (HashMap) contactObj;
                        String key = hm.get("key").toString().trim();
                        RevisionContact contact = null;
                        int id = 0;
                        try {
                            id = Integer.parseInt(hm.get("id").toString());
                        } catch (Exception e) {
                            throw new CustomGenericException(e.getMessage());
                        }

                        if (hm.containsKey("isDeleted") && id > 0) {
                            boolean isDeleted = (Boolean) hm.get("isDeleted");
                            if (isDeleted) {
                                revisionContactService.delete(id);
                                RevisionContactSearch pcs = revisionContactSearchService.findById(Integer.toString(id));
                                if (pcs != null)
                                    revisionContactSearchService.delete(pcs.getId());
                                continue contactloop;
                            }
                        } else {
                            StringBuilder value = new StringBuilder();
                            if (hm.containsKey("value")) {
                                value.append(hm.get("value").toString());
                            }
                            if (hm.containsKey("isNew")) {
                                if (key.trim().isEmpty())
                                    continue contactloop;
                                boolean isNew = (Boolean) hm.get("isNew");
                                if (isNew) {
                                    contact = new RevisionContact();
                                    contact.setValue(value.toString().trim());
                                    contact.setName(key);
                                    contact.setRevision(rev);
                                    contact = revisionContactService.saveOrUpdate(contact);

                                    RevisionContactSearch pcs = new RevisionContactSearch();
                                    pcs.setId(Integer.toString(contact.getId()));
                                    pcs.setName(key.toLowerCase().trim());
                                    pcs.setOnDashboard(true);
                                    pcs.setRevision(rev.getId());
                                    pcs.setCreated_date(contact.getCreatedDate());
                                    pcs.setLast_updated_date(contact.getLastUpdatedDate());
                                    pcs.setValue(value.toString().toLowerCase().trim());
                                    revisionContactSearchService.saveOrUpdate(pcs);
                                }
                            } else {
                                contact = revisionContactService.findById(id);
                                if (contact == null)
                                    continue contactloop;
                                contact.setValue(value.toString());
                                contact.setName(key);
                                contact = revisionContactService.saveOrUpdate(contact);

                                RevisionContactSearch pcs = revisionContactSearchService.findById(Integer.toString(id));
                                pcs.setName(key.toLowerCase().trim());
                                pcs.setOnDashboard(true);
                                pcs.setRevision(rev.getId());
                                pcs.setCreated_date(contact.getCreatedDate());
                                pcs.setLast_updated_date(contact.getLastUpdatedDate());
                                pcs.setValue(value.toString().toLowerCase().trim());
                                revisionContactSearchService.saveOrUpdate(pcs);
                            }
                        }
                    }
                }
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    @RequestMapping(value = {"/saveBookmark"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveBookmark(HttpServletRequest req,
                                                     @RequestParam(value = "rid", defaultValue = "0") final int rid,
                                                     @RequestParam(value = "bookmark", defaultValue = "false") final boolean bookmark) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                HashMap ret = new HashMap();
                if (rid < 1) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                Revision rev = revisionService.findById(rid);
                if (rev == null) {
                    ret.put("data", "Revision Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rev.getId()));

                final String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Employee user = employeeService.findByAccountName(username);
                if (user == null) {
                    Integer user_id = Integer.parseInt(username.substring(3));
                    user = employeeService.findById(user_id);
                }
                if (user == null) {
                    ret.put("data", "User Not found");
                    ret.put("code", HttpStatus.NOT_FOUND);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ret);
                }

                Set<Revision> revs = user.getRevisions();
                if (revs == null) {
                    revs = new HashSet<Revision>();
                }
                if (bookmark) {
                    if (!revs.contains(rev)) {
                        revs.add(rev);
                        user.setRevisions(revs);
                        employeeService.saveOrUpdate(user);
                    }
                } else {
                    if (revs.contains(rev)) {
                        revs.remove(rev);
                        user.setRevisions(revs);
                        employeeService.saveOrUpdate(user);
                    }
                }
                redisCacheRepository.deleteWildCard(rs.getSegment().toLowerCase() + "_" + username + "_*");
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }


    /***********************************************************************
     * IP Revision
     * *********************************************************************/

    @RequestMapping(value = {"/getIPTable"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getIPTable(HttpServletRequest req,
                                         @RequestParam(value = "rid", defaultValue = "0") final int rid,
                                         @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                final List ret = Collections.synchronizedList(new ArrayList());
                Revision rev = revisionService.findById(rid);
                final RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                if (rs == null)
                    throw new IDNotFoundException(rid, "revision");
                final DateTime dt = new DateTime();
                List<RevisionIP> iplist = revisionIPService.findByRevision(rev);
                if (iplist != null && !iplist.isEmpty()) {
                    Program program = rev.getProgram();
                    Revision a0rev = revisionService.findByProgramName(program, "a0");
                    ExecutorService executor = Executors.newFixedThreadPool(10);
                    for (final RevisionIP obj : iplist) {
                        executor.submit(new Runnable() {
                            public void run() {
                                Revision ip = obj.getIprevision();
                                HashMap hm = indicatorService.getFrontPageRevisionInfo(ip.getId());
                                if (hm.keySet().size() > 0) {
                                    HashMap map = new HashMap();
                                    map.put("url", "/program/ip/" + hm.get("pid") + "/" + hm.get("rid") + "/dashboard");
                                    map.put("instances", obj.getInstanceNum());
                                    map.put("schedule_flag", hm.get("schedule_flag"));
                                    map.put("id", obj.getId());
                                    map.put("displayName", hm.get("displayName"));
                                    map.put("rid", hm.get("rid"));
                                    map.put("pid", hm.get("pid"));
                                    ret.add(map);
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
                    if (ret.size() > 0) {
                        return ret;
                    }
                }
                return null;
            }
        };

        return new WebAsyncTask<List>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'IPPM', 'ADMIN')")
    @RequestMapping(value = {"/saveIPTable"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveIPTable(HttpServletRequest req, @RequestBody final HashMap ipMap) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!ipMap.containsKey("rid")) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int rid = Integer.parseInt(ipMap.get("rid").toString());
                if (rid < 1) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                Revision rev = revisionService.findById(rid);
                if (rev == null) {
                    ret.put("data", "Revision Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                if (!ipMap.containsKey("data"))
                    return null;
                List list = (ArrayList) ipMap.get("data");
                if (!list.isEmpty()) {
                    //redis.multiDelete("*_ip_*");
                    iploop:
                    for (Object ipObj : list) {
                        HashMap hm = (HashMap) ipObj;
                        RevisionIP reviplink = null;
                        boolean isDeleted = false;
                        boolean isNew = false;
                        Revision ip = null;
                        int id = Integer.parseInt(hm.get("id").toString());
                        if (hm.containsKey("isDeleted")) {
                            isDeleted = (Boolean) hm.get("isDeleted");
                            reviplink = revisionIPService.findById(id);
                        } else {
                            if (hm.containsKey("isNew")) {
                                isNew = (Boolean) hm.get("isNew");
                                if (isNew) {
                                    reviplink = new RevisionIP();
                                }
                                if (!hm.containsKey("rid")) {
                                    continue iploop;
                                }
                                ip = revisionService.findById(Integer.parseInt(hm.get("rid").toString()));
                            } else {
                                reviplink = revisionIPService.findById(id);
                                if (reviplink == null) {
                                    reviplink = new RevisionIP();
                                    if (!hm.containsKey("rid")) {
                                        continue iploop;
                                    }
                                    ip = revisionService.findById(Integer.parseInt(hm.get("rid").toString()));
                                } else {
                                    ip = reviplink.getIprevision();
                                }
                            }
                            if (ip == null)
                                continue iploop;

                            reviplink.setInstanceNum(Integer.parseInt(hm.get("instances").toString()));
                            reviplink.setIprevision(ip);

                            if (isNew) {
                                reviplink.setRevision(rev);
                            }
                            revisionIPService.saveOrUpdate(reviplink);
                        }
                    }
                }
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'ADMIN')")
    @RequestMapping(value = {"/getPMOutlook"}, method = {RequestMethod.GET})
    public WebAsyncTask<HashMap> getPMOutlook(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload,
            @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<HashMap> callable = new Callable<HashMap>() {
            public HashMap call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                HashMap ret = new HashMap();
                DateTime dt = new DateTime(da);
                if (dt.getYear() == 1990) {
                    dt = null;
                }
                if (dt == null) {
                    RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                    if (rs != null) {
                        ret.put("ts", new DateTime(rs.getLast_updated_outlook_date()).toString(dfmt));
                        ret.put("outlook", "");
                        if (rs.getOutlook() != null)
                            ret.put("outlook", TextUtil.cleanHeadline(rs.getOutlook()));
                        return ret;
                    }
                } else {
                    Revision rev = revisionService.findById(rid);
                    if (rev != null) {
                        List<RevisionOutlook> outlooks = revisionOutlookService.findByRevision(rev, dt);
                        if (outlooks != null) {
                            ret.put("ts", new DateTime(outlooks.get(0).getCreatedDate()).toString(dfmt));
                            ret.put("outlook", TextUtil.cleanHeadline(outlooks.get(0).getContent()));

                            return ret;
                        }
                    }
                }
                return null;
            }
        };
        return new WebAsyncTask<HashMap>(1200000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'ADMIN')")
    @RequestMapping(value = {"/savePMOutlook"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> savePMOutlook(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!hm.containsKey("rid")) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int rid = Integer.parseInt(hm.get("rid").toString());
                if (rid < 1) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                Revision rev = revisionService.findById(rid);
                if (rev == null) {
                    ret.put("data", "Revision Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }

                String content = hm.get("value").toString().replaceAll("\\[\\d{2}\\/\\d{2}\\/\\d{2,4}\\]", "");
                RevisionOutlook outlook = new RevisionOutlook();
                outlook.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
                outlook.setContent(content);
                outlook.setRevision(rev);
                revisionOutlookService.saveOrUpdate(outlook);

                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                rs.setOutlook(content);
                rs.setLast_updated_outlook_date(new Date());
                rs = revisionSearchService.saveOrUpdate(rs);
                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };

        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    /***********************************************************************
     * Meeting Link
     * *********************************************************************/

    @RequestMapping(value = {"/getMeetingLink"}, method = {RequestMethod.GET})
    public WebAsyncTask<HashMap> getMeetingLink(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<HashMap> callable = new Callable<HashMap>() {
            public HashMap call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                HashMap ret = new HashMap();
                Revision rev = revisionService.findById(rid);
                if (rev == null)
                    throw new CustomGenericException("Revision Not found in database");
                List<Link> links = linkService.findByRevision(rev);
                if (links != null) {
                    List<HashMap> linkList = new ArrayList<HashMap>();
                    List<HashMap> meetingList = new ArrayList<HashMap>();
                    for (Link link : links) {
                        HashMap hm = new HashMap();
                        hm.put("key", link.getType().trim());
                        hm.put("url", link.getUrl().trim());
                        String name = link.getDisplay_name().replaceAll("%20", " ");
                        String[] arr_name = name.split("\\.");
                        if (arr_name.length > 0)
                            name = arr_name[0];
                        hm.put("name", name.trim());
                        hm.put("id", link.getId());
                        if (link.getCategory().toString().toLowerCase().indexOf("link") != -1) {
                            linkList.add(hm);
                        } else {
                            meetingList.add(hm);
                        }
                    }
                    ret.put("meeting", meetingList);
                    ret.put("link", linkList);
                    return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<HashMap>(1200000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveMeetingLink"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveMeetingLink(HttpServletRequest req, @RequestBody final HashMap linkMap) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!linkMap.containsKey("rid")) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int rid = Integer.parseInt(linkMap.get("rid").toString());
                if (rid < 1) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                Revision rev = revisionService.findById(rid);
                if (rev == null) {
                    ret.put("data", "Revision Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                if (!linkMap.containsKey("data"))
                    return null;
                List list = (ArrayList) linkMap.get("data");
                String cat = linkMap.get("type").toString();
                if (cat.trim().isEmpty())
                    cat = "link";
                if (!list.isEmpty()) {
                    for (Object linkObj : list) {
                        HashMap hm = (HashMap) linkObj;
                        String key = hm.get("key").toString();
                        if (key.trim().isEmpty())
                            key = "link";
                        String name = key;
                        if (hm.containsKey("name") && hm.get("name") != null && !hm.get("name").toString().trim().isEmpty()) {
                            name = hm.get("name").toString();
                        }
                        String lurl = hm.get("url").toString();
                        Link link = null;
                        boolean isDeleted = false;
                        boolean isNew = false;
                        int id = Integer.parseInt(hm.get("id").toString());
                        if (hm.containsKey("isDeleted")) {
                            isDeleted = (Boolean) hm.get("isDeleted");
                            if (isDeleted) {
                                linkService.delete(id);
                            }
                        } else {
                            if (hm.containsKey("isNew")) {
                                isNew = (Boolean) hm.get("isNew");
                                if (isNew) {
                                    link = new Link();
                                }
                            } else {
                                link = linkService.findById(id);
                                if (link == null) {
                                    link = new Link();
                                }
                            }
                            link.setUrl(lurl);
                            link.setDisplay_name(name);
                            link.setType(key);
                            if (isNew) {
                                link.setRevision(rev);
                                link.setCategory(ProjectConstant.EnumLinkCategory.valueOf(cat.toUpperCase()));
                            }
                            linkService.saveOrUpdate(link);
                        }
                    }
                }
                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    /***********************************************************************
     * Headline
     * *********************************************************************/

    @RequestMapping(value = {"/getHeadline"}, method = {RequestMethod.GET})
    public WebAsyncTask<HashMap> getHeadline(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload,
            @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<HashMap> callable = new Callable<HashMap>() {
            public HashMap call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                HashMap ret = new HashMap();
                DateTime dt = new DateTime(da);
                if (dt.getYear() == 1990) {
                    dt = null;
                }
                HeadlineSearch hls = headlineSearchService.findByRevision(rid, dt);
                if (hls != null) {
                    DateTime hlts = new DateTime(hls.getLast_updated_date());
                    String hltss = "<i>Updated on " + hlts.toString(dfmt) + "</i>";
                    if (dt == null) {
                        DateTime currentdt = new DateTime().withTimeAtStartOfDay();
                        long diff = (currentdt.getMillis() - hlts.getMillis()) / (1000 * ProjectConstant.CacheTimeout.DAY.getSecond());
                        if (diff > 14) {
                            hltss = "<i class='text-danger'>Updated on " + hlts.toString(dfmt) + "</i>";
                        }
                    }
                    ret.put("hlts", hltss);
                    ret.put("headline", "");
                    if (hls.getHeadline() != null) {
                        String hlstring = TextUtil.cleanHeadline(hls.getHeadline());
                        String[] hll = hlstring.split("<hr>");
                        ret.put("headline", hll);
                    }
                    return ret;
                }
                return null;
            }
        };

        return new WebAsyncTask<HashMap>(1200000, callable);
    }

    @RequestMapping(value = {"/getHeadlineSnapshot"}, method = {RequestMethod.GET})
    public WebAsyncTask<LinkedHashSet> getHeadlineSnapshot(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid) {
        Callable<LinkedHashSet> callable = new Callable<LinkedHashSet>() {
            public LinkedHashSet call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                Set<String> dts = headlineSearchService.getDistinctValue(rid, "last_updated_date");
                List list = new ArrayList(dts);
                Collections.sort(list, Collections.reverseOrder());
                if (list.size() > 52)
                    list = list.subList(0, 52);
                LinkedHashSet ret = new LinkedHashSet(list);
                return ret;
            }
        };

        return new WebAsyncTask<LinkedHashSet>(1200000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveHeadline"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveHeadline(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!hm.containsKey("rid")) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int rid = Integer.parseInt(hm.get("rid").toString());
                if (rid < 1) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                Revision rev = revisionService.findById(rid);
                if (rev == null) {
                    ret.put("data", "Revision Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }

                ProjectConstant.EnumHeadlineStage stage = ProjectConstant.EnumHeadlineStage.PLANNING;
                ProjectConstant.EnumIndicatorStatus schedule = ProjectConstant.EnumIndicatorStatus.BLACK;
                ProjectConstant.EnumIndicatorStatus resource = ProjectConstant.EnumIndicatorStatus.BLACK;
                ProjectConstant.EnumIndicatorStatus budget = ProjectConstant.EnumIndicatorStatus.BLACK;
                ProjectConstant.EnumIndicatorStatus prediction = ProjectConstant.EnumIndicatorStatus.BLACK;

                HeadlineSearch hls = headlineSearchService.findByRevision(rid, null);
                if (hls != null) {
                    stage = ProjectConstant.EnumHeadlineStage.valueOf(hls.getStage().toUpperCase());
                    schedule = ProjectConstant.EnumIndicatorStatus.valueOf(hls.getSchedule_flag().toUpperCase());
                    resource = ProjectConstant.EnumIndicatorStatus.valueOf(hls.getResource_flag().toUpperCase());
                    budget = ProjectConstant.EnumIndicatorStatus.valueOf(hls.getBudget_flag().toUpperCase());
                    prediction = ProjectConstant.EnumIndicatorStatus.valueOf(hls.getPrediction_flag().toUpperCase());
                }

                String value = hm.get("value").toString().replaceAll("\\[\\d{2}\\/\\d{2}\\/\\d{2,4}\\]", "");
                if (value.replaceAll("[0-9]", "").trim().isEmpty()) {
                    value = "";
                }

//                rev = revisionService.saveOrUpdate(rev);

                //create new headline
                Headline headline = new Headline();
                headline.setHeadline(value);
                headline.setRevision(rev);
                headline.setStage(stage);
                headline.setBudget_flag(budget);
                headline.setPrediction_flag(prediction);
                headline.setResource_flag(resource);
                headline.setSchedule_flag(schedule);
                headline.setIsActive(rev.getIsActive());
                headline = headlineService.saveOrUpdate(headline);


                hls.setRevision_name(rev.getName());
                hls.setRevision_id(rid);
                hls.setHeadline(value);
                hls.setLast_updated_date(headline.getLastUpdatedDate());
                hls.setId(Integer.toString(headline.getId()));
                headlineSearchService.saveOrUpdate(hls);


                //publish event to clear cache front page and report
                headlineSaveEventPublisher.publish(new HeadlineSaveEvent(rid));


                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    /***********************************************************************
     * Weekly Update Note
     * *********************************************************************/

    @RequestMapping(value = {"/getRemark"}, method = {RequestMethod.GET})
    public WebAsyncTask<HashMap> getRemark(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "gid", defaultValue = "0") final int gid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload,
            @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<HashMap> callable = new Callable<HashMap>() {
            @Override
            public HashMap call() {
                if (rid < 0)
                    throw new IDNotFoundException(rid, "revision");
                if (gid < 0)
                    throw new IDNotFoundException(gid, "category");
                DateTime dt = new DateTime(da);
                if (dt.getYear() == 1990) {
                    dt = null;
                }
                HashMap ret = new HashMap();
                IndicatorGroupSearch igs = indicatorGroupSearchService.findByGroupId(gid, dt);
                DateTime remarkts = new DateTime(igs.getLast_updated_date());
                ret.put("remark", TextUtil.cleanRemark(igs.getRemark()));
                ret.put("ts", remarkts.toString(dfmt));
                return ret;
            }
        };
        return new WebAsyncTask<HashMap>(120000, callable);
    }

    @RequestMapping(value = {"/getRemarkSnapshot"}, method = {RequestMethod.GET})
    public WebAsyncTask<LinkedHashSet> getRemarkSnapshot(
            HttpServletRequest req,
            @RequestParam(value = "gid", defaultValue = "0") final int gid) {
        Callable<LinkedHashSet> callable = new Callable<LinkedHashSet>() {
            @Override
            public LinkedHashSet call() {
                if (gid < 1)
                    throw new IDNotFoundException(gid, "category");
                Set<String> dts = indicatorGroupSearchService.getDistinctValue(gid, "igroup_id", "last_updated_date");
                List list = new ArrayList(dts);
                Collections.sort(list, Collections.reverseOrder());
                if (list.size() > 52)
                    list = list.subList(0, 52);
                LinkedHashSet ret = new LinkedHashSet(list);
                return ret;
            }
        };

        return new WebAsyncTask<LinkedHashSet>(1200000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveRemark"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveRemark(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!hm.containsKey("gid")) {
                    ret.put("data", "Category Group is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int gid = Integer.parseInt(hm.get("gid").toString());
                if (gid < 1) {
                    ret.put("data", "Category Group is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                String remark = hm.get("remark").toString();

                IndicatorGroupSearch igs = indicatorGroupSearchService.findByGroupId(gid);

                IGroup g = iGroupService.findById(gid);
                if (g == null) {
                    ret.put("data", "Category Group Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                IGroupHistory gh = new IGroupHistory();
                gh.setIGroup(g);
                gh.setStatus(ProjectConstant.EnumIndicatorStatus.valueOf(igs.getStatus().toUpperCase()));
                gh.setRemark(remark);
                gh = iGroupHistoryService.saveOrUpdate(gh);

                igs.setId(Integer.toString(gh.getId()));
                igs.setIgroup_id(g.getId());
                igs.setIgroup_name(g.getName().toLowerCase().trim());
                igs.setLast_updated_date(gh.getLastUpdatedDate());
                igs.setOrder_num(igs.getOrder_num());
                igs.setRemark(remark);
                igs.setStatus(igs.getStatus());
                igs.setRevision_id(igs.getRevision_id());
                igs.setRevision_name(igs.getRevision_name());
                indicatorGroupSearchService.saveOrUpdate(igs);

                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);

            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    /***********************************************************************
     * Indicator Category
     * *********************************************************************/

    @RequestMapping(value = {"/getIndicatorCategoryList"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getIndicatorCategoryList(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload,
            @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");
                DateTime dt = new DateTime(da).withTimeAtStartOfDay();
                if (dt.getYear() == 1990) {
                    dt = null;
                }
                List ret = new ArrayList();
                final DateTime lastResetDate = DateResetUtil.getResetDate(dt);
                Set<String> allGroup = indicatorGroupSearchService.getDistinctValue(rid, "revision_id", "igroup_name");
                if (allGroup != null && !allGroup.isEmpty()) {
                    for (String groupName : allGroup) {
                        HashMap hm = new HashMap();
                        IndicatorGroupSearch igs = indicatorGroupSearchService.findByRevision(rid, groupName.toLowerCase().trim(), dt);
                        if (igs != null) {
                            String remark = "";
                            hm.put("name", igs.getIgroup_name());
                            hm.put("id", igs.getIgroup_id());
                            hm.put("order", Integer.toString(igs.getOrder_num()));
                            hm.put("status", "black");
                            hm.put("cat_btn_color", "green");
                            if (igs.getRemark() != null) {
                                remark = TextUtil.cleanRemark(igs.getRemark());
                                hm.put("status", igs.getStatus().toString());
                                hm.put("cat_btn_color", igs.getStatus().toString());
                                DateTime dttemp = new DateTime(igs.getLast_updated_date());
                                if (dttemp.getMillis() < lastResetDate.getMillis()) {
                                    hm.put("status", "black");
                                    hm.put("cat_btn_color", "green");
                                }
                                DateTime remarkts = new DateTime(igs.getLast_updated_date());
                                hm.put("ts", remarkts.toString(dfmt));
                            }
                            hm.put("remark", remark);
                            ret.add(hm);
                        }

                    }
                }
                if (ret.size() > 0) {
                    return ret;
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(1200000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/addIndicatorCategory"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> addIndicatorCategory(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!hm.containsKey("rid")) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int rid = Integer.parseInt(hm.get("rid").toString());
                if (rid < 1) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                Revision rev = revisionService.findById(rid);
                if (rev == null) {
                    ret.put("data", "Revision Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }

                if (!hm.containsKey("name") || hm.get("name").toString().trim().isEmpty()) {
                    ret.put("data", "Category Name can not be empty");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                String name = "";
                name = hm.get("name").toString().trim();
                if (name.equalsIgnoreCase("project")) {
                    ret.put("data", "Please Choose another name");
                    ret.put("code", HttpStatus.CHECKPOINT);
                    return ResponseEntity.status(HttpStatus.CHECKPOINT).body(ret);
                }
                IGroup g = new IGroup();
                name = hm.get("name").toString();
                Integer orderNum = 1;
                if (hm.containsKey("order")) {
                    orderNum = Integer.parseInt(hm.get("order").toString());
                }
                g.setName(name);
                g.setOrderNum(orderNum);
                g.setRevision(rev);

                g = iGroupService.saveOrUpdate(g);
                ret.put("gid", Integer.toString(g.getId()));

                IGroupHistory igh = new IGroupHistory();
                igh.setIGroup(g);
                igh.setRemark("");
                igh.setStatus(ProjectConstant.EnumIndicatorStatus.BLACK);
                igh = iGroupHistoryService.saveOrUpdate(igh);

                IndicatorGroupSearch igs = new IndicatorGroupSearch();
                igs.setId(Integer.toString(igh.getId()));
                igs.setIgroup_id(g.getId());
                igs.setIgroup_name(name.toLowerCase().trim());
                igs.setLast_updated_date(igh.getLastUpdatedDate());
                igs.setOrder_num(orderNum);
                igs.setRemark("");
                igs.setRevision_id(rid);
                igs.setRevision_name(rev.getName());
                igs.setStatus("black");
                indicatorGroupSearchService.saveOrUpdate(igs);

                ret.put("code", HttpStatus.CREATED);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/removeIndicatorCategory"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> removeIndicatorCategory(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                HashMap ret = new HashMap();
                if (!hm.containsKey("gid")) {
                    ret.put("data", "Category Group is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int gid = Integer.parseInt(hm.get("gid").toString());
                if (gid < 1) {
                    ret.put("data", "Category Group is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }

                int rid = Integer.parseInt(hm.get("rid").toString());
                IGroup g = iGroupService.findById(gid);
                if (g == null) {
                    ret.put("data", "Category Group Not found in database");
                    ret.put("code", HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                }
                if (g.getName().equalsIgnoreCase("project")) {
                    ret.put("data", "Can not remove Project Category");
                    ret.put("code", HttpStatus.CHECKPOINT);
                    return ResponseEntity.status(HttpStatus.CHECKPOINT).body(ret);
                } else {
                    iGroupService.delete(gid);
                    List<IndicatorGroupSearch> igsl = indicatorGroupSearchService.findAllByGroupId(gid);
                    if (igsl != null)
                        for (IndicatorGroupSearch igs : igsl)
                            indicatorGroupSearchService.delete(igs.getId());
                    List<IndicatorTaskSearch> itsl = indicatorTaskSearchService.findByIndicatorGroup(gid);
                    if (itsl != null) {
                        for (IndicatorTaskSearch its : itsl)
                            indicatorTaskSearchService.delete(its.getId());
                    }
                    if (igsl.get(0).getIgroup_name().equalsIgnoreCase("fcs")) {
                        IndicatorGroupSearch igs = indicatorGroupSearchService.findByRevision(rid, "project", new DateTime());
                        if (igs != null)
                            ret.put("pgid", Integer.toString(igs.getIgroup_id()));
                    }
                }
                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    /***********************************************************************
     * Indicator Task
     * *********************************************************************/

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/removeIndicatorTask"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> removeIndicatorTask(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            @Override
            public ResponseEntity call() {
                HashMap ret = new HashMap();
                if (!hm.containsKey("tid")) {
                    ret.put("data", "Task is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }

                int tid = Integer.parseInt(hm.get("tid").toString());
                ITask t = iTaskService.findById(tid);
                if (t == null) {
                    ret.put("data", "Task not found in database");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                IGroup g = t.getIGroup();
                if (g == null) {
                    ret.put("data", "Category Group Not found in database");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                Revision rev = g.getRevision();
                Program p = rev.getProgram();
                if (g.getName().equalsIgnoreCase("project") && !p.getType().equals(ProjectConstant.EnumProgramType.IP)) {
                    ret.put("data", "Can not remove Project Task");
                    ret.put("code", HttpStatus.CHECKPOINT);
                    return ResponseEntity.status(HttpStatus.CHECKPOINT).body(ret);
                }
                iTaskService.delete(tid);

                List<IndicatorTaskSearch> itsl = indicatorTaskSearchService.findAllByTask(tid);
                if (itsl != null) {
                    for (IndicatorTaskSearch its : itsl)
                        indicatorTaskSearchService.delete(its.getId());
                }
                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(1200000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveIndicatorTask"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveIndicatorTask(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {

                HashMap ret = new HashMap();
                ret.put("key_milestone", "false");
                //check data before processing
                if (!hm.containsKey("gid")) {
                    ret.put("data", "Category is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                int gid = Integer.parseInt(hm.get("gid").toString());
                if (gid < 1) {
                    ret.put("data", "Category not found in database");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }

                //declare variable
                final DateTime currentdt = new DateTime();
                ProjectConstant.EnumHeadlineStage stage = null;
                ITask t = null;
                boolean isNew = false; //check to see if new
                Integer orderNum = -1;

                ProjectConstant.EnumIndicatorStatus gstatus = ProjectConstant.EnumIndicatorStatus.BLACK;
                ProjectConstant.EnumIndicatorStatus tstatus = ProjectConstant.EnumIndicatorStatus.BLACK;
                String name = "";
                String note = "";
                Revision rev = null;
                Program p = null;

                IGroup g = iGroupService.findById(gid);

                if (hm.containsKey("isNew")) {
                    isNew = (Boolean) hm.get("isNew");
                }
                if (!isNew) {
                    if (!hm.containsKey("tid")) {
                        ret.put("data", "Task is missing in request");
                        ret.put("code", HttpStatus.EXPECTATION_FAILED);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                    }
                    int tid = Integer.parseInt(hm.get("tid").toString());
                    t = iTaskService.findById(tid);
                    if (t == null) {
                        ret.put("data", "Task not found in database");
                        ret.put("code", HttpStatus.EXPECTATION_FAILED);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                    }
                    //task have more priority than group
                    g = t.getIGroup();
                }

                //check for new group mis match
                //Qumran UX case
                if (hm.containsKey("rid")) {
                    int rid = Integer.parseInt(hm.get("rid").toString());
                    if (rid < 1) {
                        ret.put("data", "Revision not found in database");
                        ret.put("code", HttpStatus.EXPECTATION_FAILED);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                    }
                    rev = revisionService.findById(rid);
                    Revision group_by_revision = g.getRevision();
                    if (group_by_revision.getId() != rid) {
                        g = iGroupService.findByName(rev, g.getName());
                    }
                }
                if (g == null) {
                    ret.put("data", "Category not found in database");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                if (rev == null) {
                    rev = g.getRevision();
                    if (rev == null) {
                        ret.put("data", "Revision not found in database");
                        ret.put("code", HttpStatus.EXPECTATION_FAILED);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                    }
                }
                p = rev.getProgram();
                if (p == null) {
                    ret.put("data", "Program not found in database");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }

                //get data out of client map
                if (hm.containsKey("order")) {
                    orderNum = Integer.parseInt(hm.get("order").toString());
                }
                if (hm.containsKey("name")) {
                    name = hm.get("name").toString();
                }
                if (hm.containsKey("note")) {
                    note = hm.get("note").toString();
                }
                if (hm.containsKey("tstatus") && hm.get("tstatus").toString().trim().length() > 1) {
                    tstatus = ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("tstatus").toString().toUpperCase());
                }
                ret.put("tstatus", tstatus.toString().toLowerCase());
                if (hm.containsKey("gstatus") && hm.get("gstatus").toString().trim().length() > 1) {
                    gstatus = ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("gstatus").toString().toUpperCase());
                }
                ret.put("gstatus", gstatus.toString().toLowerCase());
                if (g.getName().equalsIgnoreCase("project")) {
                    if (t.getName().toLowerCase().indexOf("pc") == 0 ||
                            t.getName().toLowerCase().indexOf("t/o") == 0 ||
                            t.getName().toLowerCase().indexOf("eng") == 0 ||
                            t.getName().toLowerCase().indexOf("pra") == 0) {
                        ret.put("key_milestone", "true");
                    }
                }

                //save group history
                IndicatorGroupSearch igs = indicatorGroupSearchService.findByGroupId(g.getId());
                if (igs != null) {
                    IGroupHistory gh = new IGroupHistory();
                    gh.setIGroup(g);
                    gh.setStatus(gstatus);
                    gh.setRemark(igs.getRemark());
                    gh = iGroupHistoryService.saveOrUpdate(gh);

                    igs.setId(Integer.toString(gh.getId()));
                    igs.setStatus(gstatus.toString().toLowerCase());
                    igs.setLast_updated_date(gh.getLastUpdatedDate());
                    if (igs.getRemark() == null)
                        igs.setRemark("");
                    igs = indicatorGroupSearchService.saveOrUpdate(igs);
                } else {
                    //if there is no task history exists
                    IGroupHistory gh = new IGroupHistory();
                    gh.setIGroup(g);
                    gh.setStatus(gstatus);
                    gh.setRemark("");
                    gh = iGroupHistoryService.saveOrUpdate(gh);

                    igs = new IndicatorGroupSearch();
                    igs.setId(Integer.toString(gh.getId()));
                    igs.setStatus(gstatus.toString().toLowerCase());
                    igs.setLast_updated_date(gh.getLastUpdatedDate());
                    igs.setIgroup_id(g.getId());
                    igs.setIgroup_name(g.getName().toLowerCase().trim());
                    igs.setOrder_num(g.getOrderNum());
                    igs.setRemark("");
                    igs.setRevision_id(rev.getId());
                    igs.setRevision_name(rev.getName().toLowerCase());
                    igs = indicatorGroupSearchService.saveOrUpdate(igs);
                }

                //save task
                if (isNew) {
                    t = new ITask();
                    if (orderNum == -1) {
                        orderNum = 0;
                    }
                    t.setOrderNum(orderNum);
                    t.setName(name);
                    t.setNameInReport(name);
                    t.setIGroup(g);
                    t = iTaskService.saveOrUpdate(t);
                } else {
                    if (!g.getName().equalsIgnoreCase("project") ||
                            p.getType().equals(ProjectConstant.EnumProgramType.IP)) {
                        if (orderNum != -1) {
                            t.setOrderNum(orderNum);
                        }
                        //change name case
                        if (!t.getName().equalsIgnoreCase(name.trim())) {
                            List<IndicatorTaskSearch> itsl = indicatorTaskSearchService.findAllByTask(t.getId());
                            if (itsl != null && !itsl.isEmpty()) {
                                for (IndicatorTaskSearch its : itsl) {
                                    its.setTask_name(name.toLowerCase().trim());
                                    indicatorTaskSearchService.saveOrUpdate(its);
                                }
                            }
                        }
                        t.setName(name);
                        t = iTaskService.saveOrUpdate(t);
                    }
                }
                //save task history
                IndicatorTaskSearch its = indicatorTaskSearchService.findByIndicatorTask(t.getId());
                //save new task if note/status change only
                if (its == null || !its.getNote().equalsIgnoreCase(note) ||
                        !its.getStatus().equalsIgnoreCase(tstatus.toString().toLowerCase())) {
                    ITaskHistory th = new ITaskHistory();
                    th.setITask(t);
                    th.setNote(note);
                    th.setStatus(tstatus);
                    th = iTaskHistoryService.saveOrUpdate(th);

                    if (its == null)
                        its = new IndicatorTaskSearch();
                    its.setId(Integer.toString(th.getId()));
                    its.setIgroup_id(g.getId());
                    its.setIgroup_name(g.getName().toString().toLowerCase());
                    its.setLast_updated_date(th.getLastUpdatedDate());
                    its.setNote(note);
                    its.setOrder_num(orderNum);
                    its.setRevision_id(rev.getId());
                    its.setRevision_name(rev.getName().toLowerCase());
                    its.setStatus(tstatus.toString().toLowerCase());
                    its.setTask_id(t.getId());
                    its.setTask_name(t.getName().toLowerCase().trim());
                    its.setTask_name_in_report(t.getNameInReport().toLowerCase().trim());
                    its = indicatorTaskSearchService.saveOrUpdate(its);
                }

//				/*save task with the same name
//				 * async method -- fire and dont wait for return
//					*/

                //save date
                for (ProjectConstant.EnumIndicatorTrackingDateType ttype : ProjectConstant.EnumIndicatorTrackingDateType.values()) {
                    for (ProjectConstant.EnumIndicatorEndingDateType etype : ProjectConstant.EnumIndicatorEndingDateType.values()) {
                        String date_key = ttype.toString().toLowerCase() + WordUtils.capitalizeFully(etype.toString());
                        if (hm.containsKey(date_key)) {
                            String datename = ttype.toString().toLowerCase() + "_" + etype.toString().toLowerCase();
                            HashMap dmap = (HashMap) hm.get(date_key);
                            String dvalue = dmap.get("value").toString().trim();
                            String comment = dmap.get("comment").toString();
                            ProjectConstant.EnumIndicatorStatus dhstatus = ProjectConstant.EnumIndicatorStatus.BLACK;
                            DateTime ddt = DateUtil.toDate(dvalue).withTimeAtStartOfDay();

                            //get stage of project
                            if (p.getType().equals(ProjectConstant.EnumProgramType.CHIP)) {
                                if (ddt.getYear() > 1990) {
                                    if (ttype.equals(ProjectConstant.EnumIndicatorTrackingDateType.ACTUAL)) {
                                        if (etype.equals(ProjectConstant.EnumIndicatorEndingDateType.END)) {
                                            if (stageTaskMap.containsKey(t.getName().toUpperCase())) {
                                                stage = stageTaskMap.get(t.getName().toUpperCase());
                                            }
                                        }
                                    }
                                }
                            }
                            IndicatorDateSearch ids = indicatorDateSearchService.findByIndicatorTask(t.getId(), datename);

                            if (dmap.containsKey("dhstatus") && !dmap.get("dhstatus").toString().trim().isEmpty()) {
                                dhstatus = ProjectConstant.EnumIndicatorStatus.valueOf(dmap.get("dhstatus").toString().toUpperCase());
                            }
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
                }

                //handle fcs date for customer
                if (p.getType().equals(ProjectConstant.EnumProgramType.CUSTOMER) &&
                        g.getName().equalsIgnoreCase("fcs")) {
                    IndicatorGroupSearch pigs = indicatorGroupSearchService.findByRevision(rev.getId(), "project", null);
                    if (pigs != null)
                        ret.put("pgid", Integer.toString(pigs.getIgroup_id()));
                }

                //save Headline
                //update status of headline
                if (g.getName().equalsIgnoreCase("project")) {
                    HeadlineSearch hls = headlineSearchService.findByRevision(rev.getId(), null);
                    Headline hl = new Headline();
                    hl.setHeadline("");
                    hl.setIsActive(rev.getIsActive());
                    hl.setBudget_flag(ProjectConstant.EnumIndicatorStatus.BLACK);
                    hl.setPrediction_flag(ProjectConstant.EnumIndicatorStatus.BLACK);
                    hl.setResource_flag(ProjectConstant.EnumIndicatorStatus.BLACK);
                    hl.setSchedule_flag(gstatus);//set new milestone status
                    hl.setRevision(rev);
                    if (hls != null) {
                        hl.setBudget_flag(ProjectConstant.EnumIndicatorStatus.valueOf(hls.getBudget_flag().toUpperCase()));
                        hl.setResource_flag(ProjectConstant.EnumIndicatorStatus.valueOf(hls.getResource_flag().toUpperCase()));
                        hl.setPrediction_flag(ProjectConstant.EnumIndicatorStatus.valueOf(hls.getPrediction_flag().toUpperCase()));
                        hl.setHeadline(hls.getHeadline());
                        hl.setIsActive(ProjectConstant.EnumProgramStatus.valueOf(hls.getStatus().toUpperCase()));
                        hl.setStage(ProjectConstant.EnumHeadlineStage.valueOf(hls.getStage().toUpperCase()));
                        if (stage != null) {
                            boolean isValidStage = isValidStage(ProjectConstant.EnumHeadlineStage.valueOf(hls.getStage().toUpperCase()), stage);
                            if (isValidStage) {
                                hl.setStage(stage);
                            }
                        }
                    } else {
                        if (stage != null) {
                            hl.setStage(stage);
                        }
                    }
                    hl = headlineService.saveOrUpdate(hl);

                    hls.setSchedule_flag(gstatus.toString().toLowerCase());
                    hls.setId(Integer.toString(hl.getId()));
                    hls.setLast_updated_date(hl.getLastUpdatedDate());
                    if (stage != null) {
                        hls.setStage(stage.toString().toLowerCase());
                    }
                    headlineSearchService.saveOrUpdate(hls);
                }

                HashMap tmp = new HashMap();
                tmp.putAll(hm);
                tmp.put("group_name", g.getName());
                tmp.put("pid", p.getId());
                taskSaveEventPublisher.publish(new TaskWithSameNameSaveEvent(tmp));

                String btn_color = gstatus.toString().toLowerCase();
                if (btn_color.equals("black"))
                    btn_color = "green";
                ret.put("revision_btn_color", btn_color);


                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    /***********************************************************************
     * Indicator Milestone
     * *********************************************************************/

    @RequestMapping(value = {"/getFrontPageMilestone"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getFrontPageMilestone(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        Callable<List> callable = new Callable<List>() {
            @Override
            public List call() {
                if (rid < 1)
                    throw new IDNotFoundException(rid, "revision");

                List ret = new ArrayList();
                RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                if (rs != null) {
                    IndicatorGroupSearch igs = indicatorGroupSearchService.findByRevision(rid, "project", null);
                    if (igs != null) {
                        List list = indicatorService.getIndicatorByCategory(igs.getIgroup_id(), null);
                        if (list != null && list.size() > 0) {
                            ret = indicatorService.getFrontPageMilestone(list);
                            if (ret != null && ret.size() > 0) {
                                return ret;
                            }
                        }
                    }
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(120000, callable);
    }

    @RequestMapping(value = {"/getIndicatorByCategory"}, method = {RequestMethod.GET})
    public WebAsyncTask<List> getIndicatorByCategory(
            HttpServletRequest req,
            @RequestParam(value = "rid", defaultValue = "0") final int rid,
            @RequestParam(value = "gid", defaultValue = "0") final int gid,
            @RequestParam(value = "reload", defaultValue = "0") final int reload,
            @RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        Callable<List> callable = new Callable<List>() {
            public List call() {
                if (gid < 1)
                    throw new IDNotFoundException(gid, "category");
                List ret = new ArrayList();
                DateTime dt = new DateTime(da);
                if (dt.getYear() == 1990) {
                    dt = new DateTime();
                }
                IndicatorGroupSearch igs = indicatorGroupSearchService.findByGroupId(gid);
                if (igs == null)
                    throw new CustomGenericException("Category Not found in database");
                List list = indicatorService.getIndicatorByCategory(gid, dt);
                if (list != null && list.size() > 0) {
                    return list;
                }
                return null;
            }
        };
        return new WebAsyncTask<List>(1200000, callable);
    }

    @RequestMapping(value = {"/getMilestoneSnapshot"}, method = {RequestMethod.GET})
    public WebAsyncTask<LinkedHashSet> getMilestoneSnapshot(
            HttpServletRequest req,
            @RequestParam(value = "gid", defaultValue = "0") final int gid) {
        Callable<LinkedHashSet> callable = new Callable<LinkedHashSet>() {
            @Override
            public LinkedHashSet call() {
                if (gid < 1)
                    throw new IDNotFoundException(gid, "category");
                Set<String> dts = indicatorTaskSearchService.getDistinctValue(gid, "igroup_id", "last_updated_date");
                if (dts == null) {
                    dts = new LinkedHashSet<String>();
                }
                Set<String> dts2 = indicatorDateSearchService.getDistinctValue(gid, "igroup_id", "last_updated_date");
                if (dts2 != null)
                    dts.addAll(dts2);
                List list = new ArrayList(dts);
                Collections.sort(list, Collections.reverseOrder());
                if (list.size() > 52)
                    list = list.subList(0, 52);
                LinkedHashSet ret = new LinkedHashSet(list);
                return ret;
            }
        };

        return new WebAsyncTask<LinkedHashSet>(1200000, callable);
    }

    /***********************************************************************
     * Settings
     * *********************************************************************/

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveRevisionFlag"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveSettings(final HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!hm.containsKey("rid")) {
                    ret.put("data", "Revision is missing in request");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                if (hm.containsKey("status") || hm.containsKey("schedule") ||
                        hm.containsKey("escalation") || hm.containsKey("stage")) {
                    int rid = Integer.parseInt(hm.get("rid").toString());
                    if (rid < 1) {
                        ret.put("data", "Revision is missing in request");
                        ret.put("code", HttpStatus.EXPECTATION_FAILED);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                    }
                    Revision rev = revisionService.findById(rid);
                    if (rev == null) {
                        ret.put("data", "Revision Not found in database");
                        ret.put("code", HttpStatus.BAD_REQUEST);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                    }
                    RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));

                    IndicatorGroupSearch igs = indicatorGroupSearchService.findByRevision(rid, "project", null);
                    if (igs == null) {
                        ret.put("data", "Category Group Not found in database");
                        ret.put("code", HttpStatus.BAD_REQUEST);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                    }
                    IGroup ig = iGroupService.findById(igs.getIgroup_id());
                    HeadlineSearch hls = headlineSearchService.findByRevision(rid, null);
                    ProjectConstant.EnumIndicatorStatus scolor = null;
                    ProjectConstant.EnumIndicatorStatus ecolor = null;
                    ProjectConstant.EnumProgramStatus rstatus = ProjectConstant.EnumProgramStatus.ACTIVE;
                    ProjectConstant.EnumHeadlineStage stage = null;
                    boolean isProtected = false;
                    boolean status = true;

                    if (igs != null) {
                        if (hm.containsKey("protected")) {
                            isProtected = Boolean.parseBoolean(hm.get("protected").toString().toLowerCase());
                        }
                        if (hm.containsKey("schedule")) {
                            scolor = ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("schedule").toString().toUpperCase());
                        } else {
                            scolor = ProjectConstant.EnumIndicatorStatus.valueOf(igs.getStatus().toString().toUpperCase());
                        }
                        if (hm.containsKey("escalation")) {
                            ecolor = ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("escalation").toString().toUpperCase());
                        } else {
                            ecolor = ProjectConstant.EnumIndicatorStatus.valueOf(hls.getPrediction_flag().toString().toUpperCase());
                        }
                        if (hm.containsKey("stage")) {
                            stage = ProjectConstant.EnumHeadlineStage.valueOf(hm.get("stage").toString().toUpperCase().trim().replaceAll("\\s", "_"));
                        } else {
                            stage = ProjectConstant.EnumHeadlineStage.valueOf(hls.getStage().toString().toUpperCase());
                        }
                        if (hm.containsKey("status")) {
                            Object sobj = hm.get("status");
                            if (sobj instanceof String) {
                                String statusString = hm.get("status").toString();
                                if (!statusString.equalsIgnoreCase("active")) {
                                    status = false;
                                }
                            } else if (sobj instanceof Boolean) {
                                status = (Boolean) hm.get("status");
                            }
                            if (status) {
                                rstatus = ProjectConstant.EnumProgramStatus.ACTIVE;
                            } else {
                                rstatus = ProjectConstant.EnumProgramStatus.NON_ACTIVE;
                            }
                        } else {
                            rstatus = ProjectConstant.EnumProgramStatus.valueOf(hls.getStatus().toString().toUpperCase());
                        }
                        if (stage.equals(ProjectConstant.EnumHeadlineStage.NON_ACTIVE) ||
                                stage.equals(ProjectConstant.EnumHeadlineStage.INACTIVE) ||
                                stage.equals(ProjectConstant.EnumHeadlineStage.PRA) ||
                                stage.equals(ProjectConstant.EnumHeadlineStage.CANCELLED)) {
                            if (status) {
                                if (rs.getType().equalsIgnoreCase("customer")) {
                                    stage = ProjectConstant.EnumHeadlineStage.CUSTOMER;
                                } else if (rs.getType().equalsIgnoreCase("software")) {
                                    stage = ProjectConstant.EnumHeadlineStage.SOFTWARE;
                                } else {
                                    hls = headlineSearchService.findLastNonActiveStage(rid);
                                    if (hls != null) {
                                        stage = ProjectConstant.EnumHeadlineStage.valueOf(hls.getStage().toUpperCase());
                                    } else {
                                        stage = ProjectConstant.EnumHeadlineStage.PLANNING;
                                    }
                                    scolor = ProjectConstant.EnumIndicatorStatus.BLACK;
                                }
                            } else {
                                rstatus = ProjectConstant.EnumProgramStatus.NON_ACTIVE;
                            }
                        }
                    }

                    IGroupHistory igh = new IGroupHistory();
                    igh.setIGroup(ig);
                    igh.setRemark(igh.getRemark());
                    igh.setStatus(scolor);
                    igh = iGroupHistoryService.saveOrUpdate(igh);

                    igs.setId(Integer.toString(igh.getId()));
                    igs.setLast_updated_date(igh.getLastUpdatedDate());
                    igs.setStatus(scolor.toString().toLowerCase());
                    indicatorGroupSearchService.saveOrUpdate(igs);

                    Headline hl = new Headline();
                    hl.setBudget_flag(ProjectConstant.EnumIndicatorStatus.BLACK);
                    hl.setHeadline(hls.getHeadline().replaceAll("<hr>(\\s)*?$", ""));
                    hl.setIsActive(rstatus);
                    hl.setPrediction_flag(ecolor);
                    hl.setResource_flag(scolor);
                    hl.setRevision(rev);
                    hl.setSchedule_flag(scolor);
                    hl.setStage(stage);
                    hl = headlineService.saveOrUpdate(hl);

                    hls.setId(Integer.toString(hl.getId()));
                    hls.setSchedule_flag(scolor.toString().toLowerCase());
                    hls.setStage(stage.toString().toLowerCase());
                    hls.setStatus(rstatus.toString().toLowerCase());
                    hls.setPrediction_flag(ecolor.toString().toLowerCase());
                    hls.setLast_updated_date(hl.getLastUpdatedDate());
                    headlineSearchService.saveOrUpdate(hls);

                    rev.setIsActive(rstatus);
                    rev.setIsProtected(isProtected);
                    rev = revisionService.saveOrUpdate(rev);

                    rs.setIs_active(status);
                    rs.setIs_protected(isProtected);
                    rs = revisionSearchService.saveOrUpdate(rs);

                    if (hm.containsKey("program")) {
                        Program p = rev.getProgram();
                        String pname = hm.get("program").toString();
                        if (!p.getName().equalsIgnoreCase(pname) && !pname.trim().isEmpty()) {
                            p.setName(pname);
                            p = programService.saveOrUpdate(p);
                            rs.setProgram_name(pname.toLowerCase());
                            rs = revisionSearchService.saveOrUpdate(rs);
                        }
                    }
                    if (hm.containsKey("revision")) {
                        String rname = hm.get("revision").toString();
                        if (!rs.getRev_name().equalsIgnoreCase(rname) && !rname.trim().isEmpty()) {
                            rev.setName(rname);
                            rev = revisionService.saveOrUpdate(rev);
                            rs.setRev_name(rname.toLowerCase());
                            rs = revisionSearchService.saveOrUpdate(rs);
                        }
                    }
                    if (hm.containsKey("base")) {
                        String base = hm.get("base").toString();
                        if (!rs.getBase_num().equalsIgnoreCase(base)) {
                            Program p = rev.getProgram();
                            p.setBaseNum(base);
                            p = programService.saveOrUpdate(p);
                            rs.setBase_num(base.toLowerCase());
                            rs = revisionSearchService.saveOrUpdate(rs);
                        }
                    }
                    ret.put("data", "Saved to db");
                    return ResponseEntity.ok(ret);
                }
                ret.put("data", "No saved data found");
                ret.put("code", HttpStatus.BAD_REQUEST);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(1200000, callable);
    }

    @PreAuthorize("hasAnyRole('PM', 'IPM', 'CPM', 'IPPM','SWPM', 'ADMIN')")
    @RequestMapping(value = {"/saveOrder"}, method = {RequestMethod.POST})
    public Callable<ResponseEntity> saveRevOrder(HttpServletRequest req, @RequestBody final HashMap map) {
        return new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                Map ret = new HashMap();
                if (!map.containsKey("data")) {
                    ret.put("data", "Empty Data");
                    ret.put("code", HttpStatus.EXPECTATION_FAILED);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(ret);
                }
                List list = (ArrayList) map.get("data");
                if (list != null && !list.isEmpty()) {
                    for (Object obj : list) {
                        HashMap hm = (HashMap) obj;
                        if (hm.containsKey("rid") && hm.containsKey("order")) {
                            Integer rid = Integer.parseInt(hm.get("rid").toString());
                            Integer orderNum = Integer.parseInt(hm.get("order").toString());
                            if (rid < 1)
                                throw new IDNotFoundException(rid, "revision");
                            Revision rev = revisionService.findById(rid);
                            if (rev == null)
                                throw new CustomGenericException("Revision Not found in database");
                            rev.setOrderNum(orderNum);
                            rev = revisionService.saveOrUpdate(rev);
                            RevisionSearch rs = revisionSearchService.findById(Integer.toString(rid));
                            if (rs != null) {
                                rs.setRev_order_num(orderNum);
                                rs.setProgram_order_num(orderNum);
                                revisionSearchService.saveOrUpdate(rs);
                            }
                        }
                    }
                }
                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
    }

    /***********************************************************************
     * SOFTWARE PROJECT
     * *********************************************************************/
    @RequestMapping(value = {"/getSWHeadlineList"}, method = {RequestMethod.GET})
    public Callable<List> getSWHeadlineList(HttpServletRequest req,
                                            @RequestParam(value = "pid", defaultValue = "0") final int pid,
                                            @RequestParam(value = "reload", defaultValue = "0") final int reload) {
        return new Callable<List>() {
            public List call() {
                if (pid < 1)
                    throw new IDNotFoundException(pid, "program");
                Program program = programService.findById(pid);
                if (program == null)
                    throw new CustomGenericException("Program Not found in database");
                if (!program.getType().equals(ProjectConstant.EnumProgramType.SOFTWARE))
                    throw new CustomGenericException("This Program is not in Software Category");
                List ret = indicatorService.getSWHeadlineList(pid);
                if (ret != null && !ret.isEmpty()) {
                    return ret;
                }
                return null;
            }
        };
    }

    @RequestMapping(value = {"/saveSW"}, method = {RequestMethod.POST})
    public WebAsyncTask<ResponseEntity> saveSW(HttpServletRequest req, @RequestBody final HashMap hm) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            public ResponseEntity call() {
                boolean isNew = false;
                HashMap ret = new HashMap();
                RevisionSearch rs = null;
                int rid = 0;
                Integer pid = Integer.parseInt(hm.get("pid").toString());
                String t = "software";
                String s = "software";

                if (hm.containsKey("id")) {
                    rid = Integer.parseInt(hm.get("id").toString());
                }
                if (rid > 0)
                    rs = revisionSearchService.findById(Integer.toString(rid));

                ProjectConstant.EnumIndicatorStatus color = ProjectConstant.EnumIndicatorStatus.BLACK;
                if (hm.containsKey("color")) {
                    color = ProjectConstant.EnumIndicatorStatus.valueOf(hm.get("color").toString().toUpperCase());
                }
                final String value = hm.get("headline").toString().replaceAll("\\[\\d{2}\\/\\d{2}\\/\\d{2,4}\\]", "");

                final DateTime currentdt = new DateTime();
                boolean includeReport = (Boolean) hm.get("includeReport");
                ProjectConstant.EnumProgramStatus status = ProjectConstant.EnumProgramStatus.ACTIVE;

                String rname = hm.get("rname").toString();

                if (!includeReport) {
                    status = ProjectConstant.EnumProgramStatus.NON_ACTIVE;
                    color = ProjectConstant.EnumIndicatorStatus.GREY;
                } else {
                    status = ProjectConstant.EnumProgramStatus.ACTIVE;
                    if (color.equals(ProjectConstant.EnumIndicatorStatus.GREY)) {
                        color = ProjectConstant.EnumIndicatorStatus.BLACK;
                    }
                }
                Revision rev = null;
                StringBuilder relatedsb = new StringBuilder();
                if (hm.containsKey("ga") && !hm.get("ga").toString().trim().isEmpty()) {
                    relatedsb.append("ga:" + hm.get("ga").toString().trim());
                }
                if (hm.containsKey("ea") && !hm.get("ea").toString().trim().isEmpty()) {
                    if (relatedsb.length() > 0) {
                        relatedsb.append("<br>");
                    }
                    relatedsb.append("ea:" + hm.get("ea").toString().trim());
                }
                if (hm.containsKey("isNew")) {
                    isNew = (Boolean) hm.get("isNew");
                    if (isNew) {
                        Program p = null;
                        if (pid < 1) {
                            ret.put("data", "Program Not found in database");
                            ret.put("code", HttpStatus.BAD_REQUEST);
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                        } else {
                            p = programService.findById(pid);
                        }
                        if (p == null) {
                            ret.put("data", "Revision Not found in database");
                            ret.put("code", HttpStatus.BAD_REQUEST);
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ret);
                        }
                        rev = new Revision();
                        rev.setIsActive(ProjectConstant.EnumProgramStatus.ACTIVE);
                        rev.setIsRevisionIncludeInReport(true);
                        rev.setIpRelated(relatedsb.toString());
                        rev.setName(rname);
                        rev.setOrderNum(0);
                        rev.setProgram(p);
                        rev.setIsProtected(false);
                        rev = revisionService.saveOrUpdate(rev);
                        rid = rev.getId();

                        rs.setBase_num("");
                        rs.setId(Integer.toString(rev.getId()));
                        rs.setInclude_in_report(true);
                        rs.setIp_related(relatedsb.toString().toLowerCase());
                        rs.setIs_active(true);
                        rs.setIs_protected(false);
                        rs.setLast_updated_outlook_date(rev.getLastUpdatedDate());
                        rs.setOutlook("");
                        rs.setProgram_id(pid);
                        rs.setProgram_name(p.getName().toLowerCase().trim());
                        rs.setProgram_order_num(p.getOrderNum());
                        rs.setRev_name(rname.toLowerCase().trim());
                        rs.setRev_order_num(rev.getOrderNum());
                        rs.setSegment("software");
                        rs.setType("software");
                        rs = revisionSearchService.saveOrUpdate(rs);
                    }
                } else {
                    if (rs == null)
                        return null;
                    rev = revisionService.findById(rid);
                    rev.setIsRevisionIncludeInReport(includeReport);
                    rev.setName(rname);
                    rev.setIsActive(status);
                    rev.setIpRelated(relatedsb.toString());
                    rev = revisionService.saveOrUpdate(rev);

                    rs.setInclude_in_report(includeReport);
                    rs.setRev_name(rname.toLowerCase().trim());
                    rs.setIp_related(relatedsb.toString().toLowerCase());
                    rs.setIs_active(includeReport);
                    rs.setLast_updated_outlook_date(rev.getLastUpdatedDate());
                    rs = revisionSearchService.saveOrUpdate(rs);
                }
                HeadlineSearch old_hls = headlineSearchService.findByRevision(rev.getId(), null);
                if (old_hls == null || !value.trim().equalsIgnoreCase(old_hls.getHeadline().trim()) ||
                        !color.toString().toLowerCase().equalsIgnoreCase(old_hls.getSchedule_flag())) {
                    Headline headline = new Headline();
                    headline.setHeadline(value);
                    headline.setRevision(rev);
                    headline.setStage(ProjectConstant.EnumHeadlineStage.SOFTWARE);
                    headline.setBudget_flag(color);
                    headline.setPrediction_flag(color);
                    headline.setResource_flag(color);
                    headline.setSchedule_flag(color);
                    headline.setIsActive(rev.getIsActive());
                    headline = headlineService.saveOrUpdate(headline);

                    HeadlineSearch hls = new HeadlineSearch();
                    hls.setStage("software");
                    hls.setBudget_flag(color.toString().toLowerCase());
                    hls.setPrediction_flag(color.toString().toLowerCase());
                    hls.setResource_flag(color.toString().toLowerCase());
                    hls.setSchedule_flag(color.toString().toLowerCase());
                    hls.setStatus(status.toString().toLowerCase());
                    hls.setRevision_name(rev.getName());
                    hls.setRevision_id(rid);
                    hls.setLast_updated_date(headline.getLastUpdatedDate());
                    hls.setHeadline(value);
                    hls.setId(Integer.toString(headline.getId()));
                    headlineSearchService.saveOrUpdate(hls);

                }
                ret.put("code", HttpStatus.OK);
                ret.put("data", "Saved to db");
                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(120000, callable);
    }

    private boolean isValidStage(ProjectConstant.EnumHeadlineStage oldStage, ProjectConstant.EnumHeadlineStage newStage) {
        boolean ret = false;
        if (newStage.equals(ProjectConstant.EnumHeadlineStage.CANCELLED)
                || newStage.equals(ProjectConstant.EnumHeadlineStage.CUSTOMER)
                || newStage.equals(ProjectConstant.EnumHeadlineStage.SOFTWARE)
                || newStage.equals(ProjectConstant.EnumHeadlineStage.SERDES))
            return true;
        if (!oldStage.equals(newStage)) {
            if (oldStage.equals(ProjectConstant.EnumHeadlineStage.PRA)) {
                ret = false;
            } else if (oldStage.equals(ProjectConstant.EnumHeadlineStage.PRE_PRODUCTION)
                    && newStage.equals(ProjectConstant.EnumHeadlineStage.PRA)) {
                ret = true;
            } else if (oldStage.equals(ProjectConstant.EnumHeadlineStage.VER_QUAL)
                    && (newStage.equals(ProjectConstant.EnumHeadlineStage.PRA)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.PRE_PRODUCTION))) {
                ret = true;
            } else if (oldStage.equals(ProjectConstant.EnumHeadlineStage.FABRICATION)
                    && (newStage.equals(ProjectConstant.EnumHeadlineStage.PRA)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.PRE_PRODUCTION)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.VER_QUAL))) {
                ret = true;
            } else if (oldStage.equals(ProjectConstant.EnumHeadlineStage.DESIGN)
                    && (newStage.equals(ProjectConstant.EnumHeadlineStage.PRA)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.PRE_PRODUCTION)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.VER_QUAL)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.FABRICATION))) {
                ret = true;
            } else if (oldStage.equals(ProjectConstant.EnumHeadlineStage.PLANNING)
                    && (newStage.equals(ProjectConstant.EnumHeadlineStage.PRA)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.PRE_PRODUCTION)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.VER_QUAL)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.FABRICATION)
                    || newStage.equals(ProjectConstant.EnumHeadlineStage.DESIGN))) {
                ret = true;
            }
        }

        return ret;
    }
}
