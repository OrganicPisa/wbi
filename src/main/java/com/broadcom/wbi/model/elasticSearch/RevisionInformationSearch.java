package com.broadcom.wbi.model.elasticSearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Document(indexName = "revisioninformationidx", type = "revisioninformationsearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class RevisionInformationSearch implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date created_date;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date last_updated_date;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String name;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String value;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer orderNum;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean onDashboard;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String phase;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer revision;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean isUserEditable;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean isRestrictedView;

}