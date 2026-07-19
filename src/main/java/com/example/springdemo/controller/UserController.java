package com.example.springdemo.controller;

import com.example.springdemo.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")//给类 / 方法绑定统一访问前缀 /user。
public class UserController{
    //*新增用户
    @PostMapping("/user")
    public String save(@RequestBody User user) {
//        @RequestBody：读取请求体中的 JSON 数据，自动转换成 User 实体类对象；
//        User 实体需要和 JSON 字段对应，提供对应 getter/setter。
//        示例请求 JSON：
//        {
//            "name":"小明",
//                "age":20
//        }
        return "用户新增成功";
    }
    /*查询所有用户*/
    @GetMapping("/user")//无参数查询
    public String getAll() {
        return "查询所有用户成功";
    }
    /*查询用户*/
    @GetMapping("/user/{id}")//有参数查询
    public String get(@PathVariable Integer id) {
        return "查询用户成功";
    }
    //修改用户
    @PutMapping("/user/{id}")//只接收 PUT 请求，REST 规范中 PUT 用于修改更新数据
    public String update(@PathVariable Long id,@RequestBody User user) {
        //@PathVariable：获取 URL 中的参数 id，@RequestBody 接收请求体 JSON，自动封装为 User 对象
        return "修改用户成功";
    }
    //删除用户
    @DeleteMapping("/user/{id}")
    /**仅接收 DELETE 请求，REST 规范中 DELETE 用于删除资源
     *{id} 是路径变量，代表待删除用户主键
     *访问示例：http://localhost:8080/user/5，5 为要删除的用户 id
     */
    public String delete(@PathVariable Long id) {
        return"删除用户成功";
    }
}