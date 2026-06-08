package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.entity.Blog;
import com.tiv.rating.system.service.BlogService;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @PostMapping
    public BusinessResponse<Long> saveBlog(@RequestBody Blog blog) {
        return ResultUtils.success(blogService.saveBlog(blog));
    }

    @PutMapping
    public BusinessResponse<?> updateBlog(@RequestBody Blog blog) {
        blogService.updateById(blog);
        return ResultUtils.success();
    }

    @GetMapping("/{id}")
    public BusinessResponse<?> getBlogById(@PathVariable Long id) {
        return ResultUtils.success(blogService.getBlogById(id));
    }

}