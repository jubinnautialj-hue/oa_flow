package com.oa.dto;

import lombok.Data;

@Data
public class MenuDTO {
    private Long id;
    private String name;
    private String path;
    private String icon;
    private Integer sort;
    private Long parentId;
    private Integer status;
}
