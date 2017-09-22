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
@Document(indexName = "resourceactualidx", type = "resourceactualsearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class ResourceActualSearch implements Serializable {

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String employee;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String employee_name;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String employee_id;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String employee_status;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String employee_type;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String manager;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String manager_name;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String manager_id;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String project;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String project_name;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String project_type;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    String skill;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String skill_name;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String cost_center;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String loc_country;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String loc_name;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String charged_to;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String charged_from;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String profit_center_group;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String profit_center_project;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String category;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String sub_category;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String design_center;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    String segment_name;
    @Field(type = FieldType.Date, index = FieldIndex.analyzed, store = true)
    Date month;
    @Field(type = FieldType.Date, index = FieldIndex.analyzed, store = true)
    Date last_updated_time;
    @Field(type = FieldType.Double, index = FieldIndex.analyzed, store = true)
    Double count;
    @Field(type = FieldType.Integer, index = FieldIndex.analyzed, store = true)
    Integer approved_skill;
    @Field(type = FieldType.Integer, index = FieldIndex.analyzed, store = true)
    Integer approved_project;
    @Id
    private String id;

}