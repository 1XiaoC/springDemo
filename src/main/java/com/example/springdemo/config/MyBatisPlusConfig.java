package com.example.springdemo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@MapperScan("com.example.springdemo.mapper")
//自动扫描指定包下所有 Mapper 接口，自动生成代理对象放入 Spring 容器W
@Configuration
//标记当前类为Spring 配置类，Spring 启动时会解析这个类里的 Bean、扫描配置等，替代传统 xml 配置。
public class MyBatisPlusConfig {
    //配置 MyBatis-Plus 分页插件，让 selectPage 分页方法生效，否则分页代码只会查全表，不会自动 LIMIT 分页。
    @Bean//将当前方法返回的 MybatisPlusInterceptor 对象注册为 Spring 容器 Bean，项目启动自动加载该拦截器。
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        /*如果有多数据源可以不配具体类型，否则都建议配上具体的DbType*/
        return interceptor;
    }
}
