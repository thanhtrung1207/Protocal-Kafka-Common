package com.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.example.config.KafkaConfig;
import com.example.service.FlinkJobService;

@SpringBootApplication
public class Main implements CommandLineRunner{

    @Autowired
    private FlinkJobService flinkJobService;
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        KafkaConfig kafkaConfig = context.getBean(KafkaConfig.class);
        // FlinkJobConfig jobConfig = context.getBean(FlinkJobConfig.class);
        // KafkaAdminConfig kafkaAdminConfig = context.getBean(KafkaAdminConfig.class);

        // System.out.println("Bootstrap Server: " + kafkaConfig.CONSUME_BOOTSTRAP_SERVER);
        // System.out.println("List: " + jobConfig.enabledJobs);
    }

    @Override
    public void run(String... args) throws Exception {
        flinkJobService.startFlinkJob();;
    }
}
