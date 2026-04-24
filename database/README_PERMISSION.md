# 权限控制完善 - 数据库初始化指南

## 📋 概述

本次更新完善了项目的权限控制系统，实现了基于角色的访问控制（RBAC）。需要执行数据库脚本来创建必要的表和初始数据。

## 🗄️ 数据库脚本

数据库脚本已存在于 `database/schema.sql` 文件中，包含以下内容：

### 1. 表结构
- **users** - 用户表
- **roles** - 角色表
- **permissions** - 权限表
- **user_roles** - 用户角色关联表
- **role_permissions** - 角色权限关联表

### 2. 初始数据

#### 角色数据
| ID | 角色名称 | 角色代码 | 描述 |
|----|---------|---------|------|
| 1 | 超级管理员 | SUPER_ADMIN | 所有权限 |
| 2 | 商家 | MERCHANT | 商品和订单管理 |
| 3 | 普通用户 | USER | 浏览和购买 |

#### 权限数据
| ID | 权限名称 | 权限代码 | 资源类型 |
|----|---------|---------|---------|
| 1 | 查看商品 | product:view | api |
| 2 | 管理商品 | product:manage | api |
| 3 | 查看订单 | order:view | api |
| 4 | 管理订单 | order:manage | api |
| 5 | 用户管理 | user:manage | api |

#### 测试用户
| 用户名 | 密码 | 角色 |
|-------|------|------|
| admin | 123456 | SUPER_ADMIN |
| merchant | 123456 | MERCHANT |
| user | 123456 | USER |

## 🚀 执行步骤

### 方法一：使用 MySQL 命令行

```bash
# 连接到 MySQL
mysql -u root -p

# 执行脚本
source D:/JAVA2025/Store/database/schema.sql
```

### 方法二：使用 MySQL Workbench

1. 打开 MySQL Workbench
2. 连接到数据库服务器
3. 打开 `database/schema.sql` 文件
4. 点击执行按钮（⚡）

### 方法三：使用 PowerShell

```powershell
# 在 PowerShell 中执行
Get-Content "D:\JAVA2025\Store\database\schema.sql" | mysql -u root -p123456
```

## ✅ 验证安装

执行以下 SQL 查询验证数据是否正确导入：

```sql
-- 检查角色
SELECT * FROM roles;

-- 检查权限
SELECT * FROM permissions;

-- 检查用户及其角色
SELECT u.username, r.role_name, r.role_code
FROM users u
INNER JOIN user_roles ur ON u.id = ur.user_id
INNER JOIN roles r ON ur.role_id = r.id;

-- 检查角色及其权限
SELECT r.role_name, p.permission_name, p.permission_code
FROM roles r
INNER JOIN role_permissions rp ON r.id = rp.role_id
INNER JOIN permissions p ON rp.permission_id = p.id
ORDER BY r.role_name;
```

## 🔐 权限使用说明

### 1. 登录获取 Token

```bash
POST http://localhost:8089/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

响应示例：
```json
{
  "code": 200,
  "message": "登录成功",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}
```

### 2. 使用 Token 访问受保护接口

```bash
GET http://localhost:8089/api/user/list
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. 不同角色的访问权限

#### SUPER_ADMIN（admin）
- ✅ 可以访问所有接口
- ✅ 拥有所有权限

#### MERCHANT（merchant）
- ✅ 可以访问 `/api/user/{id}`
- ❌ 不能访问 `/api/user/list`（需要 SUPER_ADMIN 角色或 user:manage 权限）

#### USER（user）
- ✅ 可以访问 `/api/user/info`
- ❌ 不能访问 `/api/user/list` 和 `/api/user/{id}`

## 📝 添加新的权限控制

### 在 Controller 方法上使用注解

```java
// 需要特定角色
@RequireRole({"SUPER_ADMIN"})
@GetMapping("/admin/users")
public List<User> getAllUsers() { ... }

// 需要特定权限
@RequirePermission({"product:manage"})
@PostMapping("/products")
public Product createProduct(@RequestBody Product product) { ... }

// 多个条件（OR 逻辑）
@RequireRole(value = {"SUPER_ADMIN", "MERCHANT"}, logical = RequireRole.Logical.OR)
@GetMapping("/orders")
public List<Order> getOrders() { ... }
```

## 🔧 技术架构

### 核心组件

1. **JwtUtil** - JWT 工具类，生成包含角色和权限的 Token
2. **AuthenticationFilter** - 认证过滤器，验证 Token 有效性
3. **PermissionInterceptor** - 权限拦截器，检查角色和权限
4. **@RequireRole** - 角色验证注解
5. **@RequirePermission** - 权限验证注解

### 工作流程

```
请求 → AuthenticationFilter (验证Token) 
     → PermissionInterceptor (检查角色/权限) 
     → Controller (业务逻辑)
```

## ⚠️ 注意事项

1. **首次运行前必须执行数据库脚本**
2. 确保 MySQL 服务正在运行
3. 检查 `application.yml` 中的数据库配置是否正确
4. Token 中包含角色和权限信息，有效期为 24 小时
5. 登出时客户端需要删除本地存储的 Token

## 🎯 下一步建议

1. 根据业务需求添加更多角色和权限
2. 实现动态权限管理界面
3. 添加 Token 黑名单机制（用于登出）
4. 实现权限缓存以提高性能
5. 添加审计日志记录权限访问

---

**完成时间**: 2026-04-24  
**版本**: v3.0  
**作者**: Store Team
