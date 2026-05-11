package com.oa.service;

import com.oa.dto.UserDTO;
import com.oa.entity.Role;
import com.oa.entity.User;
import com.oa.repository.DepartmentRepository;
import com.oa.repository.PositionRepository;
import com.oa.repository.RoleRepository;
import com.oa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> search(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return userRepository.search(keyword);
        }
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User create(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        
        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(user::setDepartment);
        }
        if (dto.getPositionId() != null) {
            positionRepository.findById(dto.getPositionId()).ifPresent(user::setPosition);
        }
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            dto.getRoleIds().forEach(roleId -> {
                roleRepository.findById(roleId).ifPresent(roles::add);
            });
            user.setRoles(roles);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        
        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(user::setDepartment);
        } else {
            user.setDepartment(null);
        }
        if (dto.getPositionId() != null) {
            positionRepository.findById(dto.getPositionId()).ifPresent(user::setPosition);
        } else {
            user.setPosition(null);
        }
        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            dto.getRoleIds().forEach(roleId -> {
                roleRepository.findById(roleId).ifPresent(roles::add);
            });
            user.setRoles(roles);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public User updateCurrentUserProfile(UserDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changePasswordByAdmin(Long userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getCode()) || "超级管理员".equals(role.getName()));
    }
}
