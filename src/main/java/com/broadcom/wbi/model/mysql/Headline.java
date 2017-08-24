package com.broadcom.wbi.model.mysql;

import com.broadcom.wbi.util.ProjectConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "headline")
public class Headline extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "headline_id")
    @GenericGenerator(name = "headline", strategy = "increment")
    @GeneratedValue(generator = "headline")
    private Integer id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_flag", nullable = false)
    private ProjectConstant.EnumIndicatorStatus schedule_flag = ProjectConstant.EnumIndicatorStatus.BLACK;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "budget_flag", nullable = false)
    private ProjectConstant.EnumIndicatorStatus budget_flag = ProjectConstant.EnumIndicatorStatus.BLACK;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_flag", nullable = false)
    private ProjectConstant.EnumIndicatorStatus resource_flag = ProjectConstant.EnumIndicatorStatus.BLACK;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_flag", nullable = false)
    private ProjectConstant.EnumIndicatorStatus prediction_flag = ProjectConstant.EnumIndicatorStatus.BLACK;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private ProjectConstant.EnumProgramStatus isActive = ProjectConstant.EnumProgramStatus.ACTIVE;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String headline;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private ProjectConstant.EnumHeadlineStage stage;

    @JsonIgnore
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;

}
