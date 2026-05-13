package com.weep.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON 响应打包工具类
 *
 * <p>提供统一的响应数据打包格式，用于控制层方法返回标准化的 JSON 响应。
 * 包含两种打包方式：返回 XJson 对象或直接返回 JSON 字符串。
 */
public class PackJsn {
    
    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(PackJsn.class);
    
    /** JSON 序列化工具 */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 打包响应数据为 XJson 对象
     * 
     * <p>控制层每个方法统一使用的返回数据打包格式，由 @ResponseBody 自动转化为 JSON。
     *
     * @param code 响应状态码（0 表示成功，非 0 表示失败）
     * @param msg 响应消息
     * @param data 响应数据
     * @return XJson 对象
     */
    public static XJson pack(int code, String msg, Object data) {
        return new XJson(code, msg, data);
    }

    /**
     * 打包响应数据为 JSON 字符串
     *
     * @param code 响应状态码
     * @param msg 响应消息
     * @param data 响应数据
     * @return JSON 字符串
     */
    public static String packM(int code, String msg, Object data) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("msg", msg);
        m.put("data", data);
        
        String json = "";
        try {
            json = objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            logger.error("JSON 序列化失败: {}", e.getMessage());
        }
        logger.debug("JSON 响应: {}", json);
        return json;
    }

    /**
     * 拼接 JSON 对象字符串（私有方法，仅供内部使用）
     *
     * @param code 响应状态码
     * @param msg 响应消息
     * @param data 响应数据（字符串格式）
     * @return JSON 字符串
     */
    private String pack2(int code, String msg, String data) {
        String strJson = "{" +
                "\"code\":" + code + "," +
                "\"msg\":\"" + msg + "\"," +
                "\"data\":\"" + data + "\"" +
                "}";
        return strJson;
    }
}