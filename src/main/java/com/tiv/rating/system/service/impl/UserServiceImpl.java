package com.tiv.rating.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.entity.User;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

}




