package com.broadcom.wbi.model.mssql;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class ResourceReaderId implements Serializable {

    @Column(name = "PROFIT_CENTER_CHARGED_FROM")
    private String charged_from;
    @Column(name = "PROFIT_CENTER_CHARGED_TO")
    private String charged_to;
    @Column(name = "PROJECT")
    private String project;
    @Column(name = "SKILL")
    private String skill;
    @Column(name = "FTE_HEADS")
    private Double count;

    @Override
    public String toString() {
        return charged_from + " " + charged_to + " " + project + " " + skill + " " + count;
    }
}
