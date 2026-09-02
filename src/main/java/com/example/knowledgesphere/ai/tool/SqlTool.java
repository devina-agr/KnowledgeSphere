package com.example.knowledgesphere.ai.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlTool {

    private final JdbcTemplate jdbcTemplate;

    @Tool(description = "Executes a read-only SQL query")
    public List<Map<String,Object>> execute(String sql){

        return jdbcTemplate.queryForList(sql);

    }

}