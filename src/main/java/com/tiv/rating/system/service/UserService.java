package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.dto.LoginDTO;
import com.tiv.rating.system.entity.User;

public interface UserService extends IService<User> {

    void sendCode(String phone);

    String login(LoginDTO loginDTO);

    void sign();

    Integer signCount();

}
