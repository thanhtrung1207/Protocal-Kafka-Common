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
public class Demo {

    @JsonProperty("id")
    private String id;

    @JsonProperty("age")
    private String age;

    @JsonProperty("name")
    private String name;
}
