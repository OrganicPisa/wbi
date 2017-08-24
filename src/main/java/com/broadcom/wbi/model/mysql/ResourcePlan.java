package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "resource_plan", indexes = {
        @Index(columnList = "resource_plan_id", name = "resource_plan_id_hidx"),
        @Index(columnList = "resource_plan_skill", name = "resource_plan_skill_hidx"),
        @Index(columnList = "resource_plan_month", name = "resource_planmonth_hidx"),
        @Index(columnList = "resource_plan_type", name = "resource_plan_type_hidx"),
        @Index(columnList = "program_id", name = "program_id_hidx")
})
public class ResourcePlan extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "resource_plan_id")
    @GenericGenerator(name = "rplan", strategy = "increment")
    @GeneratedValue(generator = "rplan")
    private Integer id;

    @NotEmpty
    @Column(name = "resource_plan_skill", nullable = false)
    private String plan_skill;

    @NonNull
    @Column(name = "resource_plan_month", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date month;

    @NonNull
    @Column(name = "resource_plan_count", nullable = false)
    private Double count = 0.0;

    @NonNull
    @Column(name = "resource_plan_include_contractor", columnDefinition = "tinyint default 1", nullable = false)
    private Boolean include_contractor = true;

    @NotEmpty
    @Column(name = "resource_plan_type", nullable = false)
    private String type;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "program_id")
    private Program program;

}
