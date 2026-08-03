package com.example.springdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration//标识当前类为Spring 配置类，项目启动时 Spring 自动扫描并加载这个类中的 Bean
public class SpringDocConfig {
    //用于自动生成后端 RESTful 接口可视化 API 文档。
    //启动项目后访问文档地址，前端 / 测试人员可以在线查看所有接口、参数，还能直接在线调试接口。
    @Bean//把方法返回的OpenAPI对象注册到 Spring 容器，作为全局 Bean
    public OpenAPI restfulOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("后台Swagger的api文档")
                        .description("api文档，用于指定前后端访问api规范，方便测试者对接口进行可视化测试!")
                        .version("v2026")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
                );
    }
}
