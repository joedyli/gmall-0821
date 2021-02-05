package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD}) // 注解作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时注解
@Documented
public @interface GmallCache {

    /**
     * 缓存的前缀
     * 将来缓存的key：prefix + 方法参数
     * @return
     */
    String prefix() default "";

    /**
     * 缓存的过期时间
     * 单位为min
     * @return
     */
    int timeout() default 5;

    /**
     * 为了防止缓存雪崩
     * 给缓存指定随机值范围
     * @return
     */
    int random() default 5;

    /**
     * 为了防止缓存击穿，添加分布式锁
     * 此处需要指定分布式锁前缀
     * @return
     */
    String lock() default "lock:";
}
