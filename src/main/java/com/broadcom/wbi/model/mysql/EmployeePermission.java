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

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "employee_permission")
public class EmployeePermission extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "employee_permission_id")
    @GenericGenerator(name = "employeepermission", strategy = "increment")
    @GeneratedValue(generator = "employeepermission")
    private Integer id;


    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private ProjectConstant.EnumPermissionType permission;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Employee employee;

}
