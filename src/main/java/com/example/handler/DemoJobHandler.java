package com.example.handler;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.config.KafkaAdminConfig;
import com.example.config.KafkaConfig;
import com.example.config.KafkaJsonModelSchema;
import com.example.config.KafkaSinkRecordModelSchema;
import com.example.filter.DemoFilter;
import com.example.model.Demo;
import com.example.until.CommonUtils;

@Component
public class DemoJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(DemoJobHandler.class);

    private final RestHighLevelClient elasticsearchClient;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaAdminConfig kafkaAdminConfig;

    public DemoJobHandler(RestHighLevelClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void onStarted() {
        final StreamExecutionEnvironment env = getExecutionEnvironment();

        // Kafka source
        KafkaSource<Demo> source = kafkaAdminConfig.createKafkaSource(
            "wordcount-input",
            CommonUtils.getClientId("wordcount-input"),
            kafkaConfig.CONSUMER_GROUP_ID,
            new KafkaJsonModelSchema<>(Demo.class),
            OffsetsInitializer.earliest(),
            false
        );

        DataStream<Demo> inputStream = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            String.format("Kafka-Source: %s", "wordcount-input")
        );

        DataStream<Demo> filterStream = inputStream.filter(new DemoFilter());

        // Sink to Kafka
        KafkaSink<Demo> sinkKafka = kafkaConfig
            .createKafkaSink(new KafkaSinkRecordModelSchema<Demo>(
                "wordcount-output",
                e -> e.getAge(),
                e -> e
            ));

        filterStream.sinkTo(sinkKafka);

        try {
            env.execute("Demo");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StreamExecutionEnvironment getExecutionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // env.enableCheckpointing(1000);
        return env;
    }
}
