package com.broadcom.wbi.model.mysql;

import com.broadcom.wbi.util.ProjectConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "indicator_date")
public class IDate extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "indicator_date_id")
    @GenericGenerator(name = "indicatordate", strategy = "increment")
    @GeneratedValue(generator = "indicatordate")
    private Integer id;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(name = "end_date_type", nullable = false)
    private ProjectConstant.EnumIndicatorEndingDateType etype = ProjectConstant.EnumIndicatorEndingDateType.END;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(name = "track_date_type", nullable = false)
    private ProjectConstant.EnumIndicatorTrackingDateType ttype = ProjectConstant.EnumIndicatorTrackingDateType.CURRENT;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "indicator_task_id")
    private ITask iTask;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "iDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IDateHistory> date_history = new HashSet<IDateHistory>();

}
