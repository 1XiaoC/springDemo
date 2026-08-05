# 代码更新学习笔记（2026-08-06）

> 📚 **学习主题**：Spring AOP（面向切面编程）— 日志切面
>
> 本次更新引入了 Spring 全家桶中非常核心的编程思想：**AOP（Aspect-Oriented Programming，面向切面编程）**。
>
> 🎯 **用一句话理解 AOP**：在**不修改原有业务代码**的前提下，**统一**给某一批方法（比如所有Controller方法）**添加额外功能**，比如打日志、算耗时、验权限、记操作日志、处理事务等。

---

## 一、更新总览

| 文件 | 变化类型 | 核心知识点 |
|------|----------|------------|
| `pom.xml` | 新增依赖 | `spring-boot-starter-aspectj`（Spring Boot AOP 启动器，自动装配切面能力） |
| `aspect/LogAspect.java` | **新文件** | AOP 切面类：`@Aspect` + `@Component` 声明切面；`@Before` 前置通知打印日志；`@Around` 环绕通知包裹执行、打印返回值 |

---

## 二、先搞懂：为什么需要 AOP？（学习前提）

### 场景想象
假设有 10 个 Controller，每个方法都想做下面 3 件事：
```
① 方法开始前，打印一行 "接口XXX开始执行"
② 方法结束后，打印 "接口XXX执行完成，返回值是XXX"
③ 接口耗时超过 3 秒就打印 "接口XXX执行很慢，耗时X秒"（性能告警）
```

#### 不用 AOP：每个方法自己写（坏味道 ❌）

```java
@GetMapping("/user/{id}")
public Result get(@PathVariable Long id) {
    long start = System.currentTimeMillis();
    System.out.println("接口 get 开始执行，参数=" + id);        // 重复代码①

    Result result = Result.success(userService.getById(id));   // 真正的业务

    System.out.println("接口 get 执行完成，返回值=" + result); // 重复代码②
    long cost = System.currentTimeMillis() - start;
    if (cost > 3000) {
        System.out.println("接口执行很慢，耗时=" + cost);      // 重复代码③
    }
    return result;
}
```
💥 **问题**：10 个方法 × 3 段重复 = **30 段模板代码散落在各处**，要改一个规则得改 10 个地方，**违反 DRY 原则（Don't Repeat Yourself）**。

#### 用了 AOP：切面里写一次，所有方法自动生效（优雅 ✅）

```java
@Around("所有Controller方法")
public Object around(ProceedingJoinPoint pjp) {
    // ① 前置：所有方法进来都会走这里（写一次！）
    long start = System.currentTimeMillis();
    System.out.println(pjp.getSignature() + "开始，参数=" + Arrays.toString(pjp.getArgs()));

    Object result = pjp.proceed();   // 调用真正的 Controller 方法

    // ② 后置：所有方法返回前都会走这里（写一次！）
    System.out.println(pjp.getSignature() + "结束，返回=" + result);
    long cost = System.currentTimeMillis() - start;
    if (cost > 3000) System.out.println("慢接口告警！耗时=" + cost);

    return result;
}
```
✅ **效果**：10 个方法**一行不用改**，全部自动被"增强"；后面要加权限校验/操作审计，**只改切面一个地方就够了**。

> 💡 **AOP 是 OOP（面向对象）的补充**：OOP 纵向解决"类与类之间的复用（继承/组合）"；AOP 横向解决"一堆不相关类都要做同一件事"的复用。
>
> 🧩 类比理解：
> ```
> OOP  = 一栋栋房子盖出来（纵向分模块）
> AOP  = 给所有房子统一装"消防喷淋器"（横向一刀切）
>        — 不用拆每家的墙，喷淋器一着火就自动工作
> ```

---

## 三、pom.xml — 引入 AOP 启动器

**文件路径：** [pom.xml#L107-L112](file:///c:/Users/27019/Desktop/测试文件夹/springDemo/pom.xml#L107-L112)

### 3.1 完整代码

```xml
<!--SpringBoot AOP 启动器，提供面向切面编程能力。可以在不修改原有业务代码的前提下
对方法做增强：日志打印、权限校验、接口耗时统计、异常拦截、操作日志记录等。-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aspectj</artifactId>
</dependency>
```

### 3.2 启动器作用拆解

```
引入 spring-boot-starter-aspectj
        │
        ▼
Spring Boot 自动装配（AutoConfiguration）：
  ① 自动注册 AnnotationAwareAspectJAutoProxyCreator（BeanPostProcessor）
     → 这是 AOP 的核心："自动代理创建器"
  ② 自动启动 AspectJ 注解扫描
     → 识别 @Aspect + @Component 标注的类作为"切面类"
  ③ 启动时对匹配切点的 Bean 创建代理对象
     → 用代理模式包装原对象，方法调用会先经过切面
```

> 💡 **面试常问：AOP 的底层是什么？**
> - Bean 是单例对象 → Spring 启动时不会直接给你"原始对象"
> - 而是给你 **CGLIB 动态代理对象**（继承原类的子类）
> - 代理对象里重写了目标方法 → 先执行切面代码 → 再调用父类（真实）方法

---

## 四、LogAspect.java — 日志切面（核心文件）

**文件路径：** [aspect/LogAspect.java](file:///c:/Users/27019/Desktop/测试文件夹/springDemo/src/main/java/com/example/springdemo/aspect/LogAspect.java)

### 4.1 完整代码 + 行内学习注释

```java
package com.example.springdemo.aspect;

import com.example.springdemo.common.Result;
// 环绕通知专用的"连接点"对象：可以拿到方法名、参数、也能proceed()调用原方法
import org.aspectj.lang.ProceedingJoinPoint;
// AOP 5种通知注解：@Before @After @AfterReturning @AfterThrowing @Around
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

// ─── 类级注解：缺一不可！───
// @Component：注册切面类为 Spring Bean（没有这个 Spring 找不到这个类）
// @Aspect   ：告诉 Spring "这个类是个切面类，里面有切点和通知"
@Component
@Aspect
public class LogAspect {

    // ─── 通知1：前置通知 ───
    // @Before(切点表达式) → 在目标方法执行前运行
    @Before("execution(* com.example.springdemo.controller.*.*(..))")
    public void before() {
        System.out.println("开始打印日志");
    }

    // ─── 通知2：环绕通知（最强大！）───
    // @Around(切点表达式) → 可以决定：何时调用原方法、要不要调、甚至改返回值/改异常
    @Around("execution(* com.example.springdemo.controller.*.*(..))")
    public Result around(ProceedingJoinPoint joinPoint) {
        // joinPoint：连接点对象（只有 @Around 是 ProceedingJoinPoint，其他通知是 JoinPoint）
        // 它代表"当前被拦截的那个方法"，可以通过它获得：
        //   .getSignature()  → 方法签名（全限定名）
        //   .getArgs()       → 方法实参数组
        //   .getTarget()     → 真实目标对象
        //   .proceed()       → 调用真实目标方法（必须手动调用！否则不执行）
        System.out.println("开始打印日志");
        try {
            // ① 调用真实的 Controller 方法
            // 返回值被强转成 Result（这里有风险：如果某个方法返回不是 Result 会 ClassCastException）
            Result result = (Result) joinPoint.proceed();

            // ② 真实方法执行完 → 打印返回值（@Data 自动生成的 toString 生效）
            System.out.println(result.toString());
            System.out.println("在方法执行之后");
            return result;

        } catch (Throwable e) {
            // ③ 捕获到异常 → 这里选择"包装后再抛出"
            // ⚠️ 重要经验：不要在这里 return Result.error()！
            //    因为我们之前写了 @RestControllerAdvice 全局异常处理器
            //    如果这里吞了异常 return 错误结果 → 全局异常处理器就"接不到异常"被绕过了
            //    正确做法：继续 throw 出去 → 交回给全局异常处理器统一处理
            throw new RuntimeException(e);
        }
    }
}
```

---

### 4.2 注解详解表（4 个核心注解）

| 注解 | 所属包 | 作用 | 学习记忆点 |
|------|--------|------|------------|
| `@Aspect` | `org.aspectj.lang.annotation` | **标记类为"切面类"** | 告诉 Spring：这不是普通类，是装了"通知+切点"的切面 |
| `@Component` | `org.springframework.stereotype` | **把类注册为 Spring Bean** | 光有 `@Aspect` 不够，Spring 只会扫描 IOC 里的 Bean；两个注解**缺一不可** |
| `@Before("切点")` | 同上 | **前置通知**：在目标方法执行**之前**运行 | 只能执行前干点事，阻止不了执行，也拿不到返回值 |
| `@Around("切点")` | 同上 | **环绕通知（最强）**：在目标方法执行**前后+异常**都能介入 | 唯一**可以调用/跳过**目标方法、**可以修改返回值**、**可以捕获异常**的通知 |

---

### 4.3 AOP 5种通知类型一览（学习扩展）

本次我们用了 `@Before` + `@Around`，完整 AOP 有 5 种通知，全部掌握：

| 通知类型 | 注解 | 执行时机 | 能做什么？（实际场景） |
|----------|------|----------|------------------------|
| 前置通知 | `@Before` | 目标方法执行**前** | 权限校验、参数校验、打印开始日志 |
| 后置通知 | `@After` | 目标方法执行**后（无论成功失败）** | 释放资源、关闭流、清除 ThreadLocal |
| 返回通知 | `@AfterReturning` | 目标方法**正常返回后** | 拿到返回值做二次处理（脱敏、缓存） |
| 异常通知 | `@AfterThrowing` | 目标方法**抛出异常后** | 异常统计、钉钉告警、异常埋点 |
| 环绕通知 | `@Around` | 以上**全部时机包起来** | 万能！接口耗时统计、事务（@Transactional内部就是环绕通知）、加解密、重试 |

> 💡 **学习优先级**：`@Around`（最常用、最强大）→ `@Before` → 其他了解。
>
> ⚠️ **注意**：如果你同时写了 `@Before` + `@Around`，两者会**叠加生效**。

---

### 4.4 切点表达式详解：`execution(* com.example.springdemo.controller.*.*(..))`

这个字符串是 **AspectJ 切点表达式**，AOP 中"告诉 Spring 我要切哪些方法"的匹配规则。下面逐段拆解：

```
execution(* com.example.springdemo.controller.*.*(..))
│         │ └────────────────────────────────┬─────────────────┘
│         │          方法全限定名匹配部分      │
│         │                                  │
│  修饰符+返回值      包名          类名.方法名(参数列表)
│
└── 关键字：execution = "方法执行"切点
            （除了execution 还有 within @annotation 等其他切点指示符）
```

逐段拆解：

| 位置 | 内容 | 含义 |
|------|------|------|
| 关键字 | `execution` | 匹配"方法被执行"这个连接点 |
| 返回值 | `*` | 返回值类型任意（`*` = 通配符，任何类型都行；如果写 `Result` 就只切返回 Result 的方法） |
| 包名 | `com.example.springdemo.controller` | 精确匹配这一个包；如果写 `..controller` 表示任意包层级下的 controller 包 |
| 类名 | `.*` | **该包下的任意类**（UserController / OrderController / …… 都匹配） |
| 方法名 | `.*` | **该类的任意方法**（save/get/delete/findpage …… 都匹配） |
| 参数列表 | `(..)` | **参数任意**（0个或多个，任意类型）；如果写 `()` 只匹配无参方法；写 `(Long)` 只匹配一个 Long 参数 |

#### 常用切点表达式示例

| 表达式 | 含义 |
|--------|------|
| `execution(* com.example..service.*Service.*(..))` | com.example 包及子包下，所有以 Service 结尾的类的所有方法 |
| `execution(public Result com.example.controller.*.*(..))` | controller 包下所有 public 且返回 Result 的方法 |
| `@annotation(org.springframework.web.bind.annotation.PostMapping)` | 所有**加了 @PostMapping 注解的方法**（最精准！不按包名，按注解切） |
| `within(com.example.controller.*)` | 粗粒度匹配，controller 包下所有类的所有方法（不看方法签名） |

---

### 4.5 代码拆解：环绕通知 @Around 执行流程

```
┌─────────────────────────────────────────────────────────────────┐
│   @Around 通知方法内部的执行顺序                                  │
│                                                                 │
│  public Result around(ProceedingJoinPoint joinPoint) {          │
│                                                                 │
│       ┌─── ① 方法进来就执行（相当于@Before） ───┐               │
│       │    System.out.println("开始打印日志");   │               │
│       └──────────────────────────────────────────┘               │
│                                                                 │
│       try {                                                      │
│                                                               │
│          ┌─── ② ★ 最重要：调用真实目标方法 ───┐                 │
│          │  Result result = (Result)            │                 │
│          │              joinPoint.proceed();    │                 │
│          │              不调用 → Controller 永远不执行！          │
│          └────────────────────────────────────────┘              │
│                                                               │
│          ┌─── ③ 目标方法成功返回后执行 ────┐                   │
│          │   System.out.println(result);   │                   │
│          │   System.out.println("之后…");  │                   │
│          │   return result;                │                   │
│          └──────────────────────────────────┘                   │
│                                                               │
│       } catch (Throwable e) {                                   │
│                                                               │
│          ┌─── ④ 目标方法抛异常时执行 ─────┐                   │
│          │  throw new RuntimeException(e); │                   │
│          │  继续抛 → 交回给全局异常处理器    │                   │
│          └──────────────────────────────────┘                   │
│       }                                                          │
│  }                                                                │
└─────────────────────────────────────────────────────────────────┘
```

> ⚠️ **两个"易错点一定要记住"**：
> 1. **`joinPoint.proceed()` 一定要手动调用！** 写环绕通知漏写这句是新手最常见错误 → 所有 Controller 永远不执行，也不报错，接口就是没反应
> 2. **catch 里不要 return Result.error()！** 之前写的 `@RestControllerAdvice` 就失效了 → 统一 throw 出去让全局异常处理器接管

---

### 4.6 ProceedingJoinPoint（连接点）常用 API 速查

| API | 返回 | 作用 | 示例输出 |
|-----|------|------|----------|
| `joinPoint.proceed()` | `Object` | **调用真实目标方法**（@Around 特有） | `Result(code=200,…)` |
| `joinPoint.getSignature()` | `Signature` | 获得方法签名（方法全名） | `Result com.example.springdemo.controller.UserController.get(Long)` |
| `joinPoint.getSignature().getName()` | `String` | 只拿短方法名 | `get` |
| `joinPoint.getArgs()` | `Object[]` | 调用方法时的实参数组 | `[1]`（传进来的 id=1） |
| `joinPoint.getTarget()` | `Object` | 真实的目标对象（原始的 Controller 实例） | `UserController@xxxx` |
| `joinPoint.getThis()` | `Object` | 当前代理对象（CGLIB 生成的子类对象） | `UserController$$EnhancerBySpringCGLIB@xxxx` |

---

## 五、执行逻辑：Spring 启动时 AOP 发生了什么？

```
Spring Boot 应用启动
        │
        ▼
① 扫描所有 @Configuration / @Component
   → 发现 @Component + @Aspect 的 LogAspect → 注册为切面 Bean
   → 发现所有 @Controller / @Service …… → 准备注册普通 Bean
        │
        ▼
② 创建普通 Bean 之前，AOP 自动代理创建器介入
   对每个 Bean 判断："它的方法有没有被切点表达式命中？"
   → 例：UserController 的方法被 `controller.*.*(..)` 命中 ✅
   → 例：UserMapper / UserServiceImpl 也可能被其他切点命中，本次没配就跳过
        │
        ▼
③ 命中 → 创建 CGLIB 动态代理对象（子类）
   代理类继承原类，重写被切的方法：
   ┌───────────────────────────────────────────────┐
   │ 代理类（Spring自动生成）                         │
   │ class UserController$$EnhancerByCGLIB          │
   │       extends UserController {                 │
   │                                                │
   │   // 重写 get 方法（被切点命中）                  │
   │   public Result get(Long id) {                 │
   │       // 1. 先执行切面 @Before 通知             │
   │       // 2. 再进入切面 @Around 通知代码          │
   │       // 3. @Around 里调用 proceed()            │
   │       //    → 触发 super.get(id)（真正的业务）  │
   │       // 4. 业务执行完回到 @Around 继续         │
   │       // 5. 返回结果                            │
   │   }                                            │
   └───────────────────────────────────────────────┘
        │
        ▼
④ IOC 容器里存的是代理对象（不是原始对象！）
   当 @Resource 注入时：UserController userController → 拿到的其实是代理对象
        │
        ▼
✅ 启动完成。后续每次 HTTP 调用 → 命中代理对象 → 先切面 → 后业务
```

---

## 六、调用链路：一次 HTTP 请求穿过 AOP 时发生了什么？（串联之前所有知识点）

**场景**：用户访问 `GET /dev-api/user/user/1`，看看请求如何穿过 AOP。

```
        浏览器
           │ GET /user/user/1
           ▼
┌─────────────────────────────────────────────────────────┐
│  Tomcat / DispatcherServlet                              │
│  ① 根据 URL 找到应该由"userController"这个Bean 处理      │
│     👉 但注意：从 IOC 取出的 userController = 代理对象    │
└───────────────┬─────────────────────────────────────────┘
                ▼
┌─────────────────────────────────────────────────────────┐
│  UserController$$EnhancerByCGLIB（代理对象！）           │
│                                                         │
│  ② 先进入 AOP 拦截链（按注解顺序）                       │
│        │                                                │
│        ├── 🟢 执行 @Before 通知（LogAspect.before()）    │
│        │       → 控制台输出 "开始打印日志"               │
│        │                                                │
│        ├── 🟡 进入 @Around 通知（LogAspect.around()）   │
│        │       → 控制台输出 "开始打印日志"              │
│        │       → 调用 joinPoint.proceed()               │
│        │                 │                              │
│        │                 ▼                              │
│        │       ┌────────────────────────────┐          │
│        │       │ ③ 调用父类（真实）方法：     │          │
│        │       │ super.get(id=1)             │          │
│        │       │ → userService.getById(1)    │          │
│        │       │ → Result.success(user)      │          │
│        │       └────────────┬───────────────┘          │
│        │                    │ 返回 Result(code=200,...) │
│        │                    ▼                          │
│        │       → 控制台输出 result.toString()           │
│        │       → 控制台输出 "在方法执行之后"             │
│        │       → return result 交给 Servlet             │
│        │                                                │
│        └── ④ 没有 @After / @AfterReturning → 不执行     │
│                                                         │
│  ⑤ 返回值 Result → JSON 序列化                          │
└───────────────┬─────────────────────────────────────────┘
                ▼
        浏览器收到：{"code":200,"msg":"操作成功","data":{...}}

───────────────────────────────────────────────────────────────
📺 控制台真实输出顺序（配合上面链路看）：

开始打印日志          ← 来自 @Before
开始打印日志          ← 来自 @Around（前置部分）
Result(code=200, ...) ← 来自 @Around（后置部分，打印返回值）
在方法执行之后        ← 来自 @Around（后置部分）
```

> 💡 **为什么"开始打印日志"打了两遍？**
> 因为我们同时写了 `@Before` + `@Around`（前置部分也打了），两个通知都命中同一切点 → **叠加执行**。
>
> 实际项目中通常选其一即可：
> - 只打印前后日志 → `@Before` + `@AfterReturning`
> - 还要统计耗时 / 改返回值 → `@Around` 一个就够

---

## 七、整段代码 + 注解整体作用总览

```
┌────────────────────────────────────────────────────────────────────┐
│ 本次更新 = 引入 Spring AOP 能力 + 写一个 Controller 日志切面         │
│                                                                    │
│ ┌──────── 模块A：pom.xml 依赖 ─────────────────────────────────┐   │
│ │  spring-boot-starter-aspectj                                  │   │
│ │       = 自动装配 AOP 动态代理能力（CGLIB）                    │   │
│ └──────────────────────────────────────────────────────────────┘   │
│                                                                    │
│ ┌──────── 模块B：LogAspect 切面类 ─────────────────────────────┐   │
│ │                                                                │   │
│ │  @Aspect + @Component   = 声明"我是切面Bean"                  │   │
│ │         │                                                      │   │
│ │  @Before(切点)         = 所有Controller方法执行前打一行日志    │   │
│ │                                                                │   │
│ │  @Around(切点)         = 所有Controller方法前后 + 异常全包裹  │   │
│ │    ├─ ProceedingJoinPoint = 被拦截方法的"手柄"                │   │
│ │    │     → 拿参数 / 拿方法名 / 手动 proceed() 调用业务         │   │
│ │    ├─ 前：打印开始                                            │   │
│ │    ├─ 中：proceed() 执行真正业务                               │   │
│ │    ├─ 后：打印返回值                                          │   │
│ │    └─ 异常：throw 出去交给全局异常处理器                      │   │
│ └──────────────────────────────────────────────────────────────┘   │
│                                                                    │
│  解决的问题：                                                      │
│  ✅ 10 个 Controller 方法里不用再写重复的日志 / 计时代码            │
│  ✅ 后续要加"鉴权 / 慢接口告警 / 操作记录入库"，改切面一个地方即可  │
│  ✅ 不侵入任何业务代码（开闭原则：对扩展开放，对修改关闭）          │
└────────────────────────────────────────────────────────────────────┘
```

---

## 八、典型 AOP 应用场景（学习拓展）

| 场景 | 实现方式 | 用到的通知 |
|------|----------|------------|
| 🔐 接口鉴权 | 切点 `@annotation(RequiresLogin)` → 环绕通知里检查 Header 的 token 是否合法，不合法直接抛 `BusinessException("未登录")`，不 proceed() | @Around |
| ⏱️ 接口耗时统计 + 慢接口告警 | around 前后 `System.currentTimeMillis()` 相减，>3s 就发钉钉 | @Around |
| 📝 操作日志入库 | 切点 `@annotation(OperateLog)` → 拿到方法名、参数、返回值、操作人，存 `sys_operate_log` 表 | @Around / @AfterReturning |
| 💊 接口重试 | around 里 catch 到指定异常，for 循环最多重试 3 次 | @Around |
| 🔒 声明式事务 `@Transactional` | 就是 Spring 内置 AOP 切面实现的！**around 里前：开启事务；中：执行业务；成功后：commit；catch：rollback** | @Around |
| 🔐 结果脱敏 | 返回通知里判断字段，手机号 `138****1234` 脱敏再返回 | @AfterReturning |

> 💡 **你会发现：`@Around` 一个通知就能实现几乎所有 AOP 场景 → 所以是实际项目用得最多的通知类型**。

---

## 九、学习自测题（检验是否掌握）

1. ✍️ AOP 解决什么问题？用一句话描述。
   <details><summary>参考答案</summary>
   在不修改原有业务代码的前提下，给一批方法统一添加额外功能（日志、鉴权、耗时、事务等），解决横切关注点重复代码问题。
   </details>

2. ✍️ 切面类必须加哪两个注解？缺一不可，分别作用？
   <details><summary>参考答案</summary>
   <code>@Aspect</code>（声明为切面）+ <code>@Component</code>（注册为 Spring Bean），Spring 只在 Bean 中找 @Aspect 类。
   </details>

3. ✍️ AOP 5种通知类型是哪 5 个？最强大最常用的是哪一个？
   <details><summary>参考答案</summary>
   @Before 前置、@After 后置、@AfterReturning 返回、@AfterThrowing 异常、@Around 环绕。最强大最常用是 @Around（可以包所有时机、能决定是否执行原方法、能改返回值）。
   </details>

4. ✍️ `joinPoint.proceed()` 是什么？在哪个通知里才能用？**不调用会怎样？**
   <details><summary>参考答案</summary>
   proceed() 是 "调用真正的目标方法"；只有 @Around 的 ProceedingJoinPoint 才有；不调用 → 目标方法永远不执行，接口像卡住一样没有反应。
   </details>

5. ✍️ 切点表达式 `execution(* com.example.springdemo.controller.*.*(..))` 5 段每段什么意思？
   <details><summary>参考答案</summary>
   ① execution = 切点指示符（方法执行）；② * = 返回值任意；③ com.example.springdemo.controller = 包；④ .* = 包下任意类；⑤ .*(..) = 类下任意方法、参数任意。
   </details>

6. ✍️ AOP 里 catch 到异常后，应该 return Result.error() 还是 throw 出去？为什么？
   <details><summary>参考答案</summary>
   应该 throw 出去。如果 return Result.error() 就把异常"吞了"，之前写的 @RestControllerAdvice 全局异常处理器就接不到异常被绕过了；throw 出去才会继续走统一的异常处理流程。
   </details>

7. ✍️ 从 IOC 里拿出来的 UserController 是原始对象吗？如果不是，是什么？
   <details><summary>参考答案</summary>
   不是。Spring 在里面塞的是 CGLIB 动态代理对象（继承自 UserController 的子类），调用方法会先经过切面逻辑 → 才执行真正的父类方法。
   </details>

8. ✍️ 如果在 @Around 里把返回值从 `Result.success(user)` 改成 `Result.error("被AOP改了")`，前端拿到的是哪个？
   <details><summary>参考答案</summary>
   前端拿到的是 Result.error("被AOP改了")。因为环绕通知返回的对象就是代理方法最终返回值，覆盖原方法的结果（这是@Around强大的体现，也是危险点，改之前要知道后果）。
   </details>

---

*更新日期：2026-08-06 · 学习笔记版*
