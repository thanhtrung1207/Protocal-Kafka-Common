package com.example.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCommonResponse<T> {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createDateTime")
    private LocalDateTime createdDateTime;

    @JsonProperty("payload")
    private T payload;
}
