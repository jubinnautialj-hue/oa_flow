package com.oa.dto;

import lombok.Data;

import java.util.Date;

@Data
public class HistoricProcessInstanceDTO {
    private String id;
    private String name;
    private String businessKey;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private Integer processDefinitionVersion;
    private Date startTime;
    private Date endTime;
    private Long durationInMillis;
    private String startUserId;
    private String startActivityId;
    private String endActivityId;
    private String deleteReason;
    private String tenantId;
}
