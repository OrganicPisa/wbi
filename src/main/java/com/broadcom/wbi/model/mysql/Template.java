package com.broadcom.wbi.model.mysql;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "template")
public class Template extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "template_id")
    @GenericGenerator(name = "indicatortasktemplate", strategy = "increment")
    @GeneratedValue(generator = "indicatortasktemplate")
    private Integer id;

    @NotEmpty
    @Column(name = "template_name", nullable = false)
    private String name;

    @Column(name = "template_name_in_report", nullable = false)
    private String nameInReport;

    @Column(name = "template_type")
    private String type;

    @NonNull
    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;

    @Column(name = "template_category")
    private String category;

    @Column(name = "template_group")
    private String group;

    @NonNull
    @Column(name = "on_dashboard", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean onDashboard = false;

    @NonNull
    @Column(name = "available_ca", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean availableCA = false;

    @NonNull
    @Column(name = "available_pc", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean availablePC = false;

    @NonNull
    @Column(name = "available_ecr", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean availableECR = false;

    @NonNull
    @Column(name = "available_to", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean availableTO = false;

    @NonNull
    @Column(name = "available_current", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean availableCurrent = false;

    @NonNull
    @Column(name = "is_restricted", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean isRestrictedView = false;
}
