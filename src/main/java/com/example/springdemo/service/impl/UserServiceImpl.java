package com.example.springdemo.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.springdemo.entity.User;
import com.example.springdemo.mapper.UserMapper;
import com.example.springdemo.service.UserService;
import org.springframework.stereotype.Service;

//Spring 注解，将当前类注册为业务层 Bean，存入 Spring 容器；Controller 可以使用@Resource/@Autowired注入UserService
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
