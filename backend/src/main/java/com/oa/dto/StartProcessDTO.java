package com.oa.dto;

import lombok.Data;

@Data
public class StartProcessDTO {
    private String processDefinitionKey;
    private String businessKey;
    private String title;
}
