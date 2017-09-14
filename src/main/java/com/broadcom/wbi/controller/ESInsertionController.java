package com.broadcom.wbi.controller;

import com.broadcom.wbi.service.indexing.IndexSearchService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/admin/index")
public class ESInsertionController {

    private final IndexSearchService indexSearchService;

    @Autowired
    public ESInsertionController(IndexSearchService indexSearchService) {
        this.indexSearchService = indexSearchService;
    }

    @RequestMapping(value = {"/sku"}, method = {RequestMethod.GET})
    public int indexSku(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllSku(dt);
        return 0;
    }

    @RequestMapping(value = {"/revision"}, method = {RequestMethod.GET})
    public int indexRevision(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllRevision(dt);
        return 0;
    }

    @RequestMapping(value = {"/resource/plan"}, method = {RequestMethod.GET})
    @ResponseBody
    public int indexPlanResource(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllResourcePlan(dt);
        return 0;
    }

    @RequestMapping(value = {"/information/{infoType}"}, method = {RequestMethod.GET})
    public int indexInformation(@PathVariable String infoType,
                                @RequestParam(value = "reload", defaultValue = "0") final int reload,
                                @RequestParam(value = "type", defaultValue = "type") final String type,
                                @RequestParam(value = "rid", defaultValue = "0") final Integer rid) {
        if (infoType.equalsIgnoreCase("all"))
            indexSearchService.indexAllRevisionInformation(reload);
        else if (infoType.equalsIgnoreCase("revision") && rid > 0)
            indexSearchService.indexProgramInformationByRevision(rid);
        return 0;
    }

    @RequestMapping(value = {"/contact"}, method = {RequestMethod.GET})
    public int indexContact(@RequestParam(value = "reload", defaultValue = "0") final int reload) {
        indexSearchService.indexAllRevisionContact(reload);
        return 0;
    }

    @RequestMapping(value = {"/igroup"}, method = {RequestMethod.GET})
    public int indexIGroup(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllIndicatorGroup(dt);
        return 0;
    }

    @RequestMapping(value = {"/itask"}, method = {RequestMethod.GET})
    public int indexItask(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllIndicatorTask(dt);
        return 0;
    }

    @RequestMapping(value = {"/idate"}, method = {RequestMethod.GET})
    public int indexIdate(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllIndicatorDate(dt);
        return 0;
    }

    @RequestMapping(value = {"/milestone"}, method = {RequestMethod.GET})
    public int indexIndicator(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllIndicatorGroup(dt);
        indexSearchService.indexAllIndicatorTask(dt);
        indexSearchService.indexAllIndicatorDate(dt);
        return 0;
    }

    @RequestMapping(value = {"/headline"}, method = {RequestMethod.GET})
    public int indexHeadline(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllHeadline(dt);
        return 0;
    }

    @RequestMapping(value = {"/template"}, method = {RequestMethod.GET})
    public int indexTemplate(@RequestParam(value = "ts", defaultValue = "01/01/1990") @DateTimeFormat(pattern = "MM/dd/yyyy") final Date da) {
        DateTime dt = new DateTime(da);
        if (dt.getYear() == 1990)
            dt = null;
        indexSearchService.indexAllTemplate(dt);
        return 0;
    }
}
