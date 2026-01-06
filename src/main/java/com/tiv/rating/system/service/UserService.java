package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.dto.LoginDTO;
import com.tiv.rating.system.entity.User;

import javax.servlet.http.HttpSession;

public interface UserService extends IService<User> {

    void sendCode(String phone, HttpSession session);

    void login(LoginDTO loginDTO, HttpSession session);

}
