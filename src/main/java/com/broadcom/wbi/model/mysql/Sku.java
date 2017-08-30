package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Getter
@Setter
@Entity
@XmlRootElement
@Cacheable
@Table(name = "sku")
public class Sku extends AbstractDomainClass implements Serializable {
    @Id
    @Column(name = "sku_id")
    @GenericGenerator(name = "sku", strategy = "increment")
    @GeneratedValue(generator = "sku")
    private Integer id;

    @Column(name = "aka")
    private String aka;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "io_capacity")
    private String ioCapacity;

    @Column(name = "num_of_serdes")
    private String numOfSerdes;

    @Column(name = "port_config")
    private String portConfig;

    @Column(name = "itemp")
    private String itemp;

    @NotBlank
    @Column(name = "sku_num")
    private String skuNum = "NA";

    @Lob
    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "date_available")
    private String dateAvailable;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "program_id")
    private Program program;

}
