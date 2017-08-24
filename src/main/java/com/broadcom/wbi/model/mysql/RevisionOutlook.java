package com.broadcom.wbi.model.mysql;

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
@Table(name = "revision_outlook")
public class RevisionOutlook extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "revision_outlook_id")
    @GenericGenerator(name = "revol", strategy = "increment")
    @GeneratedValue(generator = "revol")
    private Integer id;

    @Lob
    @Column(name = "revision_outlook_content", length = 4000)
    private String content;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;

}
