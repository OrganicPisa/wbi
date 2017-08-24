package com.broadcom.wbi.model.mysql;

import com.broadcom.wbi.util.ProjectConstant;
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
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "revision")
public class Revision extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "revision_id")
    @GenericGenerator(name = "rev", strategy = "increment")
    @GeneratedValue(generator = "rev")
    private Integer id;

    @NotEmpty
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ip_related")
    private String ipRelated;

    @NonNull
    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;

    @NonNull
    @Column(name = "is_active", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectConstant.EnumProgramStatus isActive = ProjectConstant.EnumProgramStatus.ACTIVE;

    @NonNull
    @Column(name = "is_include_in_report", columnDefinition = "tinyint default 1", nullable = false)
    private Boolean isRevisionIncludeInReport = true;

    @NonNull
    @Column(name = "is_protected", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean isProtected = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "program_id")
    private Program program;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Headline> headlines = new HashSet<Headline>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IGroup> groups = new HashSet<IGroup>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RevisionOutlook> outlooks = new HashSet<RevisionOutlook>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RevisionInformation> informations = new HashSet<RevisionInformation>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RevisionContact> contacts = new HashSet<RevisionContact>();

    //use for bookmark
    @JsonIgnore
    @ManyToMany(mappedBy = "revisions")
    private Set<Employee> employees = new HashSet<Employee>();

}
