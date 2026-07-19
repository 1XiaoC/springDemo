package com.example.springdemo;

import com.example.springdemo.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class SpringDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDemoApplication.class, args);
    }
//    @GetMapping("/hello")//注解作用：限定当前方法只处理 GET 请求
//    public @ResponseBody String hello() {
//        return "Hello World!";
//        // @ResponseBody 注解作用：返回值作为响应报文返回给调用方
//    }

//    @GetMapping("/hello/{nane}")
//    public String hello(@PathVariable String name ) {
//        //要求：占位符名称 {nane} 和参数名 nane 完全一致才能自动匹配
//        //作用：取出 URL 中 /hello/ 后面的字符串赋值给变量 nane
//        System.out.println(name);
//        return "Hello Spring 8oat";
//    }

    @PostMapping("/user")//限定接口只接收 POST 请求，请求地址为 /user
    public String hello(@RequestBody User user) {
        //@RequestBody：读取请求体（Body）里的 JSON 数据；
        //Spring 自动把 JSON 字符串转为 User 实体对象；
        System.out.println("name" + user.getName());
        System.out.println("age" + user.getAge());
        return "Hello Spring Boot";
    }
}
