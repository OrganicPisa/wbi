package com.broadcom.wbi.service.report;

import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface ReportService {

    DateTimeFormatter dfmt = org.joda.time.format.DateTimeFormat.forPattern("MM/dd/yy");

    Map generateMilestoneReport(ProjectConstant.EnumProgramType type, String status);

    Map generateHeadlineReport(ProjectConstant.EnumProgramType type, String status);

    Map generatePRAReport();

    Map generateHTOLReport();

    Map generateInformationReport(ProjectConstant.EnumProgramType type);

    @Async
    void generateCustomerHeadlineWeekendEmail(String includeCustomer, String excludeCustomer, String email);

    @Async
    void generateInternalHeadlineWeekendEmail(String email);
}
