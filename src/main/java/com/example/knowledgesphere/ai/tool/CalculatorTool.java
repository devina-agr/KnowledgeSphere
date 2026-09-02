package com.example.knowledgesphere.ai.tool;

import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool {

    @Tool(description = "Evaluate mathematical expressions")
    public String calculate(String expression) {

        try {

            double result =
                    new ExpressionBuilder(expression)
                            .build()
                            .evaluate();

            return String.valueOf(result);

        } catch (Exception e) {

            return "Invalid mathematical expression.";

        }

    }

}