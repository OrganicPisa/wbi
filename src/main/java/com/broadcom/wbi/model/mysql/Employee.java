package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "employee")
public class Employee extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "employee_id", unique = true)
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @Column(nullable = false)
    private String profit_center;
    private String cost_center;
    private String segment;
    private String last_name;
    private String first_name;
    private String middle_name;
    private String full_name;

    @Email
    @Column(name = "email", length = 128, nullable = false)
    private String email;

    private String email_alias;


    @NotBlank
    @Column(name = "acc_nt", length = 128, nullable = false)
    private String accNt;
    private String acc_unix;
    private String office_num;
    private String phone_business;
    private String mobile;
    private String mobile2;
    private String title;
    private String supervisor;
    private String primary_loc_name;
    private String primary_loc_country;
    private String primary_loc_state;
    private String primary_loc_city;
    private String job_function_name;
    private String job_family_name;
    private String job_subfunction_name;

    private String design_center;

    private String org_level_1;
    private String org_level_2;
    private String org_level_3;
    private String org_level_4;
    private String org_level_5;

    private String org_level_6;
    private String org_level_7;
    private String org_level_8;
    private String org_level_9;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = false)
    private Date start_date;

    private String status;

    @Temporal(TemporalType.DATE)
    @Column(name = "term_date", nullable = true)
    private Date term_date;

    private String is_manager;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name = "last_updated_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date lastUpdatedDate;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeePermission> permissions = new HashSet<EmployeePermission>();

    @JsonIgnore
    //use for bookmark
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    @JoinTable(name = "employee_revision_composite",
            joinColumns = {@JoinColumn(name = "employee_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "revision_id", nullable = false, updatable = false)}
    )
    @Cascade({
            org.hibernate.annotations.CascadeType.LOCK,
            org.hibernate.annotations.CascadeType.REFRESH,
            org.hibernate.annotations.CascadeType.REPLICATE
    })
    private Set<Revision> revisions = new HashSet<Revision>();
}

