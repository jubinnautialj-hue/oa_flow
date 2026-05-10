package com.oa.config;

import com.oa.entity.Permission;
import com.oa.entity.Role;
import com.oa.entity.User;
import com.oa.repository.PermissionRepository;
import com.oa.repository.RoleRepository;
import com.oa.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========================================");
        log.info("开始检查并初始化系统数据...");
        log.info("========================================");

        initPermissions();
        initRoles();
        initDefaultUsers();

        log.info("========================================");
        log.info("系统数据初始化完成!");
        log.info("默认账号:");
        log.info("  管理员: admin / 123456");
        log.info("  部门经理: manager / 123456");
        log.info("  普通用户: user / 123456");
        log.info("========================================");
    }

    @Transactional
    protected void initPermissions() {
        createPermissionIfNotExists("ADMIN", "系统管理员", "系统超级管理员权限");
        createPermissionIfNotExists("USER_VIEW", "用户查看", "查看用户列表");
        createPermissionIfNotExists("USER_EDIT", "用户编辑", "新增、编辑、删除用户");
        createPermissionIfNotExists("PROCESS_VIEW", "流程查看", "查看流程定义");
        createPermissionIfNotExists("PROCESS_MANAGE", "流程管理", "部署、删除流程");
        createPermissionIfNotExists("TASK_VIEW", "任务查看", "查看任务列表");
        createPermissionIfNotExists("TASK_HANDLE", "任务处理", "办理、签收任务");
    }

    @Transactional
    protected void initRoles() {
        Set<Permission> allPermissions = new HashSet<>();
        allPermissions.add(getPermission("ADMIN"));
        allPermissions.add(getPermission("USER_VIEW"));
        allPermissions.add(getPermission("USER_EDIT"));
        allPermissions.add(getPermission("PROCESS_VIEW"));
        allPermissions.add(getPermission("PROCESS_MANAGE"));
        allPermissions.add(getPermission("TASK_VIEW"));
        allPermissions.add(getPermission("TASK_HANDLE"));
        createOrUpdateRole("ADMIN", "系统管理员", "系统超级管理员", allPermissions);

        Set<Permission> managerPermissions = new HashSet<>();
        managerPermissions.add(getPermission("USER_VIEW"));
        managerPermissions.add(getPermission("PROCESS_VIEW"));
        managerPermissions.add(getPermission("PROCESS_MANAGE"));
        managerPermissions.add(getPermission("TASK_VIEW"));
        managerPermissions.add(getPermission("TASK_HANDLE"));
        createOrUpdateRole("MANAGER", "部门经理", "部门经理角色", managerPermissions);

        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(getPermission("PROCESS_VIEW"));
        userPermissions.add(getPermission("TASK_VIEW"));
        userPermissions.add(getPermission("TASK_HANDLE"));
        createOrUpdateRole("USER", "普通用户", "普通用户角色", userPermissions);
    }

    @Transactional
    protected void initDefaultUsers() {
        createOrUpdateUser("admin", "系统管理员", "admin@example.com", "13800138000", getRole("ADMIN"));
        createOrUpdateUser("manager", "部门经理", "manager@example.com", "13800138001", getRole("MANAGER"));
        createOrUpdateUser("user", "普通用户", "user@example.com", "13800138002", getRole("USER"));
    }

    private void createPermissionIfNotExists(String code, String name, String description) {
        if (!permissionRepository.existsByCode(code)) {
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name);
            permission.setDescription(description);
            permission.setStatus(1);
            permissionRepository.save(permission);
            log.info("创建权限: {}", code);
        }
    }

    private Permission getPermission(String code) {
        return permissionRepository.findByCode(code).orElse(null);
    }

    private void createOrUpdateRole(String code, String name, String description, Set<Permission> permissions) {
        Optional<Role> existingRole = roleRepository.findByCode(code);
        Role role;

        if (existingRole.isPresent()) {
            role = existingRole.get();
            role.setName(name);
            role.setDescription(description);
            role.setPermissions(permissions);
            log.info("更新角色: {}", code);
        } else {
            role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            role.setStatus(1);
            role.setPermissions(permissions);
            log.info("创建角色: {}", code);
        }
        roleRepository.save(role);
    }

    private Role getRole(String code) {
        return roleRepository.findByCode(code).orElse(null);
    }

    private void createOrUpdateUser(String username, String name, String email, String phone, Role role) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("检查用户: {} (ID: {})", username, user.getId());

            boolean passwordNeedsUpdate = false;
            try {
                if (!passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                    passwordNeedsUpdate = true;
                }
            } catch (Exception e) {
                log.warn("用户 {} 的密码格式无效，需要重置", username);
                passwordNeedsUpdate = true;
            }

            if (passwordNeedsUpdate) {
                user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                log.info("重置用户 {} 的密码为: {}", username, DEFAULT_PASSWORD);
            }

            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);

            Set<Role> roles = new HashSet<>();
            if (role != null) {
                roles.add(role);
            }
            user.setRoles(roles);
            userRepository.save(user);
            log.info("更新用户: {}", username);

        } else {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            user.setStatus(1);

            Set<Role> roles = new HashSet<>();
            if (role != null) {
                roles.add(role);
            }
            user.setRoles(roles);
            userRepository.save(user);
            log.info("创建用户: {} (密码: {})", username, DEFAULT_PASSWORD);
        }
    }
}
