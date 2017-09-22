package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "indicator_group")
public class IGroup extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "indicator_group_id")
    @GenericGenerator(name = "indicatorgroup", strategy = "increment")
    @GeneratedValue(generator = "indicatorgroup")
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Min(value = 0L, message = "Order value should be positive")
    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "iGroup", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<IGroupHistory> group_history = new HashSet<IGroupHistory>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "iGroup", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<ITask> tasks = new HashSet<ITask>();

}
