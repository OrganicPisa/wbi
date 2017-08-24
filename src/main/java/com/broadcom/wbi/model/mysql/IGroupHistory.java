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
@Table(name = "indicator_group_history")
public class IGroupHistory extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "indicator_group_history_id")
    @GenericGenerator(name = "indicatorgrouph", strategy = "increment")
    @GeneratedValue(generator = "indicatorgrouph")
    private Integer id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;

    @Lob
    @Column(name = "remark", columnDefinition = "MEDIUMTEXT")
    private String remark;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "indicator_group_id")
    private IGroup iGroup;

}
