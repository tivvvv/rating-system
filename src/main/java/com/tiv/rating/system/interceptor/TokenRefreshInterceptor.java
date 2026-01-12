package com.tiv.rating.system.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
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
import java.util.concurrent.TimeUnit;

/**
 * token刷新拦截器
 */
public class TokenRefreshInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public TokenRefreshInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的token,对于没有token的请求直接放行,登录校验交给LoginInterceptor
        String token = request.getHeader(CommonConstants.AUTHORIZATION);
        if (StrUtil.isBlank(token)) {
            return true;
        }

        // 2. 基于token获取redis中用户信息
        String tokenKey = String.format("%s_%s", RedisConstants.LOGIN_TOKEN, token);
        Map<Object, Object> userDTOMap = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3. 判断用户是否登录
        if (MapUtil.isEmpty(userDTOMap)) {
            return true;
        }

        // 4. 保存用户信息到ThreadLocal中
        UserHolder.saveUser(BeanUtil.fillBeanWithMap(userDTOMap, new UserDTO(), false));
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_TOKEN_TTL + RandomUtil.randomInt(RedisConstants.LOGIN_TOKEN_TTL), TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 移除用户信息
        UserHolder.removeUser();
    }

}
