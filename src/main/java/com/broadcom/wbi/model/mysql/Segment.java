package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "segment")
public class Segment extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "segment_id")
    @GenericGenerator(name = "segment", strategy = "increment")
    @GeneratedValue(generator = "segment")
    private Integer id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "segment_group", nullable = false)
    private String segmentGroup = "CHIP";

    @Lob
    @Column(name = "description", length = 1024)
    private String description;

    @JsonIgnore
    @ManyToMany(mappedBy = "segments")
    private Set<Program> programs = new HashSet<Program>();

    @JsonIgnore
    @NotNull
    @Column(name = "include_in_report", columnDefinition = "tinyint default 1", nullable = false)
    private Boolean isSegmentIncludeInReport = true;

    @Min(value = 0L, message = "Order should be positive number")
    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;
}
