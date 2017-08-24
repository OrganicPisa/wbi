package com.broadcom.wbi.model.mysql;

import com.broadcom.wbi.util.ProjectConstant;
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
@Table(name = "link")
public class Link extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "link_id")
    @GenericGenerator(name = "link", strategy = "increment")
    @GeneratedValue(generator = "link")
    private Integer id;

    @NotEmpty
    @Column(name = "display_name", nullable = false)
    private String display_name = "link";

    @NotEmpty
    @Column(name = "type", nullable = false)
    private String type = "type";

    @NotEmpty
    @Column(name = "url", nullable = false)
    private String url = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProjectConstant.EnumLinkCategory category = ProjectConstant.EnumLinkCategory.LINK;

    @NonNull
    @Column(name = "order_num", columnDefinition = "int default 0", nullable = false)
    private Integer orderNum = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "revision_id")
    private Revision revision;
}
