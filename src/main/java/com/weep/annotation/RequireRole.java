package com.weep.annotation;

import java.lang.annotation.*;

/**
 * 角色验证注解
 * <p>
 * 用于标记需要特定角色才能访问的方法
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 需要的角色代码列表
     * <p>
     * 例如：{"SUPER_ADMIN", "MERCHANT"}
     * </p>
     *
     * @return 角色代码数组
     */
    String[] value();

    /**
     * 逻辑关系（AND/OR）
     * <p>
     * AND: 需要拥有所有指定的角色
     * OR: 只需要拥有其中一个角色
     * </p>
     *
     * @return 逻辑关系，默认为 OR
     */
    Logical logical() default Logical.OR;

    /**
     * 逻辑枚举
     */
    enum Logical {
        AND, OR
    }
}
