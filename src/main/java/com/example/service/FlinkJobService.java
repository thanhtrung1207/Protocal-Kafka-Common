package com.example.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void startFlinkJob() {
        for (String jobName : jobConfig.getEnabledJobs()) {
            if (jobFactory.isJobRegistered(jobName)) {
                executorService.submit(() -> executeJob(jobName));
            } else {
                System.err.println("⚠️ Job is not defined: " + jobName);
            }
        }
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
