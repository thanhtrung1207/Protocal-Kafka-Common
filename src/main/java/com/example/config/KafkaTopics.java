package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopics {
    
    @Value("topic.input")
    public static String inputTopic;

    @Value("topic.output")
    public static String outputTopic;
}
