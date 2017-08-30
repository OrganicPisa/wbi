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
@Table(name = "indicator_task")
public class ITask extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "indicator_task_id")
    @GenericGenerator(name = "indicatortask", strategy = "increment")
    @GeneratedValue(generator = "indicatortask")
    private Integer id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @Column(name = "name_in_report")
    private String nameInReport;

    @Min(value = 0L, message = "Order value should be positive")
    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "indicator_group_id")
    private IGroup iGroup;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "iTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ITaskHistory> task_history = new HashSet<ITaskHistory>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "iTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IDate> iDate = new HashSet<IDate>();

}
