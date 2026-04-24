package com.weep.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * <p>
 * 用于标记需要特定权限才能访问的方法
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限代码列表
     * <p>
     * 例如：{"product:view", "order:manage"}
     * </p>
     *
     * @return 权限代码数组
     */
    String[] value();

    /**
     * 逻辑关系（AND/OR）
     * <p>
     * AND: 需要拥有所有指定的权限
     * OR: 只需要拥有其中一个权限
     * </p>
     *
     * @return 逻辑关系，默认为 AND
     */
    Logical logical() default Logical.AND;

    /**
     * 逻辑枚举
     */
    enum Logical {
        AND, OR
    }
}
