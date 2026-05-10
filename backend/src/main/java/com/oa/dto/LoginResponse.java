package com.oa.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String name;

    public LoginResponse(String token, String username, String name) {
        this.token = token;
        this.username = username;
        this.name = name;
    }
}
