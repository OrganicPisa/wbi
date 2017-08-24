package com.broadcom.wbi.model.mysql;

import com.broadcom.wbi.util.ProjectConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotEmpty;

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
@Table(name = "program")
public class Program extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "program_id")
    @GenericGenerator(name = "program", strategy = "increment")
    @GeneratedValue(generator = "program")
    private Integer id;

    @NotEmpty
    @Column(name = "base_num", nullable = false)
    private String baseNum = "NA";

    @NotEmpty
    @Column(name = "name", nullable = false)
    private String name;

    @NotEmpty
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectConstant.EnumProgramType type = ProjectConstant.EnumProgramType.CHIP;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    @JoinTable(name = "program_segment_composite",
            joinColumns = {@JoinColumn(name = "program_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "segment_id", nullable = false, updatable = false)}
    )
    @Cascade({
            org.hibernate.annotations.CascadeType.LOCK,
            org.hibernate.annotations.CascadeType.REFRESH,
            org.hibernate.annotations.CascadeType.REPLICATE
    })
    private Set<Segment> segments = new HashSet<Segment>();

    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;

    @NonNull
    @Column(name = "is_include_in_report", columnDefinition = "tinyint default 1", nullable = false)
    private Boolean isProgramIncludeInReport = true;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Sku> skus = new HashSet<Sku>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Revision> revisions = new HashSet<Revision>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResourcePlan> resourcePlanning = new HashSet<ResourcePlan>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SkillMapping> skillMapping = new HashSet<SkillMapping>();

}
