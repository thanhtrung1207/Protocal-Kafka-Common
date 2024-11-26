package com.example.service;

import java.io.IOException;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchHealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHealthCheckService.class);
    private final RestHighLevelClient client;

    public ElasticsearchHealthCheckService(RestHighLevelClient client) {
        this.client = client;
    }

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

    public boolean checkHealthAndSendRecord(String index, String id, String jsonRecord) {
        if (!checkHealth()) {
            logger.warn("❌ Elasticsearch health check failed. Record will not be sent.");
            return false;
        }

        try {
            IndexRequest request = new IndexRequest(index).id(id).source(jsonRecord);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            logger.info("✅ Record indexed successfully with ID: {}", response.getId());
            return true;
        } catch (IOException e) {
            logger.error("❌ Failed to index record into Elasticsearch", e);
            return false;
        }
    }
}
