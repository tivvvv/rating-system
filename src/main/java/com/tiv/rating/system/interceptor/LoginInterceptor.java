package com.tiv.rating.system.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.tiv.rating.system.common.CommonConstants;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的token
        String token = request.getHeader(CommonConstants.AUTHORIZATION);
        if (StrUtil.isBlank(token)) {
            response.setStatus(401);
            return false;
        }

        // 2. 基于token获取redis中用户信息
        Map<Object, Object> userDTOMap = stringRedisTemplate.opsForHash().entries(String.format("%s_%s", RedisConstants.LOGIN_TOKEN, token));

        // 3. 判断用户是否登录
        if (MapUtil.isEmpty(userDTOMap)) {
            response.setStatus(401);
            return false;
        }

        // 4. 保存用户信息到ThreadLocal中
        UserHolder.saveUser(BeanUtil.fillBeanWithMap(userDTOMap, new UserDTO(), false));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 移除用户信息
        UserHolder.removeUser();
    }

}
