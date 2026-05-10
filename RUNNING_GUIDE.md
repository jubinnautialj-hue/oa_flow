# OA流程审批系统 - 完整运行指南

## 项目概述

这是一个完整的OA流程审批系统，包含：

- **后端**：Spring Boot 3.2.0 + JDK 17 + Flowable 7.0.0 + Spring Security + JWT
- **前端**：Vue 3 + Vite + Element Plus + Pinia + Axios
- **数据库**：MySQL 8.0

## 项目结构

```
oa_flow/
├── backend/                 # 后端项目
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/oa/
│       │   ├── OaFlowApplication.java
│       │   ├── config/      # 配置类
│       │   ├── controller/  # 控制器
│       │   ├── dto/         # 数据传输对象
│       │   ├── entity/      # 实体类
│       │   ├── repository/  # 数据访问层
│       │   ├── security/    # 安全配置
│       │   └── service/     # 业务逻辑层
│       └── resources/
│           └── application.yml
├── frontend/                # 前端项目
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/          # 路由
│       ├── stores/          # 状态管理
│       ├── utils/           # 工具类
│       ├── layouts/         # 布局
│       └── views/           # 页面
│           ├── Login.vue
│           ├── Dashboard.vue
│           ├── system/      # 系统管理页面
│           └── workflow/    # 工作流页面
├── scripts/
│   └── init_database.sql    # 数据库初始化脚本
└── readme.md
```

## 环境要求

### 必需环境
- **JDK**: 17.0.10 或更高（已验证）
- **Maven**: 3.8.1 或更高（已验证）
- **Node.js**: 16.0 或更高（已验证 v20.19.5）
- **MySQL**: 8.0 或更高
- **npm**: 10.8.2 或更高（已验证）

## 快速开始

### 第一步：配置并启动MySQL

1. 确保MySQL服务已启动
2. 登录MySQL：
   ```bash
   mysql -u root -p
   ```

3. 创建数据库（或者直接执行初始化脚本）：
   ```bash
   mysql -u root -p < scripts/init_database.sql
   ```

4. 默认数据库连接信息（如需修改，编辑 `backend/src/main/resources/application.yml`）：
   - 数据库名：`oa_flow`
   - 用户名：`root`
   - 密码：`root`
   - 主机：`localhost:3306`

### 第二步：启动后端

```bash
cd backend

# 方式一：使用Maven直接运行
mvn spring-boot:run

# 方式二：先打包再运行
mvn clean package -DskipTests
java -jar target/oa-flow-backend-1.0.0.jar
```

后端启动后访问：http://localhost:8080/api

**注意**：
- JPA会自动创建所有业务表
- DataInitializer会自动初始化默认数据（如果表是空的）
- Flowable会自动创建其所需的所有表

### 第三步：启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问：http://localhost:5173

## 默认账号

系统初始化时会自动创建以下账号：

| 用户名  | 密码   | 角色         | 权限范围                     |
|---------|--------|--------------|------------------------------|
| admin   | 123456 | 系统管理员   | 全部权限                     |
| manager | 123456 | 部门经理     | 用户查看、流程管理、任务处理 |
| user    | 123456 | 普通用户     | 流程查看、任务处理           |

## 功能测试流程

### 1. 登录系统
1. 访问 http://localhost:5173
2. 使用 `admin / 123456` 登录

### 2. 部署示例流程
1. 登录后，点击左侧菜单 "流程定义"
2. 在右侧 "流程部署" 区域，点击 "加载示例" 按钮
3. 点击 "部署流程" 按钮
4. 部署成功后，左侧列表会显示 "请假审批流程"

### 3. 发起流程
1. 在流程定义列表中，点击 "发起" 按钮
2. 输入流程标题，点击 "提交"
3. 流程会自动流转到第一个审批节点

### 4. 办理任务
1. 退出当前账号，使用 `manager / 123456` 登录
2. 点击 "我的待办"，可以看到待办任务
3. 点击 "办理"，输入审批意见，选择 "同意"
4. 提交后，任务完成

### 5. 查看历史
1. 点击 "我的申请"，可以查看已发起的流程
2. 点击 "我的待办" → "历史任务" 可以查看已完成的任务

## 功能模块说明

### 系统管理模块

#### 用户管理
- 查看、新增、编辑、删除用户
- 为用户分配角色、部门、岗位

#### 角色管理
- 查看、新增、编辑、删除角色
- 为角色分配权限

#### 权限管理
- 查看、新增、编辑、删除权限项

#### 部门管理
- 查看、新增、编辑、删除部门
- 支持多级部门结构

#### 岗位管理
- 查看、新增、编辑、删除岗位

### 工作流模块

#### 流程定义
- 查看所有已部署的流程定义
- 通过XML部署新流程
- 提供示例流程快速体验

#### 流程设计器
- 可视化配置审批节点
- 生成BPMN 2.0 XML
- 导出XML文件

#### 我的待办
- 查看待办任务
- 签收候选任务
- 办理并审批任务

#### 我的申请
- 查看历史申请记录
- 查看流程状态

## 后端API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/current` - 获取当前用户

### 系统管理接口
- `GET/POST/PUT/DELETE /api/users` - 用户管理
- `GET/POST/PUT/DELETE /api/roles` - 角色管理
- `GET/POST/PUT/DELETE /api/permissions` - 权限管理
- `GET/POST/PUT/DELETE /api/departments` - 部门管理
- `GET/POST/PUT/DELETE /api/positions` - 岗位管理

### 工作流接口
- `GET /api/process/definitions` - 获取流程定义列表
- `GET /api/process/deployments` - 获取部署列表
- `POST /api/process/deploy/xml` - 通过XML部署流程
- `POST /api/process/start` - 启动流程
- `DELETE /api/process/deployments/{id}` - 删除部署

- `GET /api/task/todo` - 获取待办任务
- `GET /api/task/candidate` - 获取待签收任务
- `GET /api/task/{taskId}` - 获取任务详情
- `POST /api/task/complete` - 完成任务
- `POST /api/task/{taskId}/claim` - 签收任务
- `POST /api/task/{taskId}/unclaim` - 取消签收
- `GET /api/task/history/tasks` - 获取历史任务
- `GET /api/task/history/processes` - 获取历史流程

## 常见问题

### 1. 后端启动失败：无法连接数据库
**解决方法**：
- 检查MySQL服务是否启动
- 检查 `application.yml` 中的数据库连接配置
- 确认数据库已创建

### 2. 前端启动失败：依赖安装错误
**解决方法**：
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### 3. 登录失败：用户名或密码错误
**解决方法**：
- 确认使用了正确的账号密码
- 如果数据库表已存在但数据错误，可以删除数据库后重新启动后端，DataInitializer会重新初始化

### 4. 流程部署失败：BPMN XML格式错误
**解决方法**：
- 使用系统提供的示例流程测试
- 检查XML格式是否符合BPMN 2.0规范

## 技术栈详情

### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Security | 6.2.0 | 安全框架 |
| Spring Data JPA | 3.2.0 | ORM框架 |
| Flowable | 7.0.0 | 工作流引擎 |
| JWT (jjwt) | 0.11.5 | Token认证 |
| MySQL Connector | 8.0.33 | 数据库驱动 |
| Lombok | 1.18.30 | 代码简化 |

### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.3.4 | 前端框架 |
| Vue Router | 4.2.5 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.4.4 | UI组件库 |
| Axios | 1.6.2 | HTTP请求 |
| Vite | 5.0.8 | 构建工具 |
| Bpmn-js | 11.5.0 | BPMN编辑器（可选） |

## 端口占用

- 后端：8080
- 前端：5173

如需修改端口：
- 后端：修改 `backend/src/main/resources/application.yml` 中的 `server.port`
- 前端：修改 `frontend/vite.config.js` 中的 `server.port`

## 开发建议

1. **数据库开发**：JPA的 `ddl-auto: update` 会自动更新表结构，生产环境建议改为 `validate` 或 `none`

2. **安全性**：
   - 修改 `jwt.secret` 为更安全的密钥
   - 生产环境使用HTTPS
   - 定期更新密码

3. **性能优化**：
   - 后端可以配置连接池
   - 前端可以使用代码分割和懒加载

## 后续扩展建议

1. 集成邮件通知功能
2. 添加流程流程图展示
3. 实现流程挂起/激活
4. 添加流程版本管理
5. 实现委托/转办功能
6. 添加系统日志审计
7. 集成文件上传/下载
8. 实现移动端适配

## 许可证

本项目仅用于学习和演示目的。

---

**祝您使用愉快！如有问题，请参考本文档或联系开发人员。**
