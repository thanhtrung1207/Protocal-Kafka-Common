package com.example.until;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeData<T>{
    private T data;
    private String timestamp;

    public TimeData(T data) {
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public T getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
