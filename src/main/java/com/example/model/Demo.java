package com.example.model;

import org.apache.commons.lang3.RandomStringUtils;

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

    public String setId(){
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = true;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
        String result = String.format("traceId-%s-%s", this.id, generatedString);
        return result;
    }
}
