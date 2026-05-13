package com.weep.util;

/**
 * 统一响应结果封装类
 *
 * <p>用于封装 API 响应数据，包含状态码、消息和数据三部分。
 * 该类由 Spring MVC 的 @ResponseBody 自动序列化为 JSON 格式返回给前端。
 *
 * <p>响应格式说明：
 * <ul>
 *   <li>code: 状态码，0 表示成功，非 0 表示失败</li>
 *   <li>msg: 响应消息，描述操作结果</li>
 *   <li>data: 响应数据，可为任意类型的对象</li>
 * </ul>
 */
public class XJson {
    
    /** 响应状态码 */
    private int code;
    
    /** 响应消息 */
    private String msg;
    
    /** 响应数据 */
    private Object data;

    /**
     * 无参构造函数
     */
    public XJson() {
    }

    /**
     * 全参构造函数
     *
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     */
    public XJson(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置状态码
     *
     * @param code 状态码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置响应消息
     *
     * @param msg 响应消息
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 对象字符串表示
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "XJson{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}