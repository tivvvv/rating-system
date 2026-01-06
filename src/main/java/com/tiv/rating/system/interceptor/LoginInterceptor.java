package com.tiv.rating.system.interceptor;

import com.tiv.rating.system.common.Constants;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取session
        HttpSession session = request.getSession();

        // 2. 获取session中用户信息
        Object user = session.getAttribute(Constants.USER);

        // 3. 判断用户是否登录
        if (user == null) {
            response.setStatus(401);
            return false;
        }

        // 4. 保存用户信息到ThreadLocal中
        UserHolder.saveUser((UserDTO) user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 移除用户信息
        UserHolder.removeUser();
    }

}
