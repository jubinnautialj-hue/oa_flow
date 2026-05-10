package com.oa.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CompleteTaskDTO {
    private String taskId;
    private String comment;
    private String outcome;
    private Map<String, Object> variables;
}
