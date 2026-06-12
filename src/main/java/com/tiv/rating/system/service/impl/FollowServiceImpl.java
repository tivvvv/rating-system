package com.tiv.rating.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.Follow;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.FollowMapper;
import com.tiv.rating.system.service.FollowService;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void follow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        // 1. 不能关注自己
        if (userId.equals(followUserId)) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "不能关注自己");
        }
        // 2. 校验目标用户是否存在
        if (userService.getById(followUserId) == null) {
            throw new BusinessException(BusinessCodeEnum.NOT_FOUND_ERROR, "关注的用户不存在");
        }
        // 3. 幂等: 已关注则直接返回
        if (isFollowed(userId, followUserId)) {
            return;
        }
        // 4. 新增关注记录
        Follow follow = Follow.builder()
                .userId(userId)
                .followUserId(followUserId)
                .build();
        save(follow);
        // 5. 更新缓存
        String key = getFollowSetKey(userId);
        ensureFollowSetCached(userId);
        stringRedisTemplate.opsForSet().add(key, followUserId.toString());
        stringRedisTemplate.expire(key, RedisConstants.FOLLOW_TTL, TimeUnit.SECONDS);
    }

    @Override
    public void unfollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        // 1. 删除关注记录
        lambdaUpdate()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId)
                .remove();
        // 2. 更新缓存
        stringRedisTemplate.opsForSet().remove(getFollowSetKey(userId), followUserId.toString());
    }

    @Override
    public Boolean isFollow(Long followUserId) {
        return isFollowed(UserHolder.getUser().getId(), followUserId);
    }

    @Override
    public List<UserDTO> getCommonFollows(Long targetUserId) {
        Long userId = UserHolder.getUser().getId();
        // 1. 确保双方关注集合已缓存
        ensureFollowSetCached(userId);
        ensureFollowSetCached(targetUserId);
        // 2. 求交集得到共同关注的用户id
        Set<String> commonIds = stringRedisTemplate.opsForSet()
                .intersect(getFollowSetKey(userId), getFollowSetKey(targetUserId));
        if (CollectionUtil.isEmpty(commonIds)) {
            return Collections.emptyList();
        }
        // 3. 批量查询用户信息
        List<Long> ids = commonIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 判断 userId 是否关注了 followUserId
     */
    private boolean isFollowed(Long userId, Long followUserId) {
        return lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId)
                .count() > 0;
    }

    private String getFollowSetKey(Long userId) {
        return RedisConstants.FOLLOW + userId;
    }

    /**
     * 确保用户的关注集合已缓存
     */
    private void ensureFollowSetCached(Long userId) {
        String key = getFollowSetKey(userId);
        if (stringRedisTemplate.hasKey(key)) {
            return;
        }
        // 缓存缺失
        List<Follow> follows = query()
                .select("follow_user_id")
                .eq("user_id", userId)
                .list();
        if (CollectionUtil.isEmpty(follows)) {
            return;
        }
        String[] members = follows.stream()
                .map(follow -> follow.getFollowUserId().toString())
                .toArray(String[]::new);
        stringRedisTemplate.opsForSet().add(key, members);
        stringRedisTemplate.expire(key, RedisConstants.FOLLOW_TTL, TimeUnit.SECONDS);
    }

}
