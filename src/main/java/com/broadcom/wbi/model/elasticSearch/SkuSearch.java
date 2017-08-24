package com.broadcom.wbi.model.elasticSearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Getter
@Setter
@Document(indexName = "skuidx", type = "skusearch", refreshInterval = "-1")
public class SkuSearch implements Serializable {
    @Id
    private String id;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String programDisplayName;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String programName;

    @Field(type = FieldType.Integer)
    private Integer program;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String programType;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String baseNum;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String url;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String aka;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String otherName;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String frequency;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String itemp;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String ioCapacity;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String numOfSerdes;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String portConfig;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String skuNum;

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String description;

    @Field(type = FieldType.String)
    private String dateAvailable;

}
