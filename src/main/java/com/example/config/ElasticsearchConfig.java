package com.example.config;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.model.OcbElasticsearchSinkModel;
import com.example.until.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ElasticsearchConfig {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Value("${elasticsearch.host}")
    private  String host;
    
    @Value("${elasticsearch.port}")
    private  String port;

    @Value("${elasticsearch.schema}")
    private  String schema;

    @Value("${elasticsearch.username}")
    private  String username;

    @Value("${elasticsearch.password}")
    private  String password;
    
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, NumberUtils.toInt(port), schema))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                );

        return new RestHighLevelClient(builder);
    }

    private static String createIndex(String indexName, String indexDate) {
        return indexDate == null || indexDate.isEmpty()
            ? indexName
            : String.format("%s-%s", indexName, indexDate);
    }
    
    private static <T> IndexRequest createIndexRequest(OcbElasticsearchSinkModel<T> element) {
        try {
            String json = JsonUtils.mapToString(element.getData());
            Map<String, Object> jsonMap = new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {});
            String idx = createIndex(element.getIndexName(), element.getIndexDate());
            return Requests.indexRequest()
                .index(idx)
                .id(element.getId())
                .source(jsonMap);
        } catch (Exception e) {
            logger.error("Error creating IndexRequest for element: {}", element, e);
            throw new RuntimeException("Failed to create IndexRequest", e); 
        }
    }

    @Bean
    public <T> ElasticsearchSink<OcbElasticsearchSinkModel<T>> elasticsearchSink() {
        HttpHost httpHost = new HttpHost(host, NumberUtils.toInt(port), schema);

        return new Elasticsearch7SinkBuilder<OcbElasticsearchSinkModel<T>>()
            .setHosts(httpHost)
            .setEmitter((element, context, indexer) -> {
                try {
                    indexer.add(createIndexRequest(element));
                } catch (Exception e) {
                    logger.error("Failed to emit data to Elasticsearch for element: {}", element, e);
                }
            })
            .setBulkFlushBackoffStrategy(FlushBackoffType.EXPONENTIAL, 5, 1000)
            .setConnectionUsername(username)
            .setConnectionPassword(password)
            .build();
    }
}
