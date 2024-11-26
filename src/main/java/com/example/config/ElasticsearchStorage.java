package com.example.config;

import java.io.IOException;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.config.interfaces.StorageStrategy;
import com.example.model.ErrorRecord;

public class ElasticsearchStorage implements StorageStrategy{
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchStorage.class);
    private final RestHighLevelClient client;
    private final String index;

    public ElasticsearchStorage(RestHighLevelClient client, String index) {
        this.client = client;
        this.index = index;
    }

    @Override
    public void store(ErrorRecord errorRecord) {
        try{
            IndexRequest request = new IndexRequest(index)
                .source(errorRecord.toJson(), XContentType.JSON);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            logger.info("✅ Logged to Elasticsearch (ID: {}): {}", response.getId(), errorRecord);
        }catch(IOException e) {
            logger.error("❌ Failed to log to Elasticsearch", e);
        }
    }
    
}
