package com.tiv.rating.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.entity.Follow;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.FollowMapper;
import com.tiv.rating.system.service.FollowService;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private UserService userService;

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
    }

    @Override
    public void unfollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        // 删除关注记录
        lambdaUpdate()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId)
                .remove();
    }

    @Override
    public Boolean isFollow(Long followUserId) {
        return lambdaQuery()
                .eq(Follow::getUserId, UserHolder.getUser().getId())
                .eq(Follow::getFollowUserId, followUserId)
                .count() > 0;
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

}
