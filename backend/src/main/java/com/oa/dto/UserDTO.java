package com.oa.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Long departmentId;
    private Long positionId;
    private Set<Long> roleIds;
}
