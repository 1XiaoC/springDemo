# 代码更新学习笔记（2026-08-04）

> 📚 **学习主题**：Swagger(OpenAPI3)接口文档 + MyBatis-Plus 主键策略 + 字段自动填充
>
> 本次更新引入了两个非常实用的企业级功能：
> 1. **接口文档自动生成（SpringDoc / OpenAPI 3）**：不再需要手写接口文档，启动项目就能看到可视化页面，还能在线调试
> 2. **字段自动填充（MetaObjectHandler）**：新增用户时自动写入创建时间，修改用户时自动写入更新时间，不需要每次手动 set

---

## 一、更新总览

| 文件 | 变化类型 | 核心知识点 |
|------|----------|------------|
| `pom.xml` | 新增依赖 | `springdoc-openapi-starter-webmvc-ui`（Swagger3/OpenAPI3 启动器） |
| `config/SpringDocConfig.java` | 新增文件 | `@Configuration` + `@Bean` 配置 Swagger 文档标题/描述/版本 |
| `controller/UserController.java` | 加注解 | `@Tag`（接口分组）、`@Operation`（接口说明）、`@Parameter`（参数说明） |
| `entity/User.java` | 注解增强 | `@TableId(type=IdType.AUTO)` 主键自增 + `@TableField(fill=FieldFill.INSERT/UPDATE)` 声明自动填充字段 |
| `handler/MyMetaObjectHandler.java` | 新增文件 | 实现 `MetaObjectHandler` 接口：`insertFill` 写入 createTime、`updateFill` 写入 updateTime |

---

## 二、pom.xml — 引入 Swagger3 (OpenAPI3) 依赖

**文件路径：** [pom.xml#L100-L105](file:///c:/Users/27019/Desktop/测试文件夹/springDemo/pom.xml#L100-L105)

### 2.1 完整代码

```xml
<!-- Swagger OpenAPI3 文档 替代springfox swagger2 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

### 2.2 学习要点：为什么用它？替代谁？

| 对比项 | SpringFox Swagger2（老项目常用） | **SpringDoc OpenAPI3（我们现在用的）** |
|--------|-----------------------------------|------------------------------------------|
| 支持规范 | Swagger 2.0 | **OpenAPI 3.0**（最新标准） |
| Spring Boot 3/4 兼容 | ❌ 不兼容（项目已停更） | ✅ 原生兼容 |
| UI 界面 | swagger-ui.html | **swagger-ui/index.html**（新版UI） |
| 注解包 | `io.swagger.annotations` | **`io.swagger.v3.oas.annotations`** |

> 💡 **记忆口诀**：Spring Boot 3/4 用 SpringDoc，不要再用 SpringFox 了，不兼容会启动报错。

### 2.3 启动后访问地址

启动项目后浏览器直接打开：

| 资源 | URL |
|------|-----|
| Swagger UI 可视化页面（调试用） | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON 原始规范（前端代码生成工具可读取） | `http://localhost:8080/v3/api-docs` |

### 2.4 依赖执行逻辑

```
pom.xml 引入 springdoc-openapi-starter-webmvc-ui
        │
        ▼
Spring Boot 自动装配（AutoConfiguration）启动时：
  ① 扫描所有 @RestController / @Controller 类
  ② 读取 @RequestMapping、@GetMapping 等映射注解
  ③ 读取 @Tag / @Operation / @Parameter 等 Swagger 注解
  ④ 在内存中组装成标准 OpenAPI 3.0 JSON 对象
        │
        ▼
暴露两个接口：
  GET /v3/api-docs         → 返回 JSON 格式文档
  GET /swagger-ui/**       → 返回前端 UI 页面，用户浏览器渲染
```

---

## 三、SpringDocConfig.java — Swagger 文档全局配置

**文件路径：** [config/SpringDocConfig.java](file:///c:/Users/27019/Desktop/测试文件夹/springDemo/src/main/java/com/example/springdemo/config/SpringDocConfig.java)

### 3.1 完整代码

```java
package com.example.springdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI restfulOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("后台Swagger的api文档")
                        .description("api文档，用于指定前后端访问api规范，方便测试者对接口进行可视化测试!")
                        .version("v2026")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
                );
    }
}
```

### 3.2 注解详解

| 注解 | 包 | 作用 | 学习关键点 |
|------|-----|------|------------|
| `@Configuration` | `org.springframework.context.annotation` | 标记类为 **Spring 配置类**，项目启动时自动扫描加载 | 等价于以前的 XML 配置文件；内部可以写多个 `@Bean` 方法 |
| `@Bean` | 同上 | 把方法**返回值对象**注册到 Spring IOC 容器中，作为 Bean 供其他地方注入 | 方法名默认是 Bean name，返回类型是 Bean 的类型 |

### 3.3 代码拆解：Builder 链式写法

```
return new OpenAPI()                           // ① 创建OpenAPI根对象
    .info(                                      // ② 调用 info() 设置文档基本信息（返回this，可继续点）
        new Info()                              // ③ 创建Info对象（描述文档元数据）
            .title("后台Swagger的api文档")      // ④ 标题 → 页面最上方大标题
            .description("...")                 // ⑤ 描述文字 → 标题下方说明
            .version("v2026")                   // ⑥ 版本号 → 每次接口大改+1
            .license(                           // ⑦ 开源协议（可选）
                new License()
                    .name("MIT")
                    .url("...")
            )
    );
```

> 💡 **设计模式**：这种 `.a().b().c()` 一路点的写法叫 **链式编程 / Builder 模式**，每次调用都返回对象自己（this），代码清爽不用写多行 set。

### 3.4 整段代码作用总结

```
┌───────────────────────────────────────────────────────┐
│  SpringDocConfig.java                                  │
│  = 告诉 Swagger UI 页面"这个项目文档长啥样"              │
│                                                       │
│  如果没有这个配置类：Swagger依然能跑，但标题、描述都是   │
│  默认的 "OpenAPI definition" / "Default Definition"   │
│  加了这个类之后：页面顶部显示自定义标题+说明+版本号      │
└───────────────────────────────────────────────────────┘
```

---

## 四、UserController.java — Controller 加 Swagger 注解

**文件路径：** [controller/UserController.java](file:///c:/Users/27019/Desktop/测试文件夹/springDemo/src/main/java/com/example/springdemo/controller/UserController.java)

### 4.1 加注解后的关键代码片段

```java
// 顶部新增3个import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

// ...

// 新增用户 接口上
@Tag(
        name = "用户模块",
        description = "用户模块接口"
)
@Operation(
        summary = "新增用户",
        description = "新增用户"
)
@PostMapping("/user")
public Result save(@RequestBody User user) {
    return Result.success(userService.save(user));
}

// 根据ID查询 接口上
@Parameter(
        name = "user",
        description = "用户对象",
        required = true
)
@GetMapping("/user/{id}")
public Result get(@PathVariable Integer id) {
    return Result.success(userService.getById(id));
}
```

### 4.2 注解详解表

| 注解 | 包 | 作用 | 页面效果 |
|------|-----|------|----------|
| `@Tag` | `io.swagger.v3.oas.annotations.tags` | **接口分组**（模块分类） | 页面左侧菜单中，该 Controller 下所有接口归到 "用户模块" 分组下 |
| `@Operation` | `io.swagger.v3.oas.annotations` | **修饰单个接口方法**：写接口标题和说明 | 每个接口上方显示 summary 作为短标题，点击后展开 description 详细说明 |
| `@Parameter` | 同上 | **声明单个参数**：参数名、描述、是否必填 | "Parameters" 表格中显示参数说明和 Required 列 |

### 4.3 常见参数位置对应关系（学习扩展）

```
@Parameter 应该加在哪里？

┌─────────────┬──────────────────────────┬────────────────────┐
│ 参数来源     │ Controller 写法           │ @Parameter 加哪里? │
├─────────────┼──────────────────────────┼────────────────────┤
│ URL路径变量  │ @PathVariable Long id    │ 加在方法前         │
│ URL查询参数  │ @RequestParam String name│ 加在方法前         │
│ 请求体JSON   │ @RequestBody User user   │ 加在方法前         │
│ 请求头       │ @RequestHeader String token│ 加在方法前       │
└─────────────┴──────────────────────────┴────────────────────┘
```

### 4.4 代码拆解：@Tag 与 @Operation 配合

```
一个完整接口在Swagger页面的显示结构：

┌──────────────────────────────────────────────┐
│  用户模块 ← @Tag(name)                        │
│  ─────────                                    │
│  ▸ 新增用户 ← @Operation(summary)             │
│    说明: 新增用户 ← @Operation(description)   │
│    参数：                                      │
│      Name  | Description | Required           │
│      user  | 用户对象    | true  ← @Parameter │
│    响应示例: Result JSON                      │
└──────────────────────────────────────────────┘
```

### 4.5 Swagger 在线调试调用链路

```
浏览器打开 swagger-ui/index.html
        │
        ▼  页面加载
前端JS请求 GET /v3/api-docs → 拿到完整 JSON
        │
        ▼  渲染
页面显示所有分组、接口、参数（@Tag/@Operation/@Parameter生效）
        │
        ▼  用户点击 "Try it out" → 填参数 → 点击 "Execute"
Swagger UI 内部发起真实 HTTP 请求
    e.g. POST http://localhost:8080/user/user
         Body: {"name":"小明","age":20}
        │
        ▼  请求打到后端
Spring MVC → Controller → Service → Mapper → DB
        │
        ▼  返回 Result JSON
Swagger UI 将响应完整展示（Curl/请求URL/响应码/响应体）
        │
        ✅ 无需Postman，浏览器直接完成接口测试！
```

---

## 五、User.java — 实体类注解增强（主键策略 + 自动填充标记）

**文件路径：** [entity/User.java](file:///c:/Users/27019/Desktop/测试文件夹/springDemo/src/main/java/com/example/springdemo/entity/User.java)

### 5.1 完整代码 + 行内学习注释

```java
package com.example.springdemo.entity;

// 合并通配符导入：3个注解都来自 .annotation 包，用 * 一次性导入
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    // ─── 变化点1：指定主键生成策略为"数据库自增" ───
    // 之前：@TableId                   → 默认策略（跟随全局配置，可能不是自增）
    // 现在：@TableId(type = IdType.AUTO) → 明确使用数据库AUTO_INCREMENT自增
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Integer age;
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    // ─── 变化点2：声明插入时自动填充 ───
    // fill = FieldFill.INSERT 表示：执行 INSERT SQL 时，这个字段由MP自动帮我填值
    // 👉 具体填什么值？见 MyMetaObjectHandler.insertFill()
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    // ─── 变化点3：声明更新时自动填充 ───
    // fill = FieldFill.UPDATE 表示：执行 UPDATE SQL 时，这个字段由MP自动帮我填值
    // 👉 具体填什么值？见 MyMetaObjectHandler.updateFill()
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
```

### 5.2 注解详解

#### ① `@TableId(type = IdType.AUTO)` — 主键生成策略

| 注解元素 | 可选值 | 含义 | 适用场景 |
|----------|--------|------|----------|
| `type` | `IdType.AUTO` | **数据库ID自增**（INSERT SQL 不带 id，靠 MySQL `AUTO_INCREMENT` 生成） | 常用 ✅ 简单，依赖DB |
|  | `IdType.NONE` | 不设置（跟随全局配置） | 默认值 |
|  | `IdType.INPUT` | 用户自己 setId 后插入 | 手动赋值主键 |
|  | `IdType.ASSIGN_ID` | MP自动生成雪花算法 Long（默认） | 分布式系统、分库分表 |
|  | `IdType.ASSIGN_UUID` | MP自动生成 UUID 字符串 | 无顺序主键 |

> 💡 **我们这次选 AUTO 的原因**：数据库表中 `id` 字段已设置 `AUTO_INCREMENT`，保持行为一致。
>
> ❓ **如果不加 type=AUTO 会怎样？**
> 默认策略是跟随全局配置（MP 3.12+ 默认雪花算法），此时 INSERT 会带一个很长的 id 数字插入，而不是数据库自增值 —— 和DB设计不一致，容易混乱。**所以明确写出来最好！**

---

#### ② `@TableField(value = "...", fill = FieldFill.xxx)` — 字段填充声明

| fill 取值 | 含义 | 触发时机 |
|-----------|------|----------|
| `FieldFill.DEFAULT` | 默认不填充 | — |
| `FieldFill.INSERT` | **只在插入时填充** | `INSERT INTO ...` 前 |
| `FieldFill.UPDATE` | **只在更新时填充** | `UPDATE ...` 前 |
| `FieldFill.INSERT_UPDATE` | 插入和更新时都填充 | 两者之前都触发 |

> ⚠️ **关键点**：`@TableField(fill=...)` 只是"**声明**"这个字段要自动填充，就像贴了一张标签。**真正填什么值、怎么填？** 需要我们写一个类实现 `MetaObjectHandler` 接口 —— 也就是下一节的 `MyMetaObjectHandler`！
>
> 🧩 类比理解：
> ```
> @TableField(fill=INSERT)  = 超市商品上贴的"此商品需要称重"标签
> MetaObjectHandler         = 超市的称重员（看到标签就执行对应动作）
> ```

### 5.3 代码拆解：3 个新增注解元素的作用位置

```
        MySQL 表结构
    ┌───────────────────────────┐
    │ id      BIGINT  PK AUTO   │ ←── @TableId(type=AUTO)
    │ name    VARCHAR           │
    │ age     INT               │
    │ email   VARCHAR           │
    │ create_time DATETIME      │ ←── @TableField(fill=INSERT)
    │ update_time DATETIME      │ ←── @TableField(fill=UPDATE)
    └───────────────────────────┘
              ▲
              │ 字段一一对应
    ┌─────────┴──────────┐
    │   User 实体类       │
    │  @TableId(type=     │
    │   IdType.AUTO)      │
    │  id / name / age …  │
    │  @TableField(       │
    │    fill=INSERT)     │     ╔══════════════════════════╗
    │  createTime         │────▶║ 触发时机：INSERT 之前     ║
    │  @TableField(       │     ║ 调用：insertFill() 方法   ║
    │    fill=UPDATE)     │     ╚══════════════════════════╝
    │  updateTime         │────▶╔══════════════════════════╗
    └────────────────────┘     ║ 触发时机：UPDATE 之前     ║
                               ║ 调用：updateFill() 方法   ║
                               ╚══════════════════════════╝
```

---

## 六、MyMetaObjectHandler.java — 自动填充处理器（核心！）

**文件路径：** [handler/MyMetaObjectHandler.java](file:///c:/Users/27019/Desktop/测试文件夹/springDemo/src/main/java/com/example/springdemo/handler/MyMetaObjectHandler.java)

### 6.1 完整代码 + 行内学习注释

```java
package com.example.springdemo.handler;

// MP 提供的自动填充接口：只要实现它，Spring启动时MP自动识别
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
// MyBatis 内部反射用的元对象：通过它可以给实体的任意字段赋值
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j          // Lombok 注解：自动在类中生成一个 static final Logger log 对象
                // 👉 后面直接用 log.info("...") 就能打日志，不用自己 new Logger
@Component      // Spring 注解：把当前类注册到IOC容器
                // 👉 MP 会自动从容器里找所有 MetaObjectHandler Bean 并使用
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * 触发时机：执行 INSERT SQL 前（调用 userService.save() 时 MP 内部回调）
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充..");

        // this.strictInsertFill(元对象, "字段名", 字段类型.class, 要填的值)
        // strict = 严格模式：只在字段值为null时才填，不会覆盖已手动set过的值
        this.strictInsertFill(
                metaObject,
                "createTime",            // 注意：写 Java 属性名（小驼峰），不是数据库列名！
                LocalDateTime.class,      // 要填的字段类型
                LocalDateTime.now()       // 要填的值：当前系统时间
        );
    }

    /**
     * 更新时自动填充
     * 触发时机：执行 UPDATE SQL 前（调用 userService.updateById() 时 MP 内部回调）
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充……");
        this.strictUpdateFill(
                metaObject,
                "updateTime",             // Java 属性名
                LocalDateTime.class,
                LocalDateTime.now()
        );
    }
}
```

### 6.2 注解详解

| 注解 | 包 | 作用 | 学习关键点 |
|------|-----|------|------------|
| `@Slf4j` | `lombok.extern.slf4j` | **自动注入日志对象** | 等价于写 `private static final Logger log = LoggerFactory.getLogger(当前类.class);`<br>Lombok 编译期帮你生成，少写模板代码 |
| `@Component` | `org.springframework.stereotype` | **通用组件注解**：把类实例化注册到 IOC 容器 | 和 `@Service`/`@Controller`/`@Configuration` 本质一样，都是注册 Bean；区别是语义不同：@Service 业务层、@Controller 控制层、@Component 通用组件 |

### 6.3 接口方法详解（MetaObjectHandler）

| 方法 | 触发时机 | 对应 fill 标记 | 我们填了什么 |
|------|----------|----------------|--------------|
| `insertFill(MetaObject metaObject)` | **INSERT 前** | `FieldFill.INSERT` 或 `INSERT_UPDATE` | `createTime = LocalDateTime.now()` |
| `updateFill(MetaObject metaObject)` | **UPDATE 前** | `FieldFill.UPDATE` 或 `INSERT_UPDATE` | `updateTime = LocalDateTime.now()` |

### 6.4 代码拆解：strictInsertFill 参数含义

```
this.strictInsertFill(
    metaObject,        // ① MP 传进来的元对象，里面装着实体的所有字段反射信息
    "createTime",      // ② 要填的【Java 属性名】（驼峰，不是下划线！）
                       //   👉 如果这里写错了"create_time" 会填充失败！
    LocalDateTime.class,// ③ 属性的类型，用于严格匹配，避免类型转换报错
    LocalDateTime.now() // ④ 填充的值：当前时间
);
```

> 💡 **strictInsertFill vs setFieldValByName 区别**
>
> | 方法 | 是否覆盖已有的值 | 场景 |
> |------|-----------------|------|
> | `strictInsertFill` | ❌ **null 才填**，已手动 set 过的不覆盖 | 推荐✅ 符合直觉，保留手动赋值能力 |
> | `setFieldValByName` | ✅ **无脑覆盖**，不管有没有值 | 需要强制覆盖时才用 |

### 6.5 整段代码作用总结

```
┌───────────────────────────────────────────────────────────────┐
│  MyMetaObjectHandler.java = MP 官方的"字段填充钩子"             │
│                                                               │
│  我们只需要：                                                  │
│   1. implements MetaObjectHandler 接口                         │
│   2. 加 @Component 让Spring管理                                │
│   3. 在两个方法里写填充逻辑                                     │
│                                                               │
│  MP 自动帮我们：                                                │
│   • 启动时扫描到这个 Bean，注册到填充器链                       │
│   • 每次执行 INSERT/UPDATE 前，检查实体字段上的 fill 标签       │
│   • 发现有 INSERT 标签 → 回调 insertFill() 帮填 createTime     │
│   • 发现有 UPDATE 标签 → 回调 updateFill() 帮填 updateTime     │
│   • 填充完后才真正拼接 SQL 交给 MySQL                           │
└───────────────────────────────────────────────────────────────┘
```

---

## 七、完整调用链路：新增用户时发生了什么？（串联所有知识点）

```
    前端 / Swagger UI
        │  POST /user/user
        │  Body: { "name": "小明", "age": 20, "email": "xm@qq.com" }
        │  ⚠️  注意：JSON 里没传 id / createTime / updateTime
        ▼
┌───────────────────────────────────────────────────────────┐
│  UserController.save(user)                                 │
│  @Tag @Operation @RequestBody 注解（Swagger扫描时生效）     │
│  → 调用 userService.save(user)                             │
└──────────────┬────────────────────────────────────────────┘
               ▼
┌───────────────────────────────────────────────────────────┐
│  ServiceImpl.save(user)   ← 继承得到的通用 save 方法        │
│  内部：userMapper.insert(user)                             │
└──────────────┬────────────────────────────────────────────┘
               ▼
┌───────────────────────────────────────────────────────────┐
│  MyBatis-Plus 执行 INSERT 前                               │
│                                                           │
│  【步骤1：检查主键 @TableId(type=AUTO)】                    │
│           → user.id == null 且策略是 AUTO → 不拼 id 字段    │
│           → INSERT SQL 里没有 id（交给MySQL AUTO_INCREMENT）│
│                                                           │
│  【步骤2：扫描 @TableField fill 标签】                      │
│           → 发现 createTime 有 fill=INSERT 标记            │
│           → 发现 updateTime 没有 INSERT 标记（是UPDATE）    │
│                                                           │
│  【步骤3：回调 MetaObjectHandler】                         │
│           → 从Spring容器找到 @Component 的 MyMetaObjectHandler│
│           → 调用 insertFill(metaObject)                   │
│               ├─ log.info("开始插入填充..")                │
│               └─ strictInsertFill(createTime → now())     │
│                    → user.createTime = 2026-08-04T10:30:00│
│                                                           │
│  【最终生成的 SQL】                                         │
│    INSERT INTO user                                       │
│    (name, age, email, create_time)                        │
│    VALUES                                                 │
│    ('小明', 20, 'xm@qq.com', '2026-08-04 10:30:00')      │
│                                                           │
│    👉 id 没出现（AUTO），updateTime 没出现（fill=UPDATE）   │
└──────────────┬────────────────────────────────────────────┘
               ▼
          MySQL 执行
               │
               ▼  返回自增ID（MP回填到 user.id）
         Result.success(true) → 返回前端
```

---

## 八、完整调用链路：修改用户时发生了什么？（对比理解）

```
    前端
        │  PUT /user/user/1
        │  Body: { "name": "小明改了", "age": 21 }
        ▼
┌───────────────────────────────────────────────────────────┐
│  UserController.update()                                   │
│  → userService.updateById(user)                            │
└──────────────┬────────────────────────────────────────────┘
               ▼
┌───────────────────────────────────────────────────────────┐
│  MyBatis-Plus 执行 UPDATE 前                               │
│                                                           │
│  【步骤1：检查 fill 标签】                                  │
│           → createTime 是 INSERT，跳过                     │
│           → updateTime 有 fill=UPDATE 标记 ✅              │
│                                                           │
│  【步骤2：回调 MetaObjectHandler.updateFill()】            │
│           → log.info("开始更新填充……")                    │
│           → strictUpdateFill(updateTime → now())          │
│                                                           │
│  【最终生成的 SQL】                                         │
│    UPDATE user                                            │
│    SET name='小明改了', age=21, update_time='2026-08-04 11:00'│
│    WHERE id = 1                                           │
│                                                           │
│    👉 createTime 不变（INSERT only）！                     │
└──────────────┬────────────────────────────────────────────┘
               ▼
          MySQL 执行 → Result.success(true)
```

---

## 九、整段代码 + 注解整体作用总览

```
┌────────────────────────────────────────────────────────────────────┐
│ 本次更新 = 2 个独立的功能模块                                        │
│                                                                    │
│ ┌──────── 模块A：接口文档可视化 ───────────────────────────────┐    │
│ │                                                                │    │
│ │  pom.xml + SpringDocConfig + Controller加注解                  │    │
│ │                                                                │    │
│ │  解决的问题：前后端联调时接口文档手写又慢又容易过期              │    │
│ │  现在的效果：启动项目 → 浏览器访问 swagger-ui → 页面+在线调试   │    │
│ │  关键字：@Configuration @Bean @Tag @Operation @Parameter      │    │
│ └────────────────────────────────────────────────────────────────┘    │
│                                                                    │
│ ┌──────── 模块B：字段自动填充 ────────────────────────────────┐    │
│ │                                                                │    │
│ │  User.java 标记 + MyMetaObjectHandler 执行                    │    │
│ │                                                                │    │
│ │  解决的问题：每次 save() 手动 user.setCreateTime(now()) 很繁琐 │    │
│ │                每次 update() 手动 user.setUpdateTime(now()) 容易漏│    │
│ │  现在的效果：save() 时 createTime 自动写，update() 时 updateTime│    │
│ │               自动写，代码更干净，100% 不会漏                    │    │
│ │  关键字：@TableId(type=AUTO)  @TableField(fill=INSERT/UPDATE) │    │
│ │           MetaObjectHandler  insertFill()  updateFill()       │    │
│ └────────────────────────────────────────────────────────────────┘    │
└────────────────────────────────────────────────────────────────────┘
```

---

## 十、学习自测题（检验是否掌握）

1. ✍️ Spring Boot 4 用哪个依赖引入 Swagger？页面访问地址是？
   <details><summary>参考答案</summary>
   <code>springdoc-openapi-starter-webmvc-ui</code>，访问 <code>/swagger-ui/index.html</code>
   </details>

2. ✍️ `@Configuration` 和 `@Bean` 分别做什么？配合用在哪？
   <details><summary>参考答案</summary>
   @Configuration 标记类为配置类，@Bean 把方法返回值注册成 Spring Bean；用在 SpringDocConfig 里注册 OpenAPI 对象。
   </details>

3. ✍️ `@Tag` / `@Operation` / `@Parameter` 在 Swagger 页面分别对应哪一块？
   <details><summary>参考答案</summary>
   @Tag 分组名称、@Operation 单个接口标题说明、@Parameter 单参数描述。
   </details>

4. ✍️ `@TableId(type = IdType.AUTO)` 作用？如果不加默认是什么策略？
   <details><summary>参考答案</summary>
   明确主键走数据库自增；不加时默认跟随全局（MP新默认是雪花算法 ASSIGN_ID）。
   </details>

5. ✍️ `@TableField(fill = FieldFill.INSERT)` 只是"声明"对吧？真正填值在哪写？
   <details><summary>参考答案</summary>
   是的，只声明；真正填值在实现 <code>MetaObjectHandler</code> 接口的 <code>insertFill()</code> 和 <code>updateFill()</code> 方法里。
   </details>

6. ✍️ `@Component` 为什么要加在 MyMetaObjectHandler 上？不加会怎样？
   <details><summary>参考答案</summary>
   让 Spring 管理这个类（注册成 Bean），MP 启动时才会从容器中找到并使用它；不加的话 MP 根本找不到这个填充器 → fill 标签无效，字段为 null。
   </details>

7. ✍️ strictInsertFill 和 setFieldValByName 的区别？
   <details><summary>参考答案</summary>
   strictInsertFill 只在 null 时填（不覆盖手动值），setFieldValByName 强制覆盖。
   </details>

8. ✍️ 新增用户时 SQL 里 update_time 出现了吗？为什么？
   <details><summary>参考答案</summary>
   没出现。因为它的 fill 是 FieldFill.UPDATE（只在更新时填），INSERT 操作只触发 fill=INSERT 的字段。
   </details>

---

*更新日期：2026-08-04 · 学习笔记版*
