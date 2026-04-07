# 博客系统后端

基于 Spring Boot 3.x + PostgreSQL 的博客系统后端API。

## 技术栈

- Java 17
- Spring Boot 3.2.5
- Spring Security + JWT
- MyBatis Plus 3.5.5
- PostgreSQL 15+
- Swagger/OpenAPI 3.0

## 项目结构

```
src/main/java/com/jinian/blog/
├── config/           # 配置类
├── security/         # 安全模块
├── controller/       # 控制器
│   └── admin/        # 管理后台API
├── service/          # 服务层
│   └── impl/
├── mapper/           # MyBatis Mapper
├── entity/           # 实体类
├── dto/              # 数据传输对象
│   ├── request/
│   └── response/
├── common/           # 公共模块
│   ├── exception/
│   ├── enums/
│   └── constant/
└── util/             # 工具类
```

## 快速开始

### 1. 环境要求

- JDK 17+
- PostgreSQL 15+
- Maven 3.8+

### 2. 创建数据库

```sql
CREATE DATABASE blog WITH ENCODING='UTF8';
```

### 3. 执行SQL脚本

```bash
# 执行表结构脚本
psql -d blog -f src/main/resources/db/schema.sql

# 执行初始数据脚本
psql -d blog -f src/main/resources/db/data.sql
```

### 4. 修改配置

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blog
    username: your_username
    password: your_password

jwt:
  secret: your-secret-key-must-be-at-least-256-bits-long
```

### 5. 启动项目

```bash
mvn spring-boot:run
```

访问 Swagger 文档: http://localhost:8080/api/swagger-ui.html

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## API端点

### 公开API (无需认证)

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| GET | /api/articles | 文章列表 |
| GET | /api/articles/{id} | 文章详情 |
| GET | /api/categories | 分类列表 |
| GET | /api/tags | 标签列表 |
| GET | /api/messages | 留言列表 |
| GET | /api/wishes | 许愿列表 |
| GET | /api/links | 友链列表 |

### 用户API (需认证)

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | /api/auth/me | 当前用户信息 |
| POST | /api/comments | 发表评论 |
| POST | /api/messages | 发送留言 |
| POST | /api/wishes | 发送许愿 |

### 管理API (需ADMIN角色)

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | /api/admin/users | 用户列表 |
| GET | /api/admin/roles | 角色列表 |
| GET | /api/admin/menus | 菜单管理 |
| GET | /api/admin/articles | 文章管理 |

## 角色权限

| 角色 | 说明 |
|------|------|
| SUPER_ADMIN | 超级管理员，所有权限 |
| ADMIN | 管理员，大部分管理权限 |
| EDITOR | 编辑，文章管理权限 |
| USER | 普通用户，互动权限 |
| GUEST | 访客，仅查看 |

## 数据库表

- `users` - 用户表
- `roles` - 角色表
- `permissions` - 权限表
- `menus` - 菜单表
- `articles` - 文章表
- `categories` - 分类表
- `tags` - 标签表
- `comments` - 评论表
- `messages` - 留言墙表
- `wishes` - 许愿树表
- `links` - 友链表
- `settings` - 系统配置表

## License

MIT