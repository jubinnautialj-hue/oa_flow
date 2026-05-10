# OA流程审批系统运行指南

## 项目结构

```
oa_flow/
├── backend/           # Spring Boot 后端
├── frontend/          # Vue 前端
├── scripts/           # 数据库脚本
└── readme.md
```

## 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

## 快速开始

### 1. 初始化数据库

方式一：手动执行脚本
```bash
mysql -u root -p < scripts/init_database.sql
```

方式二：直接启动后端，JPA 会自动创建表，DataInitializer 会自动初始化数据

数据库配置：
- 数据库名：oa_flow
- 用户名：root
- 密码：root

如需修改，请编辑 backend/src/main/resources/application.yml

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

或者打包运行：
```bash
cd backend
mvn clean package
java -jar target/oa-flow-backend-1.0.0.jar
```

后端启动后访问：http://localhost:8080/api

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问：http://localhost:5173

## 默认账号

| 用户名 | 密码   | 角色         |
|--------|--------|--------------|
| admin  | 123456 | 系统管理员   |
| manager| 123456 | 部门经理     |
| user   | 123456 | 普通用户     |

## 功能模块

### 1. 系统管理
- 用户管理：增删改查用户
- 角色管理：增删改查角色，分配权限
- 权限管理：增删改查权限
- 部门管理：增删改查部门
- 岗位管理：增删改查岗位

### 2. 工作流管理
- 流程定义：查看已部署的流程定义
- 流程设计器：可视化设计流程，生成 BPMN XML
- 流程部署：通过 XML 部署流程
- 发起流程：启动流程实例

### 3. 任务管理
- 我的待办：查看并办理待办任务
- 待签收任务：签收候选任务
- 任务详情：查看任务详情，办理审批
- 我的申请：查看历史申请记录

## 测试流程

1. 登录系统（admin/123456）
2. 进入"流程定义"页面
3. 点击"加载示例"按钮加载示例流程
4. 点击"部署流程"按钮部署流程
5. 回到流程定义列表，找到刚部署的流程
6. 点击"发起"按钮启动流程
7. 使用 manager 账号登录，查看待办任务
8. 办理任务，完成审批

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- Flowable 7.0.0
- MySQL 8.0

### 前端
- Vue 3
- Vue Router 4
- Pinia
- Element Plus
- Axios
- Vite
