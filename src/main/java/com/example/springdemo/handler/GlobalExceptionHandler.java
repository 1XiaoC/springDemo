package com.example.springdemo.handler;

import com.example.springdemo.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//Spring MVC 全局异常处理注解，等价于组合@ControllerAdvice + @ResponseBody
@RestControllerAdvice
public class GlobalExceptionHandler {

    //标记当前方法是异常处理方法
    @ExceptionHandler(RuntimeException.class)//判断抛出的异常是否为RuntimeException或它的子类；
    public Result handlerException(RuntimeException e) {
        //调用 Result 类静态失败方法，封装错误响应：内部自动设置错误状态码、异常提示信息，无返回数据，最终以 JSON 传给前端。
        return Result.error(e.getMessage());//获取异常里自定义的提示文本
    }
}
