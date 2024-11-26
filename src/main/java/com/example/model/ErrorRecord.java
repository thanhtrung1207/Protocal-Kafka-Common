package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorRecord {
    
    @JsonProperty("reason")
    private String reason;

    @JsonProperty("data")
    private Object data;

    public String toJson() {
        try{
            return new ObjectMapper().writeValueAsString(this);
        }catch(Exception ex) {
            throw new RuntimeException("Failed to serialize ErrorRecord to JSON", ex);
        }
    }
}
