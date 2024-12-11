package com.example.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCommonRequest<T> {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("serviceName")
    private String serviceName;

    @JsonProperty("payLoad")
    private T payLoad;
}
