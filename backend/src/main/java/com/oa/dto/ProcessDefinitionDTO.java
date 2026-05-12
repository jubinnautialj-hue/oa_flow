package com.oa.dto;

import lombok.Data;

@Data
public class ProcessDefinitionDTO {
    private String id;
    private String name;
    private String key;
    private Integer version;
    private String deploymentId;
    private String description;
    private Boolean suspended;
}
