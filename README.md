# SpringDemo

一个用于学习 **Spring Boot** 框架的入门演示项目，涵盖 Spring MVC、MyBatis-Plus、Lombok 等核心技术的整合使用。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.1.0 | 核心框架，内嵌 Tomcat |
| Spring MVC | — | RESTful Web 服务 |
| MyBatis-Plus | 3.5.9 | ORM 持久层框架 |
| MySQL | 8.0 | 关系型数据库 |
| Lombok | — | 简化 Java Bean 代码 |
| Java | 17 | 运行环境 |

## 项目结构

```
springDemo/
├── src/main/java/com/example/springdemo/
│   ├── SpringDemoApplication.java     # 启动类
│   ├── config/
│   │   └── MyBatisPlusConfig.java     # MyBatis-Plus 配置（MapperScan）
│   ├── controller/
│   │   └── UserController.java        # 用户 REST 控制器（CRUD 接口）
│   ├── entity/
│   │   └── User.java                  # 用户实体类
│   └── mapper/
│       └── UserMapper.java            # 用户 Mapper 接口
├── src/main/resources/
│   ├── application.yaml               # 主配置文件（激活 dev 环境）
│   ├── application-dev.yaml           # 开发环境配置
│   └── application-prod.yaml          # 生产环境配置
├── src/test/java/                     # 测试代码
└── pom.xml                            # Maven 项目配置
```

## 功能模块

- **用户 CRUD 接口**：RESTful 风格的增删改查接口（`/user`）
- **多环境配置**：通过 `application-{profile}.yaml` 实现开发/生产环境隔离
- **MyBatis-Plus 集成**：实体映射、分页、自动 CRUD 等能力
- **Spring Boot Actuator**：应用健康检查与监控

## 环境配置

### 开发环境（dev）

- 端口：`8080`
- 上下文路径：`/dev-api`
- 数据库：`jdbc:mysql://127.0.0.1:3306/20260614-demo`

### 生产环境（prod）

- 端口：`9090`
- 上下文路径：`/prod-api`

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+

### 运行项目

```bash
# 克隆项目
git clone https://github.com/1XiaoC/springDemo.git
cd springDemo

# 使用 Maven Wrapper 编译运行
./mvnw spring-boot:run       # Linux / macOS
mvnw.cmd spring-boot:run     # Windows

# 或打包为 JAR 运行
./mvnw package -DskipTests
java -jar target/springDemo-0.0.1-SNAPSHOT.jar
```

### 接口测试

启动后可通过以下接口测试：

```bash
# 新增用户
curl -X POST http://localhost:8080/dev-api/user -H "Content-Type: application/json" -d '{"name":"张三","age":25}'

# 查询所有用户
curl http://localhost:8080/dev-api/user

# 按 ID 查询
curl http://localhost:8080/dev-api/user/1

# 修改用户
curl -X PUT http://localhost:8080/dev-api/user/1 -H "Content-Type: application/json" -d '{"name":"李四","age":30}'

# 删除用户
curl -X DELETE http://localhost:8080/dev-api/user/1
```

## 学习目标

本项目适合 Spring Boot 初学者，通过实践掌握以下知识点：

1. **Spring Boot 基础**：启动类、自动配置、依赖管理
2. **Spring MVC**：`@RestController`、`@RequestMapping`、`@GetMapping` 等注解
3. **MyBatis-Plus**：`BaseMapper`、`@TableName`、`@TableId`、分页查询
4. **多环境配置**：`application-{profile}.yaml` 的 profile 切换
5. **Lombok**：`@Data`、`@AllArgsConstructor`、`@NoArgsConstructor` 简化代码
6. **项目结构规范**：controller / entity / mapper / config 分层架构

## 许可

本项目仅用于学习用途。