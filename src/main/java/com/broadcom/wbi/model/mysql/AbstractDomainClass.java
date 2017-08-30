package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;


@Getter
@Setter
@XmlRootElement
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractDomainClass {

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "last_update_by", nullable = false)
    protected String lastModifiedBy;

    @CreatedBy
    @JsonIgnore
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "last_update_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
}
