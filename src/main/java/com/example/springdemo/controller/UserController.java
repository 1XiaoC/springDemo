package com.example.springdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springdemo.common.Result;
import com.example.springdemo.entity.User;
import com.example.springdemo.mapper.UserMapper;
import com.example.springdemo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")//给类 / 方法绑定统一访问前缀 /user。
public class UserController{

    //通过 @Mapper 或 @MapperScan 交给 Spring 管理，生成代理对象存入 Spring 容器。
    //    @Resource
    //    private UserMapper userMapper;
    @Resource
    private UserService userService;

    //*新增用户
    @Tag(//在文档中把当前控制器下所有接口归类到【用户模块】
            name = "用户模块",
            description = "用户模块接口"
    )
    @Operation(//修饰接口方发法
            summary = "新增用户",
            description = "新增用户"
    )

    @PostMapping("/user")
    public Result save(@RequestBody User user) {
    //        @RequestBody：读取请求体中的 JSON 数据，自动转换成 User 实体类对象；
    //        User 实体需要和 JSON 字段对应，提供对应 getter/setter。
    //        示例请求 JSON：
    //        {
    //            "name":"小明",
    //                "age":20
    //        }
        return Result.success(userService.save( user));
    }

    /*查询所有用户*/
    @GetMapping("/user")//无参数查询
    public Result getAll() {
        return Result.success(userService.list());
        //实现分页查询所有用户，不再一次性查出全部数据，只分页加载，适合数据量大的场景。
        //userMapper.selectPage(new Page<User>(1, 10), new LambdaQueryWrapper<>())
    }
     /* public List getAll() {
       提供一个查询全部用户的后端接口，前端发送 GET 请求就能拿到数据库 user 表里所有用户数据。
       return userMapper.selectList(new LambdaQueryWrapper<>());
    }*/


    /*查询用户*/
    @Parameter(//申明参数
            name = "user",
            description = "用户对象",
            required = true
    )
    @GetMapping("/user/{id}")//有参数查询
    public Result get(@PathVariable Integer id) {
        return Result.success(userService.getById(id))  ;
    }


    //修改用户
    @PutMapping("/user/{id}")//只接收 PUT 请求，REST 规范中 PUT 用于修改更新数据
    public Result update(@PathVariable Long id,@RequestBody User user) {
        //@PathVariable：获取 URL 中的参数 id，@RequestBody 接收请求体 JSON，自动封装为 User 对象
        return Result.success(userService.updateById(user));
    }


    //删除用户
    @DeleteMapping("/user/{id}")
    /**仅接收 DELETE 请求，REST 规范中 DELETE 用于删除资源
     *{id} 是路径变量，代表待删除用户主键
     *访问示例：http://localhost:8080/user/5，5 为要删除的用户 id
     */
//    String 模式：前端发起请求 → Controller 返回文本字符串
//    Result 模式：前端发起请求 → Controller 调用 service 查询用户 → 组装 Result 对象 → 序列化为标准 JSON 返回前端
//    配合之前学习的全局异常处理器，异常也返回Result格式，前后端所有接口返回格式完全一致。
    public Result delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }


    //提供 GET 分页查询接口 ，实现分页 + 按姓名精准条件筛选用户数据。支持前端传递页码、每页条数、姓名筛选条件;
    // 不传参数自动使用默认值;调用 MyBatis-Plus 分页方法查询，最后封装标准JSON 返回给前端。
    @GetMapping("/page")
    public Result findpage(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String name){
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (name != null && !"".equals(name)) {
            lambdaQueryWrapper.eq(User::getName, name);
        }
        return Result.success(userService.page(new Page<>(pageNum, pageSize), lambdaQueryWrapper));
    }
}