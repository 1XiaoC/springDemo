package com.example.springdemo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data//一键生成全套基础方法
@TableName("user")//前 User 实体对应数据库中 user 表
@AllArgsConstructor//生成全参构造方法
@NoArgsConstructor//生成无参构造方法
public class User {
    //标注实体类主键字段，用来映射数据库主键，定义主键生成策略，替代 @TableField 处理主键
    @TableId
    private Long id;
    private String name; 
    private Integer age;
    private String email;
    //Java 实体规范使用驼峰命名 createTime，数据库规范使用下划线命名 create_time。
    //MyBatis-Plus 默认开启自动驼峰转下划线，大部分时候不加这个注解也能自动匹配；
    @TableField(value ="create_time")
    private LocalDateTime createTime;
    @TableField(value ="updateTime")
    private LocalDateTime updateTime;

}