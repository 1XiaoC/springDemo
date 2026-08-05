package com.example.springdemo.aspect;

import com.example.springdemo.common.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LogAspect {

    //前置通知：目标方法执行之前执行当前方法。
    @Before("execution(* com.example.springdemo.controller.*.*(..))")
    public void before(){
        System.out.println("开始打印日志");
    }

    //AOP环绕通知，包裹 Controller 目标方法；可以在方法执行前、执行后、捕获异常做处理。
    @Around("execution(* com.example.springdemo.controller.*.*(..))")
    public Result around(ProceedingJoinPoint joinPoint){
        //ProceedingJoinPoint joinPoint连接点对象，只有@Around才能使用；代表被拦截的目标方法。
        System.out.println("开始打印日志");
        try {
            //joinPoint.proceed()必须调用，否则 controller 逻辑不会执行。
            //返回值类型写死Result，如果某个 controller 返回其他对象，直接抛出ClassCastException。
            //catch 捕获 Throwable，要把异常抛出；不要直接 return 错误 Result，否则绕过全局异常处理器。
            //切面类必须加@Aspect + @Component，aop 依赖必须引入。
            Result result = (Result) joinPoint.proceed();
            System.out.println(result.toString());
            System.out.println("在方法执行之后");
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
