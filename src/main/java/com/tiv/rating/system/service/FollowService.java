package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.Follow;

import java.util.List;

public interface FollowService extends IService<Follow> {

    /**
     * 关注指定用户
     *
     * @param followUserId 被关注的用户id
     */
    void follow(Long followUserId);

    /**
     * 取消关注指定用户
     *
     * @param followUserId 被取关的用户id
     */
    void unfollow(Long followUserId);

    /**
     * 查询当前登录用户是否已关注指定用户
     *
     * @param followUserId 目标用户id
     * @return true-已关注, false-未关注
     */
    Boolean isFollow(Long followUserId);

    /**
     * 查询当前登录用户与目标用户的共同关注
     *
     * @param targetUserId 目标用户id
     * @return 共同关注的用户列表
     */
    List<UserDTO> getCommonFollows(Long targetUserId);

}
