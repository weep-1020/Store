package com.weep;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * 登录接口单元测试类
 * 测试用户登录、Token验证、登出等功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;  // 用于存储登录后的 Token
    private static final String BASE_URL = "/api/auth";

    /*
     * 测试 1: 正常登录（管理员账号）
     * 预期结果: 返回 200 状态码和有效的 JWT Token
     */
    @Test
    @Order(1)
    @DisplayName("正常登录 - 管理员账号")
    void testLoginSuccess() throws Exception {
        // 准备测试数据
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "123456");

        // 执行登录请求
        ResultActions result = mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print());  // 打印请求和响应详情

        // 验证响应
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.authorities").isArray());

        // 提取 Token 供后续测试使用
        String responseContent = result.andReturn().getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        authToken = (String) data.get("token");

        System.out.println("✅ 登录成功，Token: " + authToken.substring(0, 20) + "...");
    }

    /*
     * 测试 2: 正常登录（普通用户）
     * 预期结果: 返回 200 状态码和有效的 JWT Token
     */
    @Test
    @Order(2)
    @DisplayName("正常登录 - 普通用户")
    void testLoginUserSuccess() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "user");
        loginRequest.put("password", "123456");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("user"));
    }

    /*
     * 测试 3: 登录失败 - 密码错误
     * 预期结果: 返回 401 状态码
     */
    @Test
    @Order(3)
    @DisplayName("登录失败 - 密码错误")
    void testLoginWrongPassword() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "wrong_password");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    /*
     * 测试 4: 登录失败 - 用户不存在
     * 预期结果: 返回 401 状态码
     */
    @Test
    @Order(4)
    @DisplayName("登录失败 - 用户不存在")
    void testLoginUserNotFound() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistent_user");
        loginRequest.put("password", "123456");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /*
     * 测试 5: 登录失败 - 参数为空
     * 预期结果: 返回 400 状态码
     */
    @Test
    @Order(5)
    @DisplayName("登录失败 - 参数为空")
    void testLoginEmptyParams() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "");
        loginRequest.put("password", "");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名和密码不能为空"));
    }

    /*
     * 测试 6: Token 验证 - 有效 Token
     * 预期结果: 返回 200 状态码，Token 有效
     */
    @Test
    @Order(6)
    @DisplayName("Token 验证 - 有效 Token")
    void testValidateToken() throws Exception {
        // 确保有 Token
        if (authToken == null) {
            loginAndGetToken();
        }

        mockMvc.perform(get(BASE_URL + "/validate")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    /*
     * 测试 7: Token 验证 - 无效 Token
     * 预期结果: 返回 200 状态码，业务代码 401
     */
    @Test
    @Order(7)
    @DisplayName("Token 验证 - 无效 Token")
    void testValidateInvalidToken() throws Exception {
        // /api/auth/validate 现在是公开接口，可以到达 Controller
        mockMvc.perform(get(BASE_URL + "/validate")
                .header("Authorization", "Bearer invalid_token_here"))
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(jsonPath("$.code").value(401)) // 业务代码 401
                .andExpect(jsonPath("$.message").value("Token 无效或已过期"));
    }

    /*
     * 测试 8: Token 验证 - 缺失 Token
     * 预期结果: 返回 200 状态码，业务代码 400
     */
    @Test
    @Order(8)
    @DisplayName("Token 验证 - 缺失 Token")
    void testValidateMissingToken() throws Exception {
        // /api/auth/validate 是公开接口，缺少 Authorization 头会返回 400
        mockMvc.perform(get(BASE_URL + "/validate"))
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(jsonPath("$.code").value(400)) // 业务代码 400
                .andExpect(jsonPath("$.message").value("Token 格式错误或缺失"));
    }

    /*
     * 测试 9: 用户登出
     * 预期结果: 返回 200 状态码，Token 被删除
     */
    @Test
    @Order(9)
    @DisplayName("用户登出")
    void testLogout() throws Exception {
        // 确保有 Token
        if (authToken == null) {
            loginAndGetToken();
        }

        mockMvc.perform(post(BASE_URL + "/logout")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    /*
     * 测试 10: 登出后 Token 失效
     * 预期结果: Token 验证失败
     */
    @Test
    @Order(10)
    @DisplayName("登出后 Token 失效")
    void testTokenInvalidAfterLogout() throws Exception {
        // 先登录获取新 Token
        loginAndGetToken();
        String tempToken = authToken;

        // 登出
        mockMvc.perform(post(BASE_URL + "/logout")
                .header("Authorization", "Bearer " + tempToken))
                .andExpect(status().isOk());

        // 验证 Token 已失效
        mockMvc.perform(get(BASE_URL + "/validate")
                .header("Authorization", "Bearer " + tempToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Token 已被注销或失效"));
    }

    /*
     * 测试 11: SQL 注入尝试
     * 预期结果: 登录失败，系统安全
     */
    @Test
    @Order(11)
    @DisplayName("SQL 注入尝试")
    void testSqlInjection() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "' OR '1'='1");
        loginRequest.put("password", "' OR '1'='1");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /*
     * 测试 12: XSS 攻击尝试
     * 预期结果: 登录失败，系统安全
     */
    @Test
    @Order(12)
    @DisplayName("XSS 攻击尝试")
    void testXssAttack() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "<script>alert('xss')</script>");
        loginRequest.put("password", "123456");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /*
     * 辅助方法: 登录并获取 Token
     */
    private void loginAndGetToken() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "123456");

        String responseContent = mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        authToken = (String) data.get("token");
    }
}
