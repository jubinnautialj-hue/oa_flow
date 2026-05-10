package com.oa.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TaskDTO {
    private String id;
    private String name;
    private String description;
    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionName;
    private String processDefinitionKey;
    private String assignee;
    private String owner;
    private Date createTime;
    private Date dueDate;
    private String priority;
    private String category;
    private String formKey;
    private String parentTaskId;
    private String tenantId;
}
