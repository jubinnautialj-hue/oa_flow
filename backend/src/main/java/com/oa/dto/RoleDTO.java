package com.oa.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RoleDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Set<Long> permissionIds;
}
