package com.example.service;

import java.io.IOException;

import com.example.config.ElasticsearchConfig;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
@ConfigurationProperties(prefix = "healthcheck")
@Service
public class ElasticsearchHealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHealthCheckService.class);

    private final RestHighLevelClient client;

    public ElasticsearchHealthCheckService(RestHighLevelClient client) {
        this.client = client;
    }

    @Getter
    @Setter
    private boolean elastic_search_flag;

    @Setter
    @Getter
    private int timeout;

    public boolean checkHealth() {
        try {
            MainResponse response = client.info(RequestOptions.DEFAULT);
            logger.info("✅ Connected to Elasticsearch: {}", response.getClusterName());
            return true;
        } catch (IOException e) {
            logger.error("❌ Failed to connect to Elasticsearch: Invalid credentials or host unreachable", e);
            return false;
        }
    }

    public boolean checkHealthElastisSearch(){
        logger.info("data{}", this.elastic_search_flag);
        logger.info("data1{}", this.timeout);

        if (!checkHealth()) {
            this.setElastic_search_flag(false);
            return false;
        }

        this.setElastic_search_flag(true);
        return true;

    }
//    public boolean checkHealthAndSendRecord(String index, String id, String jsonRecord) {
//        if (!checkHealth()) {
//            logger.warn("❌ Elasticsearch health check failed. Record will not be sent.");
//            return false;
//        }
//
//        try {
//            IndexRequest request = new IndexRequest(index).id(id).source(jsonRecord);
//            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
//            logger.info("✅ Record indexed successfully with ID: {}", response.getId());
//            return true;
//        } catch (IOException e) {
//            logger.error("❌ Failed to index record into Elasticsearch", e);
//            return false;
//        }
//    }
}
