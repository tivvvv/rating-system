package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.Blog;

import java.util.List;

public interface BlogService extends IService<Blog> {

    Long saveBlog(Blog blog);

    Blog getBlogById(Long id);

    void likeBlog(Long id);

    List<UserDTO> getBlogLikeUsers(Long id);
}
