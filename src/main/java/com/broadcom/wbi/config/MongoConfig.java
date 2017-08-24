package com.broadcom.wbi.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Value("${mongo.host}")
    private String host;
    @Value("${mongo.port}")
    private int port;
    @Value("${mongo.database}")
    private String dbName;

    @Bean
    public Mongo mongo() throws Exception {
        return new MongoClient(host);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), dbName);
    }


}
