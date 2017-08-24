package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
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
@Table(name = "segment")
public class Segment extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "segment_id")
    @GenericGenerator(name = "segment", strategy = "increment")
    @GeneratedValue(generator = "segment")
    private Integer id;

    @NotEmpty
    @Column(name = "name", nullable = false)
    private String name;

    @NotEmpty
    @Column(name = "segment_group", nullable = false)
    private String segmentGroup = "CHIP";

    @Lob
    @Column(name = "description", length = 1024)
    private String description;

    @JsonIgnore
    @ManyToMany(mappedBy = "segments")
    private Set<Program> programs = new HashSet<Program>();

    @JsonIgnore
    @NonNull
    @Column(name = "include_in_report", columnDefinition = "tinyint default 1", nullable = false)
    private Boolean isSegmentIncludeInReport = true;

    @NonNull
    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;
}
