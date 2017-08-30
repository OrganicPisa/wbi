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

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "indicator_task_history")
public class ITaskHistory extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "indicator_task_history_id")
    @GenericGenerator(name = "indicatortaskhistory", strategy = "increment")
    @GeneratedValue(generator = "indicatortaskhistory")
    private Integer id;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectConstant.EnumIndicatorStatus status = ProjectConstant.EnumIndicatorStatus.BLACK;

    @Lob
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "indicator_task_id")
    private ITask iTask;

    @Override
    public String toString() {
        return iTask.getName() + " : ( " + id + " ) " + note;
    }

}
