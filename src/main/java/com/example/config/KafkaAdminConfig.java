package com.example.config;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaAdminConfig {
    private final KafkaConfig config;

    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminConfig.class);
    public <T> KafkaSource<T> createKafkaSource(String topic, String clientIdPrefix, String consumerGroupId, DeserializationSchema<T> deserializer, OffsetsInitializer offset, boolean isMasterData){
        ensureTopicExists(topic);
        return createKafkaSourceBase(topic, clientIdPrefix, consumerGroupId, deserializer, offset, isMasterData);
    }

    private <T> KafkaSource<T> createKafkaSourceBase(String topic, String clientIdPrefix, String consumerGroupId, DeserializationSchema<T> deserializer, OffsetsInitializer offset, boolean isMasterData) {
        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("bootstrap.servers", config.CONSUME_BOOTSTRAP_SERVER);
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", config.CONSUME_INTERVAL_MS);

        return KafkaSource.<T>builder()
                    .setValueOnlyDeserializer(deserializer)
                    .setClientIdPrefix(clientIdPrefix)
                    .setGroupId(consumerGroupId)
                    .setTopics(topic)
                    .setProperties(kafkaConsumerProps)
                    .setStartingOffsets(isMasterData == false ? (OffsetsInitializer.committedOffsets(offset == OffsetsInitializer.earliest() ? OffsetResetStrategy.EARLIEST : OffsetResetStrategy.LATEST)) : offset)
                    .build();
    }

    private void ensureTopicExists(String topic) {
        try (AdminClient adminClient = createAdminClient()) {
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> existingTopics = topics.names().get();

            if (!existingTopics.contains(topic)) {
                logger.info("Topic '{}' does not exist. Creating it...", topic);
                NewTopic newTopic = new NewTopic(topic, config.DEFAULT_PARTITIONS, config.DEFAULT_REPLICATION_FACTOR);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                logger.info("Topic '{}' has been created.", topic);
            } else {
                logger.info("Topic '{}' already exists.", topic);
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to check or create topic '{}'", topic, e);
            Thread.currentThread().interrupt();
        }
    }

    private AdminClient createAdminClient() {
        Properties adminProps = new Properties();
        adminProps.setProperty("bootstrap.servers", config.CONSUME_BOOTSTRAP_SERVER);
        return AdminClient.create(adminProps);
    }
}
