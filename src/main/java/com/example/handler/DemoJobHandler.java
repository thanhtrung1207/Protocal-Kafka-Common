package com.example.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

import com.example.until.JsonUtils;
import com.example.until.TimeData;
import lombok.Getter;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "healthcheck")
public class DemoJobHandler implements FlinkJob {

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaAdminConfig kafkaAdminConfig;

    @Autowired
    private ElasticsearchConfig eConfig;

    @Autowired
    private DemoFilter demoFilter; // Injected via Spring

    @Getter
    private boolean elastic_search_flag;

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
        if(this.elastic_search_flag) {
            DataStream<ElasticsearchSinkModel<Demo>> mapFunctionToEs = filterStream
                    .map(new ElasticsearchMapFunction<Demo, ElasticsearchSinkModel<Demo>>(
                            "demo1",
                            LocalDate.now().toString(),
                            "id",
                            "age"));
            ElasticsearchSink<ElasticsearchSinkModel<Demo>> sinkToElastic = eConfig.elasticsearchSink();
            mapFunctionToEs.sinkTo(sinkToElastic);
        }
        else {
            this.SinkToJsonFile(filterStream);
        }

        // Sink to Kafka
        KafkaSink<Demo> sinkKafka = kafkaConfig
                .createKafkaSink(new KafkaSinkRecordModelSchema<Demo>(
                        "wordcount-output1",
                        e -> e.getAge(),
                        e -> e));


        filterStream.sinkTo(sinkKafka);

        try {
            env.execute("Demo1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SinkToJsonFile(DataStream<Demo> filterStream){
        filterStream.map(demo -> {
            try {
                TimeData<Demo> timeData = new TimeData<>(demo);
                return JsonUtils.mapToString(timeData);
            } catch (IOException e) {
                System.out.println("❌ Error when try to write into json file");
                return null; // Hoặc xử lý giá trị mặc định
            }
        }).filter(json -> json != null) // Loại bỏ các giá trị null
        .writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE)
        .setParallelism(1);
    }
    private static StreamExecutionEnvironment getExecutionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(1000);
        return env;
    }
}
