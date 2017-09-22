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
@Document(indexName = "revisionidx", type = "revisionsearch", shards = 1, replicas = 0, refreshInterval = "-1")
public class RevisionSearch implements Serializable {

    @Id
    private String id;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String rev_name;

    @Field(type = FieldType.Boolean, index = FieldIndex.analyzed, store = true)
    private Boolean is_active;

    @Field(type = FieldType.Boolean, index = FieldIndex.analyzed, store = true)
    private Boolean include_in_report;

    @Field(type = FieldType.Boolean, index = FieldIndex.analyzed, store = true)
    private Boolean is_protected;

    @Field(type = FieldType.Integer, index = FieldIndex.analyzed, store = true)
    private Integer program_id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed, store = true)
    private String program_name;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String base_num;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String program_type;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String segment;

    @Field(type = FieldType.Integer, index = FieldIndex.analyzed, store = true)
    private Integer rev_order_num;

    @Field(type = FieldType.Integer, index = FieldIndex.analyzed, store = true)
    private Integer program_order_num;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String ip_related;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = true)
    private String outlook;

    @Field(type = FieldType.Date, index = FieldIndex.analyzed, store = true)
    private Date last_updated_outlook_date;

    @Override
    public String toString() {
        return id + " " + base_num + " " + program_name + " " + rev_name.toUpperCase() + " ( " + segment.toUpperCase() + " ) ";
    }

}