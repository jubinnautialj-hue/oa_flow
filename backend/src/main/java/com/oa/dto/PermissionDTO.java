package com.oa.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
}
