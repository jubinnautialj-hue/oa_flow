package com.oa.dto;

import lombok.Data;

import java.util.Date;

@Data
public class HistoricTaskInstanceDTO {
    private String id;
    private String name;
    private String description;
    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionName;
    private String processDefinitionKey;
    private String assignee;
    private String owner;
    private Date startTime;
    private Date endTime;
    private Long durationInMillis;
    private Long workTimeInMillis;
    private String deleteReason;
    private String taskDefinitionKey;
    private Integer priority;
    private Date dueDate;
    private String parentTaskId;
    private String category;
    private String tenantId;
}
