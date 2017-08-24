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

@Getter
@Setter
@Entity
@Cacheable
@XmlRootElement
@Table(name = "skill_mapping")
public class SkillMapping extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "skill_mapping_id")
    @GenericGenerator(name = "skillmap", strategy = "increment")
    @GeneratedValue(generator = "skillmap")
    private Integer id;

    @NotEmpty
    @Column(name = "skill_mapping_plan_skill", nullable = false)
    private String plan_skill;

    @NotEmpty
    @Column(name = "skill_mapping_actual_skill", nullable = false)
    private String actual_skill;

    @Column(name = "skill_mapping_exclude", nullable = false)
    private String exclude;

    @NonNull
    @Column(name = "skill_mapping_order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "program_id")
    private Program program;

}
