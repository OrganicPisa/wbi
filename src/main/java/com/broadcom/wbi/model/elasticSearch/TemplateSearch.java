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
@Document(indexName = "templateidx", type = "templatesearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class TemplateSearch implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date created_date;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date last_updated_date;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String name;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String nameInReport;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer orderNum;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String type;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String category;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String group;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean onDashboard;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean availableCA;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean availablePC;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean availableECR;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean availableTO;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean availableCurrent;

    @Field(type = FieldType.Boolean, index = FieldIndex.not_analyzed, store = true)
    private Boolean isRestrictedView;

    @Override
    public String toString() {
        return name + " " + nameInReport + " " + orderNum + " " + type + " " + category + " " + group;
    }

}