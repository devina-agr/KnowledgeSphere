package com.example.knowledgesphere.ai.model;

import lombok.Data;

import java.util.List;

@Data
public class DoclingResponse {

    private List<StructuredPage> pages;

}