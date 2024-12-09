package com.example.handler;

import java.time.LocalDate;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.config.ElasticsearchConfig;
import com.example.config.KafkaAdminConfig;
import com.example.config.KafkaConfig;
import com.example.config.KafkaJsonModelSchema;
import com.example.config.KafkaSinkRecordModelSchema;
import com.example.config.KafkaTopics;
import com.example.filter.DemoFilter;
import com.example.handler.interfaces.FlinkJob;
import com.example.map.ElasticsearchMapFunction;
import com.example.model.Demo;
import com.example.model.ElasticsearchSinkModel;
import com.example.until.CommonUtils;

@Component("demoJob")
public class DemoJobHandler implements FlinkJob {

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaAdminConfig kafkaAdminConfig;

    @Autowired
    private ElasticsearchConfig eConfig;

    @Autowired
    private DemoFilter demoFilter; // Injected via Spring

    @Override
    public void onStarted() {
        if (kafkaConfig == null) {
            throw new IllegalStateException("KafkaConfig is not injected!");
        }

        final StreamExecutionEnvironment env = getExecutionEnvironment();
        String clientId = String.format("%s-sinkRest-%s-Normal", kafkaConfig.CONSUMER_GROUP_ID, KafkaTopics.inputTopic);
        System.out.printf("clientID: {}", clientId);
        // Kafka source
        KafkaSource<Demo> source = kafkaAdminConfig.createKafkaSource(
                "wordcount-input",
                CommonUtils.getClientId("wordcount-input"),
                clientId,
                new KafkaJsonModelSchema<>(Demo.class),
                OffsetsInitializer.earliest(),
                false);

        DataStream<Demo> inputStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                String.format("Kafka-Source: %s", "wordcount-input"));

        // Sử dụng demoFilter đã được inject
        DataStream<Demo> filterStream = inputStream.filter(demoFilter);

        DataStream<ElasticsearchSinkModel<Demo>> mapFunctionToEs = filterStream
                .map(new ElasticsearchMapFunction<Demo, ElasticsearchSinkModel<Demo>>(
                        "demo1",
                        LocalDate.now().toString(),
                        "id",
                        "age"));

        // Sink to Kafka
        KafkaSink<Demo> sinkKafka = kafkaConfig
                .createKafkaSink(new KafkaSinkRecordModelSchema<Demo>(
                        "wordcount-output1",
                        e -> e.getAge(),
                        e -> e));

        ElasticsearchSink<ElasticsearchSinkModel<Demo>> sinkToElastic = eConfig.elasticsearchSink();

        filterStream.sinkTo(sinkKafka);
        mapFunctionToEs.sinkTo(sinkToElastic);

        try {
            env.execute("Demo1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StreamExecutionEnvironment getExecutionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(1000);
        return env;
    }
}
