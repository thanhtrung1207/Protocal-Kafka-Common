package com.example.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.handler.DemoJobHandler;

import scala.tools.jline_embedded.internal.Log;

@Service
public class FlinkJobService {

    @Autowired
    private DemoJobHandler flinkHandler;

    public void startFlinkJob() {
        try {
            Log.info("Start job");
            flinkHandler.onStarted();
        } catch (Exception e) {
            System.err.println("Failed to start Flink job: " + e.getMessage());
        }
    }
}
