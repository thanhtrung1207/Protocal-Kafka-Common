package com.example.handler;

import java.time.LocalDate;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchSink;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.config.ElasticsearchConfig;
import com.example.config.KafkaAdminConfig;
import com.example.config.KafkaConfig;
import com.example.config.KafkaJsonModelSchema;
import com.example.config.KafkaSinkRecordModelSchema;
import com.example.filter.DemoFilter;
import com.example.handler.interfaces.FlinkJob;
import com.example.map.ElasticsearchMapFunction;
import com.example.model.Demo;
import com.example.model.ElasticsearchSinkModel;
import com.example.until.CommonUtils;

@Component("demoJob")
public class DemoJobHandler implements FlinkJob {

    // private static final Logger logger = LoggerFactory.getLogger(DemoJobHandler.class);

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaAdminConfig kafkaAdminConfig;

    @Autowired
    private ElasticsearchConfig eConfig;

    @Override
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

        DataStream<ElasticsearchSinkModel<Demo>> mapFunctionToEs = filterStream.map(new ElasticsearchMapFunction<Demo, ElasticsearchSinkModel<Demo>>(
            "demo", 
            LocalDate.now().toString(),
            "id",
            "age"
        ));

        // Sink to Kafka
        KafkaSink<Demo> sinkKafka = kafkaConfig
            .createKafkaSink(new KafkaSinkRecordModelSchema<Demo>(
                "wordcount-output",
                e -> e.setId(),
                e -> e
            ));

        ElasticsearchSink<ElasticsearchSinkModel<Demo>> sinkToElastic = eConfig.elasticsearchSink();

        filterStream.sinkTo(sinkKafka);
        mapFunctionToEs.sinkTo(sinkToElastic);

        try {
            env.execute("Demo");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StreamExecutionEnvironment getExecutionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getStateBackend();
        // env.setStateBackend();
        env.enableCheckpointing(1000);
        return env;
    }
}
