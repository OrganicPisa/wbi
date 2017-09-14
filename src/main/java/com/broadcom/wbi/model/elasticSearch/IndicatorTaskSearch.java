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
@Document(indexName = "itaskidx", type = "itasksearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class IndicatorTaskSearch implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer task_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String task_name;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String task_name_in_report;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer order_num;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String note;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String status;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer revision_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String revision_name;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer igroup_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String igroup_name;

    @Field(type = FieldType.Date, index = FieldIndex.analyzed, store = true)
    private Date last_updated_date;
}