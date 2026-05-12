-- ============================================
-- OA流程审批系统数据库更新脚本
-- 新增功能：按钮管理、部署管理、运维调度
-- ============================================

USE `oa_flow`;

-- 流程按钮表
CREATE TABLE IF NOT EXISTS `sys_process_button` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `button_code` VARCHAR(100) NOT NULL COMMENT '按钮编码',
  `button_name` VARCHAR(100) NOT NULL COMMENT '按钮名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` INT(11) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `process_definition_key` VARCHAR(100) DEFAULT NULL COMMENT '流程定义Key，为空表示全局',
  `task_definition_key` VARCHAR(100) DEFAULT NULL COMMENT '任务节点Key，为空表示所有节点',
  `button_type` VARCHAR(50) DEFAULT NULL COMMENT '按钮类型：APPROVE/REJECT/TRANSFER/DELEGATE/WITHDRAW/TERMINATE/SUBMIT/CUSTOM',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程按钮表';

-- 插入默认按钮数据
INSERT IGNORE INTO `sys_process_button` (`button_code`, `button_name`, `description`, `sort`, `status`, `button_type`, `create_time`, `update_time`) VALUES
('agree', '同意', '审批通过按钮', 1, 1, 'APPROVE', NOW(), NOW()),
('reject', '驳回', '审批驳回按钮', 2, 1, 'REJECT', NOW(), NOW()),
('transfer', '转办', '任务转办按钮', 3, 1, 'TRANSFER', NOW(), NOW()),
('delegate', '委托', '任务委托按钮', 4, 1, 'DELEGATE', NOW(), NOW()),
('withdraw', '撤回', '撤回申请按钮', 5, 1, 'WITHDRAW', NOW(), NOW()),
('terminate', '终止', '终止流程按钮', 6, 1, 'TERMINATE', NOW(), NOW()),
('submit', '提交', '提交申请按钮', 7, 1, 'SUBMIT', NOW(), NOW());

-- 更新菜单：添加按钮管理、部署管理、运维调度菜单
-- 注意：由于应用启动时DataInitializer会自动创建这些菜单，如果菜单已存在则不需要执行
-- 以下仅为手动添加的参考SQL

-- 显示更新完成信息
SELECT '数据库更新完成!' AS '消息';
SELECT '新增功能说明：' AS '说明';
SELECT '1. 按钮管理：系统管理 -> 按钮管理' AS '功能1';
SELECT '2. 部署管理：工作流 -> 部署管理' AS '功能2';
SELECT '3. 运维调度：工作流 -> 运维调度' AS '功能3';
