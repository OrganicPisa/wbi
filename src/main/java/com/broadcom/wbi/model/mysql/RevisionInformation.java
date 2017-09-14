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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "revision_information")
public class RevisionInformation extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "revision_information_id")
    @GenericGenerator(name = "info", strategy = "increment")
    @GeneratedValue(generator = "info")
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "value", nullable = false)
    private String value;

    @Min(value = 0L, message = "Order should be positive number")
    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @NotBlank
    @Column(name = "phase", nullable = false)
    private String phase = "current";

    @NotNull
    @Column(name = "on_dashboard", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean onDashboard = false;

    @NotNull
    @Column(name = "is_user_editable", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean isUserEditable = false;

    @NotNull
    @Column(name = "is_restricted", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean isRestrictedView = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;

}
