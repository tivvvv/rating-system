package com.tiv.rating.system.config;

import com.tiv.rating.system.interceptor.LoginInterceptor;
import com.tiv.rating.system.interceptor.TokenRefreshInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenRefreshInterceptor(stringRedisTemplate))
                .order(0);
        registry.addInterceptor(new LoginInterceptor())
                // 不需要加上context-path
                .excludePathPatterns("/user/code", "/user/login")
                .order(1);
    }

}
