# Spring Boot 项目四层架构分析

## 一、概述

本项目采用经典的 **MVC 分层架构**，将代码按职责划分为四个层次：实体层（Entity）、数据访问层（Mapper）、配置层（Config）、控制层（Controller）。这种分层设计使得代码职责清晰、易于维护和扩展。

---

## 二、四个文件的区别与职责

### 1. User.java — 实体层（Entity / Model）

**文件路径**：`src/main/java/com/example/springdemo/entity/User.java`

**职责**：描述数据库表结构，是 Java 对象与数据库表之间的映射。

**核心代码**：

```java
@Data
@TableName("user")
public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
```

**关键注解说明**：

| 注解 | 作用 |
|------|------|
| `@Data` | Lombok 注解，自动生成 getter、setter、toString、equals、hashCode 方法 |
| `@TableName("user")` | MyBatis-Plus 注解，指定实体类对应的数据库表名 |

**定位**：数据载体，本身不包含任何业务逻辑，只负责"装数据"。

---

### 2. UserMapper.java — 数据访问层（DAO / Mapper）

**文件路径**：`src/main/java/com/example/springdemo/mapper/UserMapper.java`

**职责**：定义数据库操作接口，提供对 user 表的 CRUD 能力。

**核心代码**：

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

**关键注解说明**：

| 注解 | 作用 |
|------|------|
| `@Mapper` | 告诉 MyBatis 当前接口是数据库操作的代理接口，Spring 启动时自动生成实现类 |
| `extends BaseMapper<User>` | 继承 MyBatis-Plus 提供的基础 Mapper，自动获得 CRUD 方法 |

**继承 BaseMapper 后自动拥有的方法**：

| 方法 | 功能 |
|------|------|
| `insert(User)` | 新增记录 |
| `selectById(id)` | 按 ID 查询 |
| `selectList(null)` | 查询全部 |
| `updateById(User)` | 修改记录 |
| `deleteById(id)` | 删除记录 |

**定位**：数据库操作的入口，无需手写 SQL 即可完成基本操作。

---

### 3. MyBatisPlusConfig.java — 配置层（Config）

**文件路径**：`src/main/java/com/example/springdemo/config/MyBatisPlusConfig.java`

**职责**：Spring 配置类，告诉 Spring 去哪里扫描 Mapper 接口。

**核心代码**：

```java
@Configuration
@MapperScan("com.example.springdemo.mapper")
public class MyBatisPlusConfig {
}
```

**关键注解说明**：

| 注解 | 作用 |
|------|------|
| `@Configuration` | 标记当前类为 Spring 配置类，替代传统 XML 配置 |
| `@MapperScan` | 自动扫描指定包下所有 Mapper 接口，生成代理对象并放入 Spring 容器 |

**定位**：全局配置，本身不写业务代码，只负责"注册和扫描"。

---

### 4. UserController.java — 控制层（Controller / Web API）

**文件路径**：`src/main/java/com/example/springdemo/controller/UserController.java`

**职责**：接收 HTTP 请求，调用业务逻辑，返回响应结果。

**核心代码**：

```java
@RestController
@RequestMapping("/user")
public class UserController {
    @PostMapping("/user")
    public String save(@RequestBody User user) { ... }
    
    @GetMapping("/user")
    public String getAll() { ... }
    
    @GetMapping("/user/{id}")
    public String get(@PathVariable Integer id) { ... }
    
    @PutMapping("/user/{id}")
    public String update(@PathVariable Long id, @RequestBody User user) { ... }
    
    @DeleteMapping("/user/{id}")
    public String delete(@PathVariable Long id) { ... }
}
```

**关键注解说明**：

| 注解 | 作用 |
|------|------|
| `@RestController` | 组合注解 = `@Controller` + `@ResponseBody`，返回 JSON 数据 |
| `@RequestMapping("/user")` | 给类绑定统一访问前缀 |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | 指定 HTTP 请求方法 |
| `@RequestBody` | 读取请求体中的 JSON 数据，自动转换为 Java 对象 |
| `@PathVariable` | 获取 URL 路径中的参数 |

**定位**：对外暴露的 API 入口，是前端与后端交互的桥梁。

---

## 三、四个文件的相互作用

### 调用链路图

```
HTTP 请求                        数据库
    │                              │
    ▼                              ▼
┌──────────────┐  调用   ┌──────────────┐  操作   ┌──────────────┐
│ UserController│ ──────→ │  UserMapper  │ ──────→│  User 实体    │ ──→ user 表
│  (接收请求)    │         │  (数据库操作)  │        │  (数据映射)    │
└──────────────┘         └──────────────┘        └──────────────┘
    ▲                         ▲                         │
    │                         │                         │
    │  HTTP 响应               │  注册为 Bean              │  表名映射
    │                         │                         │
┌──────────────────────────────────────────────────────────────┐
│                  MyBatisPlusConfig (配置层)                    │
│         @MapperScan 扫描 mapper 包，将 UserMapper 注册到 Spring  │
└──────────────────────────────────────────────────────────────┘
```

### 完整流程举例（查询所有用户）

1. **前端发送请求**：`GET /user/user`
2. **UserController 接收请求**：`getAll()` 方法被调用
3. **UserController 调用 UserMapper**：`userMapper.selectList(null)`
4. **UserMapper 根据 User 实体映射**：通过 `@TableName("user")` 知道要查询 `user` 表
5. **MyBatis-Plus 自动生成 SQL**：`SELECT * FROM user`
6. **查询结果映射回 User 对象列表**
7. **UserController 返回响应**：将结果返回给前端

### 应用启动时的初始化流程

1. Spring Boot 启动，扫描 `@Configuration` 注解
2. 发现 `MyBatisPlusConfig`，执行 `@MapperScan("com.example.springdemo.mapper")`
3. 扫描 `mapper` 包下所有接口（包括 `UserMapper`）
4. 为每个 Mapper 接口生成代理实现类，并注入 Spring IOC 容器
5. `UserController` 通过 `@Autowired` 注入 `UserMapper`，即可使用

---

## 四、四层关系总结

| 文件 | 所在层 | 核心注解 | 作用 |
|------|--------|----------|------|
| User.java | Entity 实体层 | `@Data` `@TableName` | 定义数据结构，映射数据库表 |
| UserMapper.java | DAO 数据访问层 | `@Mapper` `extends BaseMapper` | 提供数据库 CRUD 操作 |
| MyBatisPlusConfig.java | Config 配置层 | `@Configuration` `@MapperScan` | 扫描注册 Mapper 到 Spring 容器 |
| UserController.java | Controller 控制层 | `@RestController` `@RequestMapping` | 接收 HTTP 请求，返回响应 |

---

## 五、当前代码存在的问题

**UserController 没有真正调用 UserMapper**，所有接口只返回字符串，没有实际操作数据库：

**当前代码**（错误）：

```java
@GetMapping("/user")
public String getAll() {
    return "查询所有用户成功";  // 只返回字符串，没有查询数据库
}
```

**应该改为**（正确）：

```java
@Autowired
private UserMapper userMapper;

@GetMapping("/user")
public List<User> getAll() {
    return userMapper.selectList(null);  // 真正查询数据库并返回结果
}
```

---

## 六、技术栈说明

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.1.0 | 应用框架，提供自动配置和内嵌服务器 |
| Spring MVC | - | Web 框架，处理 HTTP 请求 |
| MyBatis-Plus | 3.5.9 | ORM 框架，简化数据库操作 |
| MySQL | 8.0.31 | 数据库，存储用户数据 |
| Lombok | - | 代码生成工具，简化实体类代码 |

---

## 七、接口清单

| 接口 | HTTP 方法 | 功能 |
|------|-----------|------|
| `/user/user` | POST | 新增用户 |
| `/user/user` | GET | 查询所有用户 |
| `/user/user/{id}` | GET | 根据 ID 查询用户 |
| `/user/user/{id}` | PUT | 修改用户 |
| `/user/user/{id}` | DELETE | 删除用户 |

---

*文档生成时间：2026-07-22*