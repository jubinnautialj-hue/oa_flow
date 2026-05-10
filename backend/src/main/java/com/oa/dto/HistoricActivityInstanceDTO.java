package com.oa.dto;

import lombok.Data;

import java.util.Date;

@Data
public class HistoricActivityInstanceDTO {
    private String id;
    private String activityId;
    private String activityName;
    private String activityType;
    private String processDefinitionId;
    private String processInstanceId;
    private String executionId;
    private String taskId;
    private String assignee;
    private Date startTime;
    private Date endTime;
    private Long durationInMillis;
    private String deleteReason;
    private String tenantId;
}
