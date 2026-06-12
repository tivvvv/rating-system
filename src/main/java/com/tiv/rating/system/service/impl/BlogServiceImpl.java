package com.tiv.rating.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.CommonConstants;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.Blog;
import com.tiv.rating.system.entity.Follow;
import com.tiv.rating.system.entity.User;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.BlogMapper;
import com.tiv.rating.system.service.BlogService;
import com.tiv.rating.system.service.FollowService;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    private FollowService followService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long saveBlog(Blog blog) {
        // 1. 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 2. 保存笔记
        boolean saveSuccess = save(blog);
        if (!saveSuccess) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "保存笔记失败");
        }
        // 3. 查询粉丝
        List<Follow> follows = followService.query()
                .eq("follow_user_id", user.getId())
                .list();
        // 4. 给每个粉丝推送笔记消息
        for (Follow follow : follows) {
            Long userId = follow.getUserId();
            String feedKey = getFeedKey(userId);
            stringRedisTemplate.opsForZSet().add(feedKey, blog.getId().toString(), System.currentTimeMillis());
        }

        return blog.getId();
    }

    private String getFeedKey(Long userId) {
        return RedisConstants.FEED_KEY + userId;
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
        Double score = stringRedisTemplate.opsForZSet().score(likeKey, userId.toString());
        blog.setIsLiked(score != null);
    }

    @Override
    public void likeBlog(Long id) {
        // 1. 获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2. 判断当前用户是否已经点赞
        String likeKey = String.format("%s_%s", RedisConstants.BLOG_LIKE, id);
        Double score = stringRedisTemplate.opsForZSet().score(likeKey, userId.toString());
        if (score == null) {
            // 数据库点赞+1
            boolean isSuccess = update().setSql("like_count = like_count + 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(likeKey, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 数据库点赞-1
            boolean isSuccess = update().setSql("like_count = like_count - 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(likeKey, userId.toString());
            }
        }
    }

    @Override
    public List<UserDTO> getBlogLikeUsers(Long id) {
        String likeKey = String.format("%s_%s", RedisConstants.BLOG_LIKE, id);
        // 1. 查询最新点赞的5个用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(likeKey, -5, -1);
        if (CollectionUtil.isEmpty(top5)) {
            return Collections.emptyList();
        }
        // 2. 查询用户信息
        List<Long> userIds = top5.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        String userIdStr = StrUtil.join(",", userIds);
        return userService.query().in("id", userIds)
                .last("ORDER BY FIELD(id, " + userIdStr + ")")
                .list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<Blog> getBlogByUser(Long userId, Long current, Long size) {
        // 1. 页码兜底
        long pageNum = (current == null || current < 1) ? 1L : current;
        // 2. 每页条数兜底
        long pageSize = (size == null || size < 1)
                ? CommonConstants.DEFAULT_PAGE_SIZE
                : Math.min(size, CommonConstants.MAX_PAGE_SIZE);
        // 3. 按发布时间倒序分页查询用户的笔记
        return query().eq("user_id", userId)
                .orderByDesc("create_time", "id")
                .page(new Page<>(pageNum, pageSize));
    }

}