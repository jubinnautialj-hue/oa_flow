package com.oa.config;

import com.oa.entity.Permission;
import com.oa.entity.Role;
import com.oa.entity.User;
import com.oa.repository.PermissionRepository;
import com.oa.repository.RoleRepository;
import com.oa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            Permission adminPermission = new Permission();
            adminPermission.setCode("ADMIN");
            adminPermission.setName("系统管理员");
            adminPermission.setDescription("系统管理权限");
            adminPermission.setStatus(1);
            permissionRepository.save(adminPermission);

            Permission userViewPermission = new Permission();
            userViewPermission.setCode("USER_VIEW");
            userViewPermission.setName("用户查看");
            userViewPermission.setStatus(1);
            permissionRepository.save(userViewPermission);

            Permission userEditPermission = new Permission();
            userEditPermission.setCode("USER_EDIT");
            userEditPermission.setName("用户编辑");
            userEditPermission.setStatus(1);
            permissionRepository.save(userEditPermission);

            Permission processViewPermission = new Permission();
            processViewPermission.setCode("PROCESS_VIEW");
            processViewPermission.setName("流程查看");
            processViewPermission.setStatus(1);
            permissionRepository.save(processViewPermission);

            Permission processManagePermission = new Permission();
            processManagePermission.setCode("PROCESS_MANAGE");
            processManagePermission.setName("流程管理");
            processManagePermission.setStatus(1);
            permissionRepository.save(processManagePermission);

            Permission taskViewPermission = new Permission();
            taskViewPermission.setCode("TASK_VIEW");
            taskViewPermission.setName("任务查看");
            taskViewPermission.setStatus(1);
            permissionRepository.save(taskViewPermission);

            Permission taskHandlePermission = new Permission();
            taskHandlePermission.setCode("TASK_HANDLE");
            taskHandlePermission.setName("任务处理");
            taskHandlePermission.setStatus(1);
            permissionRepository.save(taskHandlePermission);

            Role adminRole = new Role();
            adminRole.setCode("ADMIN");
            adminRole.setName("系统管理员");
            adminRole.setDescription("系统超级管理员");
            adminRole.setStatus(1);
            Set<Permission> adminPermissions = new HashSet<>();
            adminPermissions.add(adminPermission);
            adminPermissions.add(userViewPermission);
            adminPermissions.add(userEditPermission);
            adminPermissions.add(processViewPermission);
            adminPermissions.add(processManagePermission);
            adminPermissions.add(taskViewPermission);
            adminPermissions.add(taskHandlePermission);
            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setCode("USER");
            userRole.setName("普通用户");
            userRole.setDescription("普通用户角色");
            userRole.setStatus(1);
            Set<Permission> userPermissions = new HashSet<>();
            userPermissions.add(processViewPermission);
            userPermissions.add(taskViewPermission);
            userPermissions.add(taskHandlePermission);
            userRole.setPermissions(userPermissions);
            roleRepository.save(userRole);

            Role managerRole = new Role();
            managerRole.setCode("MANAGER");
            managerRole.setName("部门经理");
            managerRole.setDescription("部门经理角色");
            managerRole.setStatus(1);
            Set<Permission> managerPermissions = new HashSet<>();
            managerPermissions.add(userViewPermission);
            managerPermissions.add(processViewPermission);
            managerPermissions.add(processManagePermission);
            managerPermissions.add(taskViewPermission);
            managerPermissions.add(taskHandlePermission);
            managerRole.setPermissions(managerPermissions);
            roleRepository.save(managerRole);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setName("系统管理员");
            admin.setEmail("admin@example.com");
            admin.setPhone("13800138000");
            admin.setStatus(1);
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);
            userRepository.save(admin);

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setName("普通用户");
            user.setEmail("user@example.com");
            user.setPhone("13800138001");
            user.setStatus(1);
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setRoles(userRoles);
            userRepository.save(user);

            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("123456"));
            manager.setName("部门经理");
            manager.setEmail("manager@example.com");
            manager.setPhone("13800138002");
            manager.setStatus(1);
            Set<Role> managerRoles = new HashSet<>();
            managerRoles.add(managerRole);
            manager.setRoles(managerRoles);
            userRepository.save(manager);

            System.out.println("========================================");
            System.out.println("初始化数据完成!");
            System.out.println("默认账号:");
            System.out.println("  管理员: admin / 123456");
            System.out.println("  普通用户: user / 123456");
            System.out.println("  部门经理: manager / 123456");
            System.out.println("========================================");
        }
    }
}
