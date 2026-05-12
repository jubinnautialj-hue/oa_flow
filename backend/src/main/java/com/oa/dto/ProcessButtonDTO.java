package com.oa.dto;

import lombok.Data;

@Data
public class ProcessButtonDTO {
    private Long id;
    private String buttonCode;
    private String buttonName;
    private String description;
    private Integer sort;
    private Integer status;
    private String processDefinitionKey;
    private String taskDefinitionKey;
    private String buttonType;
}
