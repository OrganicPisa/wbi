package com.broadcom.wbi.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Configuration
public class ElasticSearchConfig {

    @Value("${es.host}")
    private String host;
    @Value("${es.port}")
    private int port;
    @Value("${es.clusterName")
    private String clusterName;

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws Exception {

        return new ElasticsearchTemplate(client());
    }

    @SuppressWarnings("resource")
    @Bean
    public Client client() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put("client.transport.sniff", true)
                .put("client.transport.ping_timeout", 15, TimeUnit.MINUTES)//15 minute
                .put("client.transport.nodes_sampler_interval", 15, TimeUnit.MINUTES)//15 minute
                .put("http.enabled", true)//1 minute

                .put("client.transport.ignore_cluster_name", true)
//                .put("cluster_name", clusterName)
                .build();


        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }
}