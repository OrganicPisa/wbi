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
@Document(indexName = "igroupidx", type = "igroupsearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class IndicatorGroupSearch implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private Integer igroup_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String igroup_name;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private Integer order_num;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, searchAnalyzer = "standard", store = true)
    private String remark;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String status;

    @Field(type = FieldType.Integer, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private Integer revision_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, searchAnalyzer = "standard", store = true)
    private String revision_name;

    @Field(type = FieldType.Date, index = FieldIndex.analyzed, searchAnalyzer = "standard", store = true)
    private Date last_updated_date;

}
