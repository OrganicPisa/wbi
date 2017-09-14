package com.broadcom.wbi.service.information;

import com.broadcom.wbi.util.ProjectConstant;

import java.util.Map;

public interface InformationReportService {

    Map generateInformationReport(ProjectConstant.EnumProgramType type);
}
