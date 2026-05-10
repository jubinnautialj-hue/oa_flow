package com.oa.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DeploymentDTO {
    private String id;
    private String name;
    private Date deploymentTime;
    private String category;
    private String tenantId;
}
