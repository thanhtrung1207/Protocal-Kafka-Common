package com.example.factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.handler.interfaces.FlinkJob;

import lombok.Data;

import java.util.Map;

@Component
@Data
public class FlinkJobFactory {

    private final Map<String, FlinkJob> jobMap;

    @Autowired
    public FlinkJobFactory(ApplicationContext context) {
        this.jobMap = context.getBeansOfType(FlinkJob.class);
    }

    public FlinkJob getJob(String jobName) {
        return jobMap.get(jobName);
    }

    public boolean isJobRegistered(String jobName) {
        return jobMap.containsKey(jobName);
    }
}
