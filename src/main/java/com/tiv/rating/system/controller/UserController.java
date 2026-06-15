package com.tiv.rating.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.dto.LoginDTO;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.User;
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

    @GetMapping("/{id}")
    public BusinessResponse<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResultUtils.success();
        }
        return ResultUtils.success(BeanUtil.copyProperties(user, UserDTO.class));
    }

    @PostMapping("/sign")
    public BusinessResponse<?> sign() {
        userService.sign();
        return ResultUtils.success();
    }

    @GetMapping("/sign/count")
    public BusinessResponse<Integer> signCount() {
        return ResultUtils.success(userService.signCount());
    }

}
