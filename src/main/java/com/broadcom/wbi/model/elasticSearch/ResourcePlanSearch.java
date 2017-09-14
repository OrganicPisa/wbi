package com.broadcom.wbi.model.elasticSearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Document(indexName = "resourceplanidx", type = "resourceplansearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class ResourcePlanSearch implements Serializable {
    @Id
    String id;

    @Field(type = FieldType.Integer, store = true)
    Integer program;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String skill;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String plan_type;

    @Field(type = FieldType.Date, index = FieldIndex.analyzed, store = true)
    Date month;

    @Field(type = FieldType.Double, index = FieldIndex.analyzed, store = true)
    Double count;

    @Field(type = FieldType.Boolean, index = FieldIndex.analyzed, store = true)
    Boolean include_contractor;

}