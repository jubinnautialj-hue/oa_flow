package com.oa.controller;

import com.oa.dto.LoginRequest;
import com.oa.dto.LoginResponse;
import com.oa.entity.User;
import com.oa.security.JwtUtil;
import com.oa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String token = jwtUtil.generateToken(request.getUsername());
            User user = userService.findByUsername(request.getUsername()).orElse(null);

            return ResponseEntity.ok(new LoginResponse(token, request.getUsername(), user != null ? user.getName() : ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.badRequest().body("用户未登录");
        }
        return ResponseEntity.ok(user);
    }
}
