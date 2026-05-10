-- ============================================
-- OA流程审批系统数据库初始化脚本
-- 数据库：oa_flow
-- 字符集：UTF8MB4
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `oa_flow` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `oa_flow`;

-- ============================================
-- 基础业务表（JPA会自动创建，这里提供手动创建的脚本）
-- ============================================

-- 部门表
CREATE TABLE IF NOT EXISTS `sys_department` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '部门名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '部门描述',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '上级部门ID',
  `status` INT(11) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_dept_parent` FOREIGN KEY (`parent_id`) REFERENCES `sys_department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 岗位表
CREATE TABLE IF NOT EXISTS `sys_position` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '岗位名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '岗位描述',
  `status` INT(11) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '权限描述',
  `status` INT(11) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `status` INT(11) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
  `status` INT(11) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `department_id` BIGINT(20) DEFAULT NULL COMMENT '部门ID',
  `position_id` BIGINT(20) DEFAULT NULL COMMENT '岗位ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_position_id` (`position_id`),
  CONSTRAINT `fk_user_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`),
  CONSTRAINT `fk_user_position` FOREIGN KEY (`position_id`) REFERENCES `sys_position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 初始化数据
-- 注意：密码使用 BCrypt 加密，123456 的加密值为 $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
-- ============================================

-- 初始化部门
INSERT INTO `sys_department` (`id`, `name`, `description`, `parent_id`, `status`, `create_time`, `update_time`) VALUES
(1, '总公司', '总公司', NULL, 1, NOW(), NOW()),
(2, '技术部', '技术部', 1, 1, NOW(), NOW()),
(3, '人事部', '人事部', 1, 1, NOW(), NOW()),
(4, '财务部', '财务部', 1, 1, NOW(), NOW());

-- 初始化岗位
INSERT INTO `sys_position` (`id`, `name`, `description`, `status`, `create_time`, `update_time`) VALUES
(1, 'CEO', '首席执行官', 1, NOW(), NOW()),
(2, '部门经理', '部门经理', 1, NOW(), NOW()),
(3, '普通员工', '普通员工', 1, NOW(), NOW());

-- 初始化权限
INSERT INTO `sys_permission` (`id`, `code`, `name`, `description`, `status`, `create_time`, `update_time`) VALUES
(1, 'ADMIN', '系统管理员', '系统超级管理员权限', 1, NOW(), NOW()),
(2, 'USER_VIEW', '用户查看', '查看用户列表', 1, NOW(), NOW()),
(3, 'USER_EDIT', '用户编辑', '新增、编辑、删除用户', 1, NOW(), NOW()),
(4, 'PROCESS_VIEW', '流程查看', '查看流程定义', 1, NOW(), NOW()),
(5, 'PROCESS_MANAGE', '流程管理', '部署、删除流程', 1, NOW(), NOW()),
(6, 'TASK_VIEW', '任务查看', '查看任务列表', 1, NOW(), NOW()),
(7, 'TASK_HANDLE', '任务处理', '办理、签收任务', 1, NOW(), NOW());

-- 初始化角色
INSERT INTO `sys_role` (`id`, `code`, `name`, `description`, `status`, `create_time`, `update_time`) VALUES
(1, 'ADMIN', '系统管理员', '系统超级管理员', 1, NOW(), NOW()),
(2, 'MANAGER', '部门经理', '部门经理角色', 1, NOW(), NOW()),
(3, 'USER', '普通用户', '普通用户角色', 1, NOW(), NOW());

-- 初始化角色权限关联
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
(2, 2), (2, 4), (2, 5), (2, 6), (2, 7),
(3, 4), (3, 6), (3, 7);

-- 初始化用户（密码 123456）
-- BCrypt('123456') = $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `email`, `phone`, `status`, `department_id`, `position_id`, `create_time`, `update_time`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@example.com', '13800138000', 1, 1, 1, NOW(), NOW()),
(2, 'manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '部门经理', 'manager@example.com', '13800138001', 1, 2, 2, NOW(), NOW()),
(3, 'user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '普通用户', 'user@example.com', '13800138002', 1, 2, 3, NOW(), NOW());

-- 初始化用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3);

-- ============================================
-- Flowable 相关表会在应用启动时自动创建
-- ============================================

-- 显示初始化完成信息
SELECT '数据库初始化完成!' AS '消息';
SELECT '默认账号信息：' AS '说明';
SELECT 'admin / 123456 (系统管理员)' AS '账号1';
SELECT 'manager / 123456 (部门经理)' AS '账号2';
SELECT 'user / 123456 (普通用户)' AS '账号3';
