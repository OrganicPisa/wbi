package com.broadcom.wbi.model.mysql;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "resource_program_classification")
public class ResourceProgramClassification extends AbstractDomainClass implements Serializable {

    @Id
    @Column(name = "resource_program_classification_id")
    @GenericGenerator(name = "resourceProgramClassification", strategy = "increment")
    @GeneratedValue(generator = "resourceProgramClassification")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Lob
    @Column(name = "content", length = 4000)
    private String programList;

    @NonNull
    @Column(name = "status", nullable = false, columnDefinition = "INT(11)")
    private Boolean status = true;

}
