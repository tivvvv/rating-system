package com.tiv.rating.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.Blog;
import com.tiv.rating.system.entity.User;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.BlogMapper;
import com.tiv.rating.system.service.BlogService;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    private UserService userService;

    @Override
    public Long saveBlog(Blog blog) {
        // 1. 获取登陆用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 2. 保存笔记
        save(blog);
        return blog.getId();
    }

    @Override
    public Blog getBlogById(Long id) {
        // 1. 查询笔记
        Blog blog = getById(id);
        if (blog == null) {
            throw new BusinessException(BusinessCodeEnum.NOT_FOUND_ERROR, "笔记不存在");
        }
        // 2. 填充用户信息
        populateBlogUser(blog);

        return blog;
    }

    private void populateBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setUserName(user.getNickname());
        blog.setUserIcon(user.getIcon());
    }

}