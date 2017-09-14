package com.broadcom.wbi.model.mysql;

import com.broadcom.wbi.util.ProjectConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "indicator_date_history")
public class IDateHistory extends AbstractDomainClass implements Serializable {
    @Id
    @GenericGenerator(name = "indicatordateh", strategy = "increment")
    @GeneratedValue(generator = "indicatordateh")
    private Integer id;

    @Column(name = "value", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date value;

    @Lob
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "indicator_date_id")
    private IDate iDate;

}
