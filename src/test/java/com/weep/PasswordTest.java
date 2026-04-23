package com.weep;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码测试工具
 * <p>
 * 用于生成和验证 BCrypt 密码哈希
 * </p>
 */
public class PasswordTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String plainPassword = "123456";
        
        System.out.println("========== BCrypt 密码验证测试 ==========\n");
        
        // 生成新的哈希值（每次都会不同）
        String newHash1 = encoder.encode(plainPassword);
        String newHash2 = encoder.encode(plainPassword);
        System.out.println("原始密码: " + plainPassword);
        System.out.println("新生成的哈希值1: " + newHash1);
        System.out.println("新生成的哈希值2: " + newHash2);
        System.out.println("两次生成的哈希值不同（正常，因为盐值随机）: " + !newHash1.equals(newHash2));
        
        // 验证数据库中现有的哈希值
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matches = encoder.matches(plainPassword, dbHash);
        
        System.out.println("\n========== 数据库哈希值验证 ==========");
        System.out.println("数据库中的哈希值: " + dbHash);
        System.out.println("密码 '123456' 匹配结果: " + matches);
        
        if (matches) {
            System.out.println("\n✅ 密码哈希值正确，可以正常使用");
        } else {
            System.out.println("\n❌ 密码哈希值不正确！");
            System.out.println("\n=== 请使用以下 SQL 更新数据库 ===");
            System.out.println("USE mall_db;");
            System.out.println("UPDATE users SET password = '" + newHash1 + "' WHERE username = 'admin';");
            System.out.println("UPDATE users SET password = '" + newHash1 + "' WHERE username = 'merchant';");
            System.out.println("UPDATE users SET password = '" + newHash1 + "' WHERE username = 'user';");
        }
        
        System.out.println("\n========== 测试其他常见密码 ==========");
        String[] testPasswords = {"password", "admin", "test123", "123456789"};
        for (String testPwd : testPasswords) {
            boolean testMatch = encoder.matches(testPwd, dbHash);
            System.out.println("密码 '" + testPwd + "' 匹配: " + testMatch);
        }
    }
}
