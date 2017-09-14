package com.broadcom.wbi.service.indicator;

import com.broadcom.wbi.util.ProjectConstant;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

public interface IndicatorReportService {


    DateTimeFormatter dfmt = org.joda.time.format.DateTimeFormat.forPattern("MM/dd/yy");

    Map generateMilestoneReport(ProjectConstant.EnumProgramType type, String status);

    Map generateHeadlineReport(ProjectConstant.EnumProgramType type, String status);

    Map generatePRAReport();

    Map generateHTOLReport();
}
