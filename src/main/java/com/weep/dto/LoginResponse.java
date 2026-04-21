package com.weep.dto;

/**
 * 登录响应数据传输对象
 * <p>
 * 用于向客户端返回登录结果和认证令牌
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
public class LoginResponse {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 认证令牌（JWT Token）
     */
    private String token;

    /**
     * 用户名
     */
    private String username;

    /**
     * 默认构造函数
     */
    public LoginResponse() {
    }

    /**
     * 带参数的构造函数
     *
     * @param code     响应状态码
     * @param message  响应消息
     * @param token    认证令牌
     * @param username 用户名
     */
    public LoginResponse(Integer code, String message, String token, String username) {
        this.code = code;
        this.message = message;
        this.token = token;
        this.username = username;
    }

    /**
     * 创建成功响应
     *
     * @param token    认证令牌
     * @param username 用户名
     * @return 登录响应对象
     */
    public static LoginResponse success(String token, String username) {
        return new LoginResponse(200, "登录成功", token, username);
    }

    /**
     * 创建失败响应
     *
     * @param message 错误消息
     * @return 登录响应对象
     */
    public static LoginResponse fail(String message) {
        return new LoginResponse(401, message, null, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", token='" + (token != null ? "******" : "null") + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
