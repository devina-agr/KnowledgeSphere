package com.example.knowledgesphere.ai.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExtractionResponse {

    private List<StructuredPage> pages = new ArrayList<>();

}
