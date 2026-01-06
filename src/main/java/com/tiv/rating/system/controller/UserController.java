package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.dto.LoginDTO;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
        userService.sendCode(phone, session);
        return ResultUtils.success();
    }

    @PostMapping("/login")
    public BusinessResponse<?> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        userService.login(loginDTO, session);
        return ResultUtils.success();
    }

}
