package com.tiv.rating.system.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.Blog;
import com.tiv.rating.system.entity.User;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.BlogMapper;
import com.tiv.rating.system.service.BlogService;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long saveBlog(Blog blog) {
        // 1. 获取登录用户
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
        // 3. 填充点赞信息
        populateBlogIsLiked(blog);
        return blog;
    }

    private void populateBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setUserName(user.getNickname());
        blog.setUserIcon(user.getIcon());
    }

    private void populateBlogIsLiked(Blog blog) {
        // 1. 获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            // 用户未登录, 无需填充是否点赞
            return;
        }
        Long userId = user.getId();
        // 2. 判断用户是否已点赞
        String likeKey = String.format("%s_%s", RedisConstants.BLOG_LIKE, blog.getId());
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(likeKey, userId.toString());
        blog.setIsLiked(BooleanUtil.isTrue(isMember));
    }

    @Override
    public void likeBlog(Long id) {
        // 1. 获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2. 判断当前用户是否已经点赞
        String likeKey = String.format("%s_%s", RedisConstants.BLOG_LIKE, id);
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(likeKey, userId.toString());
        if (BooleanUtil.isFalse(isMember)) {
            // 数据库点赞+1
            boolean isSuccess = update().setSql("like_count = like_count + 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                stringRedisTemplate.opsForSet().add(likeKey, userId.toString());
            }
        } else {
            // 数据库点赞-1
            boolean isSuccess = update().setSql("like_count = like_count - 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(likeKey, userId.toString());
            }
        }
    }

}