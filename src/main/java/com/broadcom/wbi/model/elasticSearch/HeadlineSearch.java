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
@Document(indexName = "headlineidx", type = "headlinesearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class HeadlineSearch implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, searchAnalyzer = "standard", store = true)
    private String headline;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String status;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private Integer revision_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String revision_name;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String stage;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String prediction_flag;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String schedule_flag;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String resource_flag;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String budget_flag;

    @Field(type = FieldType.Date, index = FieldIndex.analyzed, searchAnalyzer = "standard", store = true)
    private Date last_updated_date;

}
