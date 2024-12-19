package com.example.service;

import java.util.concurrent.*;

import com.example.config.ElasticsearchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.config.FlinkJobConfig;
import com.example.factory.FlinkJobFactory;
import com.example.handler.interfaces.FlinkJob;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlinkJobService {

    ExecutorService executorService = Executors.newCachedThreadPool();

    private static final Logger logger = LoggerFactory.getLogger(FlinkJobService.class);

    @Autowired
    private FlinkJobFactory jobFactory;

    @Autowired
    private FlinkJobConfig jobConfig;

    @Autowired
    private ElasticsearchConfig elasticsearchConfig;
    @Value("${healthcheck.timeout}")
    private int CHECK_INTERVAL_SECONDS;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    public void startFlinkJob() {
        ElasticsearchHealthCheckService elasticsearchHealthCheckService
                = new ElasticsearchHealthCheckService(elasticsearchConfig.restHighLevelClient());
        startHealthCheckForever(elasticsearchHealthCheckService);

        for (String jobName : jobConfig.getEnabledJobs()) {
            if (jobFactory.isJobRegistered(jobName)) {
                executorService.submit(() -> executeJob(jobName));
            } else {
                System.err.println("⚠️ Job is not defined: " + jobName);
            }
        }
    }

    private void startHealthCheckForever( ElasticsearchHealthCheckService elasticsearchHealthCheckService) {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if(elasticsearchHealthCheckService.checkHealthElastisSearch()){
                    logger.info("✅ Successfully check Elasticsearch health");
                }
            } catch (Exception e) {
                logger.error("🔥 Error during Elasticsearch health check", e);
            }
        }, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS); // Initial delay of 0, then repeat every CHECK_INTERVAL_SECONDS
    }

    private void executeJob(String jobName) {
        try {
            FlinkJob flinkJob = jobFactory.getJob(jobName);
            flinkJob.onStarted();
            log.info("✅ Successfully executed job: {}", jobName);
        } catch (Exception e) {
            logger.error("🔥 Error executing job: {}", jobName, e);
        }
    }
}
