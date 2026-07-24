# SpringDemo 项目完整指南

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [核心模块说明](#核心模块说明)
- [项目执行逻辑](#项目执行逻辑)
- [启动方式](#启动方式)
- [接口清单](#接口清单)
- [配置说明](#配置说明)
- [问题排查记录](#问题排查记录)

---

## 项目概述

SpringDemo 是一个基于 Spring Boot + MyBatis-Plus 的入门级 RESTful API 项目，实现了对 user 表的基础 CRUD 操作，用于学习 Spring Boot 分层架构、MyBatis-Plus ORM 映射及 RESTful 接口开发。

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.0 | 应用框架，内嵌 Tomcat |
| MyBatis-Plus | 3.5.17 | ORM 框架，提供 BaseMapper 通用 CRUD |
| MySQL | 8.0+ | 关系型数据库 |
| Lombok | 最新 | 简化实体类代码（@Data 等） |
| Java | 17+ | 编程语言 |
| Maven | 3.x | 项目构建与依赖管理 |

---

## 项目结构

```
springDemo/
├── docs/                                    # 项目文档目录
│   ├── architecture-analysis.md             # 架构分析文档
│   └── project-guide.md                     # 项目完整指南（本文档）
├── src/
│   └── main/
│       ├── java/com/example/springdemo/
│       │   ├── SpringDemoApplication.java   # 启动类（入口）
│       │   ├── config/
│       │   │   └── MyBatisPlusConfig.java   # MyBatis-Plus 配置类
│       │   ├── controller/
│       │   │   └── UserController.java      # 控制层（API 接口）
│       │   ├── entity/
│       │   │   └── User.java                # 实体类（对应 user 表）
│       │   └── mapper/
│       │       └── UserMapper.java          # 数据访问层（Mapper 接口）
│       └── resources/
│           ├── application.yaml             # 主配置文件
│           ├── application-dev.yaml         # 开发环境配置
│           └── application-prod.yaml        # 生产环境配置
├── pom.xml                                  # Maven 依赖配置
└── README.md
```

---

## 核心模块说明

### 1. 启动类 — SpringDemoApplication.java

**文件路径**：`src/main/java/com/example/springdemo/SpringDemoApplication.java`

**作用**：项目启动入口，通过 `@SpringBootApplication` 注解开启 Spring Boot 自动配置。

**核心代码**：
```java
@SpringBootApplication
public class SpringDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringDemoApplication.class, args);
    }
}
```

**注解说明**：
- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`，是 Spring Boot 的核心注解。

---

### 2. 配置层 — MyBatisPlusConfig.java

**文件路径**：`src/main/java/com/example/springdemo/config/MyBatisPlusConfig.java`

**作用**：MyBatis-Plus 的全局配置类，扫描 Mapper 接口。

**核心代码**：
```java
@Configuration
@MapperScan("com.example.springdemo.mapper")
public class MyBatisPlusConfig {
}
```

**注解说明**：
- `@Configuration`：标记为 Spring 配置类
- `@MapperScan("com.example.springdemo.mapper")`：自动扫描指定包下所有 Mapper 接口，生成代理对象注册到 Spring 容器

---

### 3. 实体层 — User.java

**文件路径**：`src/main/java/com/example/springdemo/entity/User.java`

**作用**：与数据库 user 表一一对应的 Java 实体类，数据在各层之间传递的载体。

**核心代码**：
```java
@Data
@TableName("user")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId
    private Long id;
    private String name;
    private Integer age;
    private String email;
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
```

**注解说明**：
| 注解 | 作用 |
|------|------|
| `@Data` | Lombok 提供，自动生成 getter、setter、toString、equals、hashCode |
| `@TableName("user")` | 指定实体对应数据库表名 |
| `@TableId` | 标记主键字段 |
| `@TableField(value = "create_time")` | 指定字段映射的数据库列名（驼峰转下划线） |
| `@AllArgsConstructor` | 生成全参构造方法 |
| `@NoArgsConstructor` | 生成无参构造方法 |

---

### 4. 数据访问层 — UserMapper.java

**文件路径**：`src/main/java/com/example/springdemo/mapper/UserMapper.java`

**作用**：定义数据库操作接口，继承 BaseMapper 后自动拥有通用 CRUD 能力。

**核心代码**：
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

**继承 BaseMapper 后自动获得的方法**：
| 方法 | 说明 | 对应 SQL |
|------|------|----------|
| `insert(User)` | 新增一条记录 | INSERT INTO user ... |
| `selectById(id)` | 根据主键查询 | SELECT * FROM user WHERE id = ? |
| `selectList(null)` | 查询所有记录 | SELECT * FROM user |
| `updateById(User)` | 根据主键更新 | UPDATE user SET ... WHERE id = ? |
| `deleteById(id)` | 根据主键删除 | DELETE FROM user WHERE id = ? |

---

### 5. 控制层 — UserController.java

**文件路径**：`src/main/java/com/example/springdemo/controller/UserController.java`

**作用**：对外暴露 RESTful API，接收 HTTP 请求，调用 Mapper 操作数据库，返回响应。

**核心代码结构**：
```java
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserMapper userMapper;

    @PostMapping("/user")
    public String save(@RequestBody User user) { ... }

    @GetMapping("/user")
    public List<User> getAll() { ... }

    @GetMapping("/user/{id}")
    public String get(@PathVariable Integer id) { ... }

    @PutMapping("/user/{id}")
    public String update(@PathVariable Long id, @RequestBody User user) { ... }

    @DeleteMapping("/user/{id}")
    public String delete(@PathVariable Long id) { ... }
}
```

**注解说明**：
| 注解 | 作用 |
|------|------|
| `@RestController` | 标记为 REST 控制器，所有方法返回 JSON |
| `@RequestMapping("/user")` | 类级别的 URL 前缀 |
| `@Resource` | 依赖注入，从 Spring 容器中获取 UserMapper 实例 |
| `@RequestBody` | 将请求体 JSON 自动转换为 Java 对象 |
| `@PathVariable` | 将 URL 路径中的变量绑定到方法参数 |

---

## 项目执行逻辑

### 整体调用链路

```
HTTP 请求
    │
    ▼
┌──────────────────┐
│   UserController │  接收请求，参数校验，调用 Mapper
│   (控制层)       │
└────────┬─────────┘
         │ 调用 userMapper.xxx()
         ▼
┌──────────────────┐
│   UserMapper     │  执行 SQL，操作数据库
│   (数据访问层)    │
└────────┬─────────┘
         │ MyBatis-Plus 自动生成 SQL
         ▼
┌──────────────────┐
│   MySQL 数据库    │  user 表
└──────────────────┘
         │
         ▼
    返回结果（JSON）
```

### 完整请求流程（以查询所有用户为例）

1. **客户端**发送 `GET /dev-api/user/user` 请求
2. **Tomcat** 接收请求，转发给 Spring MVC 的 DispatcherServlet
3. **DispatcherServlet** 根据 URL 找到 `UserController.getAll()` 方法
4. **Spring** 自动注入 `userMapper` 对象
5. **Controller** 调用 `userMapper.selectList(new LambdaQueryWrapper<>())`
6. **MyBatis-Plus** 根据 `User` 实体的 `@TableName("user")` 生成 SQL：`SELECT id,name,age,email,create_time,update_time FROM user`
7. **MySQL** 执行 SQL，返回结果集
8. **MyBatis-Plus** 将结果集映射为 `List<User>`
9. **Controller** 返回 List，Spring MVC 自动转为 JSON
10. **客户端** 收到 JSON 响应

### 应用启动流程

```
运行 main() 方法
    → SpringApplication.run() 启动
    → 加载 application.yaml（激活 dev profile）
    → 加载 application-dev.yaml
    → 扫描 @Configuration 配置类（MyBatisPlusConfig）
    → @MapperScan 扫描 mapper 包，创建 UserMapper 代理对象
    → 扫描 @RestController，注册 UserController 的路由映射
    → 启动内嵌 Tomcat（端口 8080）
    → 应用就绪，等待请求
```

---

## 启动方式

### 前置条件

- JDK 17 及以上
- Maven 3.6+
- MySQL 8.0+
- 数据库中存在 `springdemo` 库和 `user` 表

### 方式一：Maven 命令启动

```bash
cd springDemo
mvn spring-boot:run
```

### 方式二：IDEA 启动

1. 打开项目
2. 找到 `SpringDemoApplication.java`
3. 点击 main 方法左侧的绿色三角按钮

### 方式三：打包后运行

```bash
mvn clean package
java -jar target/springDemo-0.0.1-SNAPSHOT.jar
```

### 验证启动成功

启动成功后，控制台会输出：
```
Started SpringDemoApplication in X.XXX seconds
Tomcat started on port 8080 (http) with context path '/dev-api'
```

访问 `http://localhost:8080/dev-api/user/user` 能看到用户列表数据即为成功。

---

## 接口清单

> 所有接口前缀：`http://localhost:8080/dev-api`
> （dev 环境的 context-path 为 `/dev-api`）

### 1. 查询所有用户

| 项 | 值 |
|----|-----|
| 接口 | `GET /user/user` |
| 完整 URL | `http://localhost:8080/dev-api/user/user` |
| 方法 | GET |
| 参数 | 无 |
| 返回 | User 对象数组 |

**响应示例**：
```json
[
  {
    "id": 1,
    "name": "Jone",
    "age": 18,
    "email": "test1@baomidou.com",
    "createTime": "2026-07-20T02:56:39",
    "updateTime": null
  }
]
```

---

### 2. 根据 ID 查询用户

| 项 | 值 |
|----|-----|
| 接口 | `GET /user/user/{id}` |
| 完整 URL | `http://localhost:8080/dev-api/user/user/1` |
| 方法 | GET |
| 路径参数 | `id` - 用户 ID |
| 返回 | 字符串 |

---

### 3. 新增用户

| 项 | 值 |
|----|-----|
| 接口 | `POST /user/user` |
| 完整 URL | `http://localhost:8080/dev-api/user/user` |
| 方法 | POST |
| 请求体 | JSON 对象 |
| 返回 | 字符串 |

**请求体示例**：
```json
{
  "name": "小明",
  "age": 20,
  "email": "xiaoming@example.com"
}
```

---

### 4. 修改用户

| 项 | 值 |
|----|-----|
| 接口 | `PUT /user/user/{id}` |
| 完整 URL | `http://localhost:8080/dev-api/user/user/1` |
| 方法 | PUT |
| 路径参数 | `id` - 用户 ID |
| 请求体 | JSON 对象 |
| 返回 | 字符串 |

---

### 5. 删除用户

| 项 | 值 |
|----|-----|
| 接口 | `DELETE /user/user/{id}` |
| 完整 URL | `http://localhost:8080/dev-api/user/user/1` |
| 方法 | DELETE |
| 路径参数 | `id` - 用户 ID |
| 返回 | 字符串 |

---

## 配置说明

### 主配置 — application.yaml

```yaml
spring:
  profiles:
    active: dev    # 激活 dev 环境配置
```

通过修改 `active` 的值切换环境：`dev`（开发）、`prod`（生产）。

---

### 开发环境 — application-dev.yaml

```yaml
server:
  servlet:
    context-path: /dev-api    # 所有接口前缀
  port: 8080                   # 服务端口

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/springdemo?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**URL 参数说明**：
| 参数 | 作用 |
|------|------|
| `characterEncoding=UTF-8` | 字符编码，防止中文乱码 |
| `serverTimezone=Asia/Shanghai` | 时区设置，MySQL 8.0 必须指定 |
| `useSSL=false` | 关闭 SSL 连接（开发环境） |
| `allowPublicKeyRetrieval=true` | 允许客户端从服务器获取公钥（解决 caching_sha2_password 认证问题） |

---

### 生产环境 — application-prod.yaml

```yaml
server:
  servlet:
    context-path: /prod-api
  port: 9090
```

生产环境使用 9090 端口，接口前缀为 `/prod-api`。

---

## 问题排查记录

### 问题 1：应用启动失败 — sqlSessionFactory 报错

**错误信息**：
```
Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required
```

**原因**：MyBatis-Plus 版本与 Spring Boot 版本不匹配。
- Spring Boot 4.1.0 需要使用 `mybatis-plus-spring-boot4-starter`
- 不能用 `mybatis-plus-spring-boot3-starter`

**解决**：
- MyBatis-Plus 版本升级到 **3.5.17**（支持 Spring Boot 4.x）
- starter 改为 `mybatis-plus-spring-boot4-starter`

---

### 问题 2：404 Not Found

**错误现象**：访问接口返回 404

**原因**：漏掉了 `context-path` 前缀
- 配置文件中 `context-path: /dev-api`
- 所有接口都要加 `/dev-api` 前缀
- 正确 URL：`http://localhost:8080/dev-api/user/user`
- 错误 URL：`http://localhost:8080/user/user`（404）

---

### 问题 3：500 Internal Server Error — URL 格式错误

**错误信息**：
```
Malformed database URL, failed to parse the connection string near 
';serverTimezone=Asia/Shanghai&amp;useSSL=false'
```

**原因**：YAML 文件中 URL 参数用了 `&amp;`（HTML 转义符），YAML 中应该直接写 `&`。

**解决**：将 `&amp;` 替换为 `&`

---

### 问题 4：500 Internal Server Error — 数据库不存在

**错误信息**：
```
Unknown database '20260614-demo'
```

**原因**：配置的数据库名与实际数据库名不一致。

**解决**：修改配置中的数据库名为实际存在的数据库（`springdemo`）。

---

### 问题 5：端口被占用

**错误信息**：
```
Web server failed to start. Port 8080 was already in use.
```

**原因**：之前启动的应用实例未关闭，仍占用 8080 端口。

**解决方法 1**：杀掉占用进程
```bash
# 查找占用 8080 的进程 PID
netstat -ano | findstr :8080
# 杀掉进程（替换 PID）
taskkill /PID <PID> /F
```

**解决方法 2**：修改端口号
在 `application-dev.yaml` 中修改 `port` 为其他端口，如 `8081`。

---

### 问题 6：Ambiguous mapping（路由冲突）

**错误信息**：
```
Ambiguous mapping. Cannot map 'userController' method getAll()
to {GET [/ || ]}: There is already 'userController' bean method get(Integer) mapped.
```

**原因**：Controller 中多个方法映射到了同一个 URL 路径（路径注解被注释掉了）。

**解决**：确保每个方法的 `@GetMapping` / `@PostMapping` 等注解的路径值唯一。

---

### 问题 7：favicon.ico 404

浏览器自动请求网站图标 `/favicon.ico`，项目中没有该文件，返回 404。

**说明**：这是正常现象，完全不影响接口功能，可以忽略。

---

## 数据库初始化

### user 表结构

```sql
CREATE DATABASE IF NOT EXISTS springdemo DEFAULT CHARSET utf8mb4;
USE springdemo;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(30) DEFAULT NULL COMMENT '姓名',
    age INT DEFAULT NULL COMMENT '年龄',
    email VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 测试数据

```sql
INSERT INTO user (id, name, age, email) VALUES
(1, 'Jone', 18, 'test1@baomidou.com'),
(2, 'Jack', 20, 'test2@baomidou.com'),
(3, 'Tom', 28, 'test3@baomidou.com'),
(4, 'Sandy', 21, 'test4@baomidou.com'),
(5, 'Billie', 24, 'test5@baomidou.com');
```
