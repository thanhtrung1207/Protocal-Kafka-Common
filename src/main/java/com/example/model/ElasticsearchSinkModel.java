package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticsearchSinkModel<T> {
    
    public String indexName;

    public String indexDate;

    public String searchIndexName;

    public String id;

    public T data;
}
