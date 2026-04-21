package com.weep.dto;

/**
 * 登录请求数据传输对象
 * <p>
 * 用于接收客户端发送的登录请求参数
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
public class LoginRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 默认构造函数
     */
    public LoginRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param username 用户名
     * @param password 密码
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='******'" +
                '}';
    }
}
