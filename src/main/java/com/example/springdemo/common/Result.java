package com.example.springdemo.common;

import lombok.Data;
import java.io.Serializable;

//Lombok 提供的注解：
//自动生成当前类所有属性的 getter、setter、toString、equals、hashCode 方法，省去手动编写。
@Data
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;
    // 状态码
    private Integer code;
    // 提示信息message
    private String msg;
    // 返回数据
    private Object data;

    /**
     * 直接返回成功结果
     * @param data 返回数据
     * @return 统一返回对象
     */
    public static Result success(Object data) {
        return success(200, "操作成功", data);
    }

    /**
     * 自定义返回成功结果
     * @param code 状态码
     * @param msg 提示信息
     * @param data 返回数据
     * @return 统一返回结果
     */
    public static Result success(int code, String msg, Object data) {
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    /**
     * 不带结果直接返回成功
     * @return 统一返回结果
     */
    public static Result success() {
        Result r = new Result();
        r.setCode(200);
        r.setMsg("操作成功");
        return r;
    }

    /**
     * 直接返回失败信息
     * @return
     */
    public static Result error() {
        return error(400, "操作失败", null);
    }

    // 带参返回失败信息
    //@param msg 错误信息
    public static Result error(String msg) {
        return error(400, msg, null);
    }

    // 静态失败返回方法
    //自定义返回失败信息
    // @param code 状态码
    // @param msg 错误信息
    // @param data 数据
    public static Result error(int code, String msg, Object data) {
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
