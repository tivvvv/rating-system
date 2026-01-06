package com.tiv.rating.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.Constants;
import com.tiv.rating.system.dto.LoginDTO;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.User;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.UserMapper;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.RegexUtils;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public void sendCode(String phone, HttpSession session) {
        // 1. 校验手机号格式
        if (!RegexUtils.isPhoneValid(phone)) {
            ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, "手机号格式错误");
        }

        // 2. 生成验证码
        String code = RandomUtil.randomString(6);

        // 3. 保存验证码到session中
        session.setAttribute(Constants.CODE, code);

        // 4. 发送验证码
        log.debug("sendCode--手机号:{},验证码:{}", phone, code);
    }

    @Override
    public void login(LoginDTO loginDTO, HttpSession session) {
        String phone = loginDTO.getPhone();
        // 1. 校验手机号格式
        if (!RegexUtils.isPhoneValid(phone)) {
            ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, "手机号格式错误");
        }

        // 2. 校验验证码
        Object cacheCode = session.getAttribute(Constants.CODE);
        if (cacheCode == null || !cacheCode.toString().equals(loginDTO.getCode())) {
            ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, "验证码错误");
        }

        // 3. 根据手机号查询用户
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 4. 不存在则创建用户
            user = createUserWithPhone(phone);
        }

        // 5. 保存用户信息到session中
        session.setAttribute(Constants.USER, BeanUtil.copyProperties(user, UserDTO.class));
    }

    private User createUserWithPhone(String phone) {
        User user = User.builder()
                .phone(phone)
                .nickname(Constants.NICKNAME_PREFIX + RandomUtil.randomString(10))
                .build();
        save(user);
        return user;
    }

}




