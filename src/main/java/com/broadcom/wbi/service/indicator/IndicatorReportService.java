package com.broadcom.wbi.service.indicator;

import com.broadcom.wbi.util.ProjectConstant;

import java.util.Map;

public interface IndicatorReportService {

    Map generateMilestoneReport(ProjectConstant.EnumProgramType type, String status);

    Map generateHeadlineReport(ProjectConstant.EnumProgramType type, String status);
}
