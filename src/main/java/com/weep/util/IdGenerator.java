package com.weep.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ID 生成工具类
 *
 * <p>提供 UUID 生成等唯一标识符生成方法，适用于需要唯一标识的业务场景。
 */
@Component
public class IdGenerator {

    /**
     * 生成不带横线的 32 位 UUID 字符串
     * 
     * <p>适用于需要紧凑格式的场景，如数据库主键、文件命名等。
     *
     * @return 32 位 UUID 字符串，例如：550e8400e29b41d4a716446655440000
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成带横线的标准 UUID 字符串
     * 
     * <p>符合 RFC 4122 标准格式，适用于需要标准 UUID 格式的场景。
     *
     * @return 标准 UUID 字符串，例如：550e8400-e29b-41d4-a716-446655440000
     */
    public static String generateStandardUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成指定长度的随机字符串（基于 UUID）
     * 
     * <p>如果长度超过 32，则重复拼接 UUID 直到达到指定长度。
     *
     * @param length 字符串长度（必须大于 0）
     * @return 指定长度的随机字符串
     * @throws IllegalArgumentException 当长度小于等于 0 时抛出
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于 0");
        }

        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(generateUUID());
        }
        
        return sb.substring(0, length);
    }

    /**
     * 生成短 UUID（8 位）
     * 
     * <p>适用于对长度有严格要求的场景，但碰撞概率略高于标准 UUID。
     *
     * @return 8 位短 UUID
     */
    public static String generateShortUUID() {
        return generateUUID().substring(0, 8);
    }
}