package com.broadcom.wbi.controller;


import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.elasticSearch.SkuSearchService;
import com.broadcom.wbi.service.google.GoogleService;
import com.broadcom.wbi.service.template.TemplateCheckingService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private GoogleService googleService;

    @Autowired
    private RevisionSearchService revisionSearchService;

    @Autowired
    private SkuSearchService skuSearchService;

    @Autowired
    private TemplateCheckingService templateCheckingService;

    @RequestMapping(value = "/getGoogleSheet", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<ResponseEntity> addNewProgram(HttpServletRequest req, HttpServletResponse res,
                                                      @RequestParam(value = "sheetID", defaultValue = "") final String sheetID,
                                                      @RequestParam(value = "sheetName", defaultValue = "Customer Programs") final String sheetName) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            @Override
            public ResponseEntity call() throws Exception {
                if (sheetID.isEmpty())
                    return ResponseEntity.ok(HttpStatus.BAD_REQUEST);

                Credential googleCrendential = googleService.authorize();
                System.out.println(googleCrendential);
                Sheets service = googleService.getSheetService(googleCrendential);

                ValueRange reponse = service.spreadsheets().values().get(sheetID, sheetName).execute();
                List<List<Object>> usedRange = reponse.getValues();

                int numCols = usedRange != null ? usedRange.get(0).size() : 0;
                int numRows = usedRange != null ? usedRange.size() : 0;

                //generate title map
                if (numRows < 1) return ResponseEntity.ok(HttpEntity.EMPTY);
                List titleRow = usedRange.get(0);

                for (int numRow = 1; numRow < numRows; numRow++) {
                    List<Object> row = usedRange.get(numRow);
                    if (row != null && !row.isEmpty()) {
                        //if manager is Delany, CPM is not N/A, and Phase is not empty
                        if (row.get(0).toString().toLowerCase().indexOf("delany") == 0 &&
                                !row.get(6).toString().equalsIgnoreCase("n/a") &&
                                !row.get(23).toString().trim().isEmpty()) {
                            List<RevisionSearch> revisionSearchList = revisionSearchService.findByProgram(row.get(1).toString().toLowerCase().trim(), row.get(7).toString().toLowerCase().trim());
                            if (revisionSearchList != null) {
                                if (!revisionSearchList.isEmpty()) {
                                    for (int i = 8; i < 20; i++) {
//                                        templateCheckingService.checkInformationTemplate(revisionSearchList.get(0), "information", titleRow.get(i).toString().toLowerCase().trim(), row.get(i).toString(), i-5);
                                    }
                                } else {
                                    System.out.println(row);
                                }
                            }
                        }

                    }
                }

                return ResponseEntity.ok(null);
            }
        };
        return new WebAsyncTask<ResponseEntity>(1800000, callable);
    }


}


