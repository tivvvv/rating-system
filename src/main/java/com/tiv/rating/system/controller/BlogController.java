package com.tiv.rating.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.dto.ScrollDTO;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.Blog;
import com.tiv.rating.system.service.BlogService;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @GetMapping("/user/{userId}")
    public BusinessResponse<Page<Blog>> getBlogByUser(
            @PathVariable Long userId,
            @RequestParam(value = "current", required = false) Long current,
            @RequestParam(value = "size", required = false) Long size) {
        return ResultUtils.success(blogService.getBlogByUser(userId, current, size));
    }

    @GetMapping("/follow")
    public BusinessResponse<ScrollDTO<Blog>> getBlogByFollow(
            @RequestParam Long lastId,
            @RequestParam Integer offset,
            @RequestParam(value = "size", required = false) Long size) {
        return ResultUtils.success(blogService.getBlogByFollow(lastId, offset, size));
    }

    @PutMapping("/like/{id}")
    public BusinessResponse<?> likeBlog(@PathVariable Long id) {
        blogService.likeBlog(id);
        return ResultUtils.success();
    }

    @GetMapping("/like/users/{id}")
    public BusinessResponse<List<UserDTO>> getBlogLikeUsers(@PathVariable Long id) {
        return ResultUtils.success(blogService.getBlogLikeUsers(id));
    }

}