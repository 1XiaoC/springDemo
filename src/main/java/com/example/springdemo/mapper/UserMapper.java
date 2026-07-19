package com.example.springdemo.mapper;

import com.example.springdemo.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
//加在 Mapper 接口 上，告诉 MyBatis：
//当前接口是数据库操作的 Mapper 代理接口，Spring 启动时自动生成该接口的实现类，注入 IOC 容器，业务层可以直接 @Autowired 注入使用。
public interface UserMapper extends BaseMapper<User>{
}
