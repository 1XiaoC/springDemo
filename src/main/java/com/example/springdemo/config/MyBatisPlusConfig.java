package com.example.springdemo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan("com.example.springdemo.mapper")
//自动扫描指定包下所有 Mapper 接口，自动生成代理对象放入 Spring 容器W
@Configuration
//标记当前类为Spring 配置类，Spring 启动时会解析这个类里的 Bean、扫描配置等，替代传统 xml 配置。
public class MyBatisPlusConfig {
}
