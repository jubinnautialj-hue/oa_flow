package com.oa.config;

import com.oa.entity.Menu;
import com.oa.entity.Permission;
import com.oa.entity.ProcessButton;
import com.oa.entity.Role;
import com.oa.entity.User;
import com.oa.repository.MenuRepository;
import com.oa.repository.PermissionRepository;
import com.oa.repository.ProcessButtonRepository;
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
    private MenuRepository menuRepository;

    @Autowired
    private ProcessButtonRepository processButtonRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========================================");
        log.info("开始检查并初始化系统数据...");
        log.info("========================================");

        initPermissions();
        initMenus();
        initRoles();
        initDefaultUsers();
        initButtons();

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
        createPermissionIfNotExists("ADMIN", "系统管理员", "系统超级管理员权限", "FUNCTION");
        
        createPermissionIfNotExists("USER_VIEW", "用户查看", "查看用户列表", "FUNCTION");
        createPermissionIfNotExists("USER_EDIT", "用户编辑", "新增、编辑、删除用户", "FUNCTION");
        createPermissionIfNotExists("ROLE_VIEW", "角色查看", "查看角色列表", "FUNCTION");
        createPermissionIfNotExists("ROLE_EDIT", "角色编辑", "新增、编辑、删除角色", "FUNCTION");
        createPermissionIfNotExists("ROLE_PERMISSION", "角色权限分配", "分配角色权限", "FUNCTION");
        createPermissionIfNotExists("MENU_VIEW", "菜单查看", "查看菜单列表", "FUNCTION");
        createPermissionIfNotExists("MENU_EDIT", "菜单编辑", "新增、编辑、删除菜单", "FUNCTION");
        createPermissionIfNotExists("DEPARTMENT_VIEW", "部门查看", "查看部门列表", "FUNCTION");
        createPermissionIfNotExists("DEPARTMENT_EDIT", "部门编辑", "新增、编辑、删除部门", "FUNCTION");
        createPermissionIfNotExists("POSITION_VIEW", "岗位查看", "查看岗位列表", "FUNCTION");
        createPermissionIfNotExists("POSITION_EDIT", "岗位编辑", "新增、编辑、删除岗位", "FUNCTION");
        
        createPermissionIfNotExists("PROCESS_VIEW", "流程查看", "查看流程定义", "FUNCTION");
        createPermissionIfNotExists("PROCESS_MANAGE", "流程管理", "部署、删除流程", "FUNCTION");
        createPermissionIfNotExists("TASK_VIEW", "任务查看", "查看任务列表", "FUNCTION");
        createPermissionIfNotExists("TASK_HANDLE", "任务处理", "办理、签收任务", "FUNCTION");
        
        createPermissionIfNotExists("DATA_ALL", "全部数据", "查看和操作全部数据", "DATA");
        createPermissionIfNotExists("DATA_DEPARTMENT", "本部门数据", "查看和操作本部门数据", "DATA");
        createPermissionIfNotExists("DATA_SELF", "仅本人数据", "仅查看和操作本人数据", "DATA");
        
        createPermissionIfNotExists("API_USER_CREATE", "创建用户接口", "调用创建用户接口", "INTERFACE");
        createPermissionIfNotExists("API_USER_UPDATE", "更新用户接口", "调用更新用户接口", "INTERFACE");
        createPermissionIfNotExists("API_USER_DELETE", "删除用户接口", "调用删除用户接口", "INTERFACE");
        createPermissionIfNotExists("API_PROCESS_START", "发起流程接口", "调用发起流程接口", "INTERFACE");
        createPermissionIfNotExists("API_TASK_COMPLETE", "完成任务接口", "调用完成任务接口", "INTERFACE");
    }

    @Transactional
    protected void initMenus() {
        Long systemMenuId = createMenuIfNotExists("系统管理", null, "Setting", 1, null, 1);
        Long workflowMenuId = createMenuIfNotExists("工作流", null, "Connection", 2, null, 1);
        
        createMenuIfNotExists("用户管理", "users", "User", 1, systemMenuId, 1);
        createMenuIfNotExists("角色管理", "roles", "UserFilled", 2, systemMenuId, 1);
        createMenuIfNotExists("菜单管理", "menus", "Menu", 3, systemMenuId, 1);
        createMenuIfNotExists("按钮管理", "button-management", "Promotion", 4, systemMenuId, 1);
        createMenuIfNotExists("部门管理", "departments", "OfficeBuilding", 5, systemMenuId, 1);
        createMenuIfNotExists("岗位管理", "positions", "Medal", 6, systemMenuId, 1);
        
        createMenuIfNotExists("流程设计", "process-design", "Edit", 1, workflowMenuId, 1);
        createMenuIfNotExists("表单设计", "form-design", "EditPen", 2, workflowMenuId, 1);
        createMenuIfNotExists("流程定义", "process-definitions", "Collection", 3, workflowMenuId, 1);
        createMenuIfNotExists("部署管理", "deployment-management", "UploadFilled", 4, workflowMenuId, 1);
        createMenuIfNotExists("运维调度", "operation-dispatch", "Operation", 5, workflowMenuId, 1);
        createMenuIfNotExists("我的待办", "my-tasks", "Tickets", 6, workflowMenuId, 1);
        createMenuIfNotExists("我的申请", "my-applications", "Document", 7, workflowMenuId, 1);
    }

    @Transactional
    protected void initRoles() {
        Set<Permission> allPermissions = new HashSet<>();
        permissionRepository.findAll().forEach(allPermissions::add);
        createOrUpdateRole("ADMIN", "系统管理员", "系统超级管理员", allPermissions);

        Set<Permission> managerPermissions = new HashSet<>();
        addPermission(managerPermissions, "USER_VIEW");
        addPermission(managerPermissions, "ROLE_VIEW");
        addPermission(managerPermissions, "MENU_VIEW");
        addPermission(managerPermissions, "DEPARTMENT_VIEW");
        addPermission(managerPermissions, "POSITION_VIEW");
        addPermission(managerPermissions, "PROCESS_VIEW");
        addPermission(managerPermissions, "PROCESS_MANAGE");
        addPermission(managerPermissions, "TASK_VIEW");
        addPermission(managerPermissions, "TASK_HANDLE");
        addPermission(managerPermissions, "DATA_DEPARTMENT");
        createOrUpdateRole("MANAGER", "部门经理", "部门经理角色", managerPermissions);

        Set<Permission> userPermissions = new HashSet<>();
        addPermission(userPermissions, "PROCESS_VIEW");
        addPermission(userPermissions, "TASK_VIEW");
        addPermission(userPermissions, "TASK_HANDLE");
        addPermission(userPermissions, "DATA_SELF");
        createOrUpdateRole("USER", "普通用户", "普通用户角色", userPermissions);
    }

    private void addPermission(Set<Permission> permissions, String code) {
        permissionRepository.findByCode(code).ifPresent(permissions::add);
    }

    @Transactional
    protected void initDefaultUsers() {
        createOrUpdateUser("admin", "系统管理员", "admin@example.com", "13800138000", getRole("ADMIN"));
        createOrUpdateUser("manager", "部门经理", "manager@example.com", "13800138001", getRole("MANAGER"));
        createOrUpdateUser("user", "普通用户", "user@example.com", "13800138002", getRole("USER"));
    }

    private void createPermissionIfNotExists(String code, String name, String description, String type) {
        if (!permissionRepository.existsByCode(code)) {
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name);
            permission.setDescription(description);
            permission.setType(type);
            permission.setStatus(1);
            permissionRepository.save(permission);
            log.info("创建权限: {} ({})", code, type);
        }
    }

    private Long createMenuIfNotExists(String name, String path, String icon, Integer sort, Long parentId, Integer status) {
        Optional<Menu> existing = menuRepository.findByParentIdOrderBySortAsc(parentId).stream()
                .filter(m -> m.getName().equals(name))
                .findFirst();
        
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        
        Menu menu = new Menu();
        menu.setName(name);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setParentId(parentId);
        menu.setStatus(status);
        menuRepository.save(menu);
        log.info("创建菜单: {}", name);
        return menu.getId();
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

    @Transactional
    protected void initButtons() {
        createButtonIfNotExists("agree", "同意", "审批通过按钮", 1, 1, "APPROVE");
        createButtonIfNotExists("reject", "驳回", "审批驳回按钮", 2, 1, "REJECT");
        createButtonIfNotExists("transfer", "转办", "任务转办按钮", 3, 1, "TRANSFER");
        createButtonIfNotExists("delegate", "委托", "任务委托按钮", 4, 1, "DELEGATE");
        createButtonIfNotExists("withdraw", "撤回", "撤回申请按钮", 5, 1, "WITHDRAW");
        createButtonIfNotExists("terminate", "终止", "终止流程按钮", 6, 1, "TERMINATE");
        createButtonIfNotExists("submit", "提交", "提交申请按钮", 7, 1, "SUBMIT");
    }

    private void createButtonIfNotExists(String buttonCode, String buttonName, String description, Integer sort, Integer status, String buttonType) {
        boolean exists = processButtonRepository.findAll().stream()
                .anyMatch(b -> buttonCode.equals(b.getButtonCode()));
        if (exists) {
            return;
        }
        
        ProcessButton button = new ProcessButton();
        button.setButtonCode(buttonCode);
        button.setButtonName(buttonName);
        button.setDescription(description);
        button.setSort(sort);
        button.setStatus(status);
        button.setButtonType(buttonType);
        processButtonRepository.save(button);
        log.info("创建流程按钮: {} ({})", buttonName, buttonCode);
    }
}
