package com.oa.dto;

import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer status;
}
