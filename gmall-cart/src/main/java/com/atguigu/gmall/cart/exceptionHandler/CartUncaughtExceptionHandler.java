package com.atguigu.gmall.cart.exceptionHandler;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Component
public class CartUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String EXCEPTION_KEY = "cart:exception:info";

    /**
     *
     * @param throwable
     * @param method
     * @param objects
     */
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("异步任务出现异常了。方法：{}，参数：{}，异常信息：{}", method.getName(), Arrays.asList(objects), throwable.getMessage());
        BoundSetOperations<String, String> setOps = this.redisTemplate.boundSetOps(EXCEPTION_KEY);
        setOps.add(objects[0].toString());
    }
}
