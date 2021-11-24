package com.example.house.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

@Configuration
public class ElasticSearchConfig {
//    //@Value("${elasticsearch.cluster.name}")
//    private String esName = "elasticsearch_peter";
//
//    @Bean
//    public TransportClient esClient() throws UnknownHostException {
//        Settings settings = Settings.builder()
//                .put("cluster.name", this.esName)
////                .put("cluster.name", "elasticsearch")
//                .put("client.transport.sniff", true)
//                .build();
//
//        TransportClient client = new PreBuiltTransportClient(settings);
//        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9301));
//        return client;
//    }

    @Bean
    RestHighLevelClient restHighLevelClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200").build();
        return RestClients.create(clientConfiguration).rest();
    }
}
