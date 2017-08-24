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
@XmlRootElement
@Cacheable
@Table(name = "revision_information")
public class RevisionInformation extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "revision_information_id")
    @GenericGenerator(name = "info", strategy = "increment")
    @GeneratedValue(generator = "info")
    private Integer id;

    @NotEmpty
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @NotEmpty
    @Column(name = "phase", nullable = false)
    private String phase = "current";

    @NonNull
    @Column(name = "on_dashboard", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean onDashboard = false;

    @NonNull
    @Column(name = "is_user_editable", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean isUserEditable = false;

    @NonNull
    @Column(name = "is_restricted", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean isRestrictedView = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;

}
