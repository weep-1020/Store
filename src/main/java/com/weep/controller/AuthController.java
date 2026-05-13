package com.weep.controller;

import com.weep.entity.LargeFile;
import com.weep.entity.User;
import com.weep.response.Result;
import com.weep.service.AuthService;
import com.weep.service.LargeFileService;
import com.weep.service.UserService;
import com.weep.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final int MAX_LOGIN_ATTEMPTS = 10;

    private static final long RATE_LIMIT_WINDOW_MS = 60_000;

    private final ConcurrentHashMap<String, long[]> loginAttempts = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Long> loginBlocked = new ConcurrentHashMap<>();

    private final AuthService authService;

    private final JwtUtil jwtUtils;

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final LargeFileService largeFileService;

    public AuthController(AuthService authService, JwtUtil jwtUtils,
                          RedisTemplate<String, Object> redisTemplate,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          LargeFileService largeFileService) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.largeFileService = largeFileService;
    }

    /*
     * 用户登录接口
     * 支持手机号或用户名 + 密码登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest,
                                             HttpServletRequest request) {
        String phone = loginRequest.get("phone");
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String clientIp = request.getRemoteAddr();

        // identifier: 优先使用 phone，否则使用 username
        String identifier = (phone != null && !phone.isBlank()) ? phone : username;

        if ((phone == null || phone.isBlank()) && (username == null || username.isBlank())) {
            return Result.error(400, "用户名和密码不能为空");
        }
        if (password == null || password.isBlank()) {
            return Result.error(400, "用户名和密码不能为空");
        }

        if (isRateLimited(clientIp)) {
            logger.warn("登录限流触发 - IP: {}, identifier: {}", clientIp, identifier);
            return Result.error(429, "登录请求过于频繁，请稍后再试");
        }

        Map<String, Object> loginResult = authService.loginWithUserInfo(identifier, password);

        if (loginResult == null) {
            recordFailedAttempt(clientIp);
            logger.warn("登录失败：用户名或密码错误 - IP: {}, identifier: {}", clientIp, identifier);
            return Result.error(401, "用户名或密码错误");
        }

        clearLoginAttempts(clientIp);
        logger.info("用户登录成功 - IP: {}, identifier: {}", clientIp, identifier);

        // 组装返回数据（兼容嵌套和扁平两种格式）
        Map<String, Object> data = new HashMap<>();
        data.put("token", loginResult.get("token"));
        data.put("username", loginResult.get("username"));
        data.put("authorities", loginResult.get("authorities"));
        data.put("userInfo", loginResult.get("userInfo"));

        return Result.success("登录成功", data);
    }

    /*
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> regRequest) {
        String phone = regRequest.get("phone");
        String password = regRequest.get("password");
        String username = regRequest.get("username");
        String email = regRequest.get("email");

        if (phone == null || phone.isBlank() || password == null || password.isBlank()) {
            return Result.error(400, "手机号和密码不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error(400, "手机号格式不正确");
        }
        if (password.length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }
        if (userService.findByPhone(phone) != null) {
            return Result.error(400, "该手机号已被注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        user.setUsername(username != null && !username.isBlank() ? username : phone);
        user.setEmail(email);
        user.setStatus(1);

        boolean success = userService.register(user);
        if (success) {
            logger.info("用户注册成功 - 手机号: {}", phone);
            return Result.success("注册成功", null);
        } else {
            return Result.error(400, "注册失败，请重试");
        }
    }

    /*
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未认证");
        }

        String phone = authentication.getName();
        User user = userService.findByPhone(phone);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("pic", user.getPic());
        data.put("status", user.getStatus());
        data.put("createdAt", user.getCreatedAt());

        return Result.success("查询成功", data);
    }

    /*
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> profileRequest,
                                      Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未认证");
        }

        String phone = authentication.getName();
        User user = userService.findByPhone(phone);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        String username = profileRequest.get("username");
        String email = profileRequest.get("email");
        String pic = profileRequest.get("pic");

        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (pic != null && !pic.isBlank()) {
            LargeFile image = largeFileService.findByPic(pic);
            if (image == null) {
                return Result.error(400, "头像图片不存在");
            }
            user.setPic(pic);
        }

        boolean success = userService.update(user);
        if (success) {
            logger.info("用户信息更新成功 - 手机号: {}", phone);
            return Result.success("更新成功", null);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    /*
     * 修改密码（直接验证旧密码，不生成无用JWT）
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> pwdRequest,
                                       Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未认证");
        }

        String oldPassword = pwdRequest.get("oldPassword");
        String newPassword = pwdRequest.get("newPassword");

        if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return Result.error(400, "新旧密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error(400, "新密码长度不能少于6位");
        }

        String phone = authentication.getName();
        User user = userService.findByPhone(phone);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 直接使用 PasswordEncoder 验证旧密码，避免生成无用 JWT
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error(400, "原密码错误");
        }

        boolean success = userService.changePassword(user.getId(), newPassword);
        if (success) {
            logger.info("密码修改成功 - 手机号: {}", phone);
            return Result.success("密码修改成功", null);
        } else {
            return Result.error(500, "密码修改失败");
        }
    }

    /*
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            logger.warn("登出失败：未认证用户");
            return Result.error(401, "未认证");
        }

        String phone = authentication.getName();
        boolean success = authService.logout(phone);

        if (success) {
            logger.info("用户登出成功 - 手机号: {}", phone);
            return Result.success("登出成功", null);
        } else {
            logger.error("用户登出失败，Redis 删除失败 - 手机号: {}", phone);
            return Result.error(500, "登出失败，请重试");
        }
    }

    /*
     * Token 验证接口
     */
    @GetMapping("/validate")
    public Result<Map<String, Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(400, "Token 格式错误或缺失");
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtils.validateToken(token)) {
                return Result.error(401, "Token 无效或已过期");
            }

            String phone = jwtUtils.getPhoneFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            String redisKey = "token:" + phone;
            String redisToken = (String) redisTemplate.opsForValue().get(redisKey);
            if (redisToken == null || !redisToken.equals(token)) {
                return Result.error(401, "Token 已被注销或失效");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("valid", true);
            data.put("phone", phone);
            data.put("username", username);
            data.put("authorities", jwtUtils.getAuthoritiesFromToken(token)
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            return Result.success("Token 有效", data);

        } catch (Exception e) {
            logger.error("Token 验证异常", e);
            return Result.error(401, "Token 验证失败");
        }
    }

    /*
     * 测试接口
     */
    @GetMapping("/testO")
    public Result<Map<String, Object>> testO(
            @RequestParam(required = false) String hdname,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String remark) {

        Map<String, Object> data = new HashMap<>();
        data.put("活动名称", hdname);
        data.put("名字", username);
        data.put("手机号", phone);
        data.put("邮箱", email);
        data.put("其他", remark);

        logger.info("收到测试请求 - 数据: {}", data);
        return Result.success("请求成功", data);
    }

    // ==================== 限流相关方法 ====================

    private boolean isRateLimited(String clientIp) {
        long now = System.currentTimeMillis();
        Long blockUntil = loginBlocked.get(clientIp);
        if (blockUntil != null) {
            if (now < blockUntil) {
                return true;
            }
            loginBlocked.remove(clientIp);
            loginAttempts.remove(clientIp);
        }
        if (loginAttempts.size() > 1000) {
            cleanupStaleEntries(now);
        }
        return false;
    }

    private void cleanupStaleEntries(long now) {
        loginAttempts.entrySet().removeIf(entry -> now - entry.getValue()[1] > RATE_LIMIT_WINDOW_MS);
        loginBlocked.entrySet().removeIf(entry -> now > entry.getValue());
    }

    private void recordFailedAttempt(String clientIp) {
        long now = System.currentTimeMillis();
        long[] record = loginAttempts.compute(clientIp, (ip, v) -> {
            if (v == null || now - v[1] > RATE_LIMIT_WINDOW_MS) {
                return new long[]{1, now};
            }
            v[0]++;
            return v;
        });
        if (record != null && record[0] >= MAX_LOGIN_ATTEMPTS) {
            loginBlocked.put(clientIp, now + RATE_LIMIT_WINDOW_MS);
            loginAttempts.remove(clientIp);
            logger.warn("IP 被封禁 {} 分钟: {}", RATE_LIMIT_WINDOW_MS / 60000, clientIp);
        }
    }

    private void clearLoginAttempts(String clientIp) {
        loginAttempts.remove(clientIp);
        loginBlocked.remove(clientIp);
    }
}
