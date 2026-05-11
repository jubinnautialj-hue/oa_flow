package com.oa.controller;

import com.oa.dto.UserDTO;
import com.oa.entity.User;
import com.oa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> list(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(userService.search(keyword));
        }
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO dto) {
        try {
            User user = userService.updateCurrentUserProfile(dto);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> params) {
        try {
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body("参数不完整");
            }
            userService.changePassword(oldPassword, newPassword);
            return ResponseEntity.ok("密码修改成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePasswordByAdmin(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            if (!userService.isCurrentUserAdmin()) {
                return ResponseEntity.status(403).body("没有权限执行此操作");
            }
            String newPassword = params.get("newPassword");
            if (newPassword == null) {
                return ResponseEntity.badRequest().body("参数不完整");
            }
            userService.changePasswordByAdmin(id, newPassword);
            return ResponseEntity.ok("密码修改成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/current/is-admin")
    public ResponseEntity<Boolean> isCurrentUserAdmin() {
        return ResponseEntity.ok(userService.isCurrentUserAdmin());
    }
}
