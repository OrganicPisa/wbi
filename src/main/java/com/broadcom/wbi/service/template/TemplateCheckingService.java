package com.broadcom.wbi.service.template;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;

public interface TemplateCheckingService {
    void checkInformationTemplate(RevisionSearch rs);

    void checkInformationTemplate(RevisionSearch rs, String type, String title, String value, int orderNum);

    void checkIndicatorTemplate(RevisionSearch rs);
}
