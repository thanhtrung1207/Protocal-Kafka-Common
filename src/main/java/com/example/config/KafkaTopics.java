package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopics {
    
    @Value("topic.input")
    private String inputTopic;

    @Value("topic.output")
    private String outputTopic;
}
