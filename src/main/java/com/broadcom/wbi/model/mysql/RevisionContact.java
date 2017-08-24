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
@Table(name = "revision_contact")
public class RevisionContact extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "revision_contact_id")
    @GenericGenerator(name = "contact", strategy = "increment")
    @GeneratedValue(generator = "contact")
    private Integer id;

    @NotEmpty
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    @NonNull
    @Column(name = "on_dashboard", columnDefinition = "tinyint default 0", nullable = false)
    private Boolean onDashboard = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;

}
