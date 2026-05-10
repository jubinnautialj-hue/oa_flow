package com.oa.dto;

import lombok.Data;

@Data
public class PositionDTO {
    private Long id;
    private String name;
    private String description;
    private Integer status;
}
