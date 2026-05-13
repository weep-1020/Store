package com.weep.config;

/*
 * Redis配置类
 * 用于配置RedisTemplate Bean，提供统一的Redis数据访问接口
 * 配置了合适的序列化器以支持String类型的Key和JSON格式的Value
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/*
 * Redis配置类
 * 负责创建和配置RedisTemplate实例，定义数据存储的序列化规则
 */
@Configuration
public class RedisConfig {
    /*
     * 创建并配置RedisTemplate Bean
     * 设置连接工厂、序列化器，并初始化RedisTemplate
     *
     * @param factory Redis连接工厂，由Spring容器自动注入
     * @return 配置完成的RedisTemplate实例，支持String Key和Object Value
     * @throws IllegalArgumentException 当连接工厂为null时抛出异常
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        /* 验证连接工厂参数有效性 */
        if (factory == null) {
            throw new IllegalArgumentException("验证连接工厂参数有效性：RedisConnectionFactory must not be null");
        }
        
        /* 创建RedisTemplate实例 */
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        /* 创建序列化器实例 */
        /* StringRedisSerializer：用于序列化Key，保证Key以纯字符串形式存储 */
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        /* GenericJackson2JsonRedisSerializer：用于序列化Value，将对象转换为JSON格式存储 */
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        /* 设置Key和HashKey的序列化器为String类型 */
        /* Key序列化：确保Redis中的键以可读的字符串形式存储 */
        template.setKeySerializer(stringSerializer);
        /* HashKey序列化：确保Hash结构中的字段名以字符串形式存储 */
        template.setHashKeySerializer(stringSerializer);
        
        /* 设置Value和HashValue的序列化器为JSON格式 */
        /* Value序列化：将Java对象序列化为JSON字符串存储，便于跨平台读取 */
        template.setValueSerializer(jsonSerializer);
        /* HashValue序列化：将Hash结构中的值序列化为JSON格式 */
        template.setHashValueSerializer(jsonSerializer);
        
        /* 显式调用初始化方法，确保RedisTemplate所有属性配置完成并生效 */
        template.afterPropertiesSet();
        
        return template;
    }
}
