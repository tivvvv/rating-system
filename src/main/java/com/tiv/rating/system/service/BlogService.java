package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.entity.Blog;

public interface BlogService extends IService<Blog> {

    Long saveBlog(Blog blog);

    Blog getBlogById(Long id);

}
