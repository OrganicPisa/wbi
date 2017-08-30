package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "revision_ip")
public class RevisionIP extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "revision_ip_id")
    @GenericGenerator(name = "revip", strategy = "increment")
    @GeneratedValue(generator = "revip")
    private Integer id;

    @Min(value = 0L, message = "Value should be greater than 0")
    @Column(name = "num_of_instance", columnDefinition = "int default 0", nullable = false)
    private Integer instanceNum = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "ip_revision_id")
    private Revision iprevision;

}
