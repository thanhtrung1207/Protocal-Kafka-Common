package com.example.config;

import java.util.Properties;

import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class KafkaConfig {

    @Value("${consumer.bootstrap-server}")
    public String CONSUME_BOOTSTRAP_SERVER;

    @Value("${consumer.partition-discovery-interval-ms}")
    public String CONSUME_INTERVAL_MS;
    
    @Value("${producer.bootstrap-server}")
    public String PRODUCER_BOOTSTRAP_SERVER;

    @Value("${producer.batch-size}")
    public String PRODUCER_BATCH_MAX_SIZE;

    @Value("${producer.acks}")
    public String PRODUCER_ACKS;

    @Value("${producer.linger-ms}")
    public String PRODUCER_LINGER_MS;

    @Value("${consumer.group-id}")
    public String CONSUMER_GROUP_ID;

    public int DEFAULT_PARTITIONS = 1;

    public short DEFAULT_REPLICATION_FACTOR = 1;


    public <T> KafkaSink<T> createKafkaSink(KafkaRecordSerializationSchema<T> serializer){
        Properties kafkaProducerProps = new Properties();
        kafkaProducerProps.setProperty("bootstrap.servers", CONSUME_BOOTSTRAP_SERVER);
        kafkaProducerProps.setProperty("batch.size", PRODUCER_BATCH_MAX_SIZE);
        kafkaProducerProps.setProperty("linger.ms", PRODUCER_LINGER_MS);
        kafkaProducerProps.setProperty("acks", PRODUCER_ACKS);

        return KafkaSink.<T>builder()
            .setKafkaProducerConfig(kafkaProducerProps)            
            .setRecordSerializer(serializer)
            .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .build();
    }
}
