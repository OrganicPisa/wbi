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
@Document(indexName = "idateidx", type = "idatesearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class IndicatorDateSearch implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer date_id;

    //will be consist of PLAN_START/PLAN_END/
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String date_name;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date value;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String comment;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String status;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer group_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String group_name;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, store = true)
    private Integer task_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String task_name;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date last_updated_date;
}