package com.example.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class FlinkJobConfig {
    
    @Value("${flinkjobs.enabledJobs}")
    public List<String> enabledJobs;
}
