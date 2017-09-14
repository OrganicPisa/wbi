package com.broadcom.wbi.model.elasticSearch;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Document(indexName = "employeeidx", type = "employeesearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class EmployeeSearch implements Serializable {

    @Id
    private String id;
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String profit_center;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String cost_center;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String segment;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String last_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String first_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String middle_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String full_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String email;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String email_alias;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String acc_nt;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String acc_unix;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String office_num;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String phone_business;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String mobile;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String mobile2;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String title;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String supervisor;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String primary_loc_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String primary_loc_country;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String primary_loc_state;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String primary_loc_city;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String job_function_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String job_family_name;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String job_subfunction_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String design_center;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_1;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_2;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_3;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_4;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_5;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_6;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_7;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_8;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String org_level_9;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date start_date;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String status;

    @Field(type = FieldType.Date, index = FieldIndex.not_analyzed, store = true)
    private Date term_date;

    @JsonIgnore
    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String is_manager;

}