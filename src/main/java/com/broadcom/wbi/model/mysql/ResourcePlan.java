package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "resource_plan")
public class ResourcePlan extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "resource_plan_id")
    @GenericGenerator(name = "rplan", strategy = "increment")
    @GeneratedValue(generator = "rplan")
    private Integer id;

    @NotEmpty
    @Column(name = "resource_plan_skill", nullable = false)
    private String plan_skill;

    @NotNull
    @Column(name = "resource_plan_month", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date month;

    @Min(value = 0L, message = "The value must be positive")
    @Column(name = "resource_plan_count", nullable = false)
    private Double count = 0.0;

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
