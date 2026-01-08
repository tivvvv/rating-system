package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.dto.LoginDTO;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.ResultUtils;
import com.tiv.rating.system.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/code")
    public BusinessResponse<?> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResultUtils.success();
    }

    @PostMapping("/login")
    public BusinessResponse<String> login(@RequestBody LoginDTO loginDTO) {
        return ResultUtils.success(userService.login(loginDTO));
    }

    @GetMapping("/info")
    public BusinessResponse<UserDTO> info() {
        return ResultUtils.success(UserHolder.getUser());
    }

}
