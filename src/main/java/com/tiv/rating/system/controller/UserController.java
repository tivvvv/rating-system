package com.tiv.rating.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.RegexUtils;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/code")
    public BusinessResponse<?> sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 1. 校验手机号格式
        if (!RegexUtils.isPhoneValid(phone)) {
            ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, "手机号格式错误");
        }

        // 2. 生成验证码
        String code = RandomUtil.randomString(6);

        // 3. 保存验证码到session中
        session.setAttribute("code", code);

        // 4. 发送验证码
        log.debug("sendCode--手机号:{},验证码:{}", phone, code);
        return ResultUtils.success();
    }

}
