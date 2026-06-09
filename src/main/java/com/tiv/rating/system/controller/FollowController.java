package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.service.FollowService;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    /**
     * 关注用户
     */
    @PostMapping("/{followUserId}")
    public BusinessResponse<?> follow(@PathVariable Long followUserId) {
        followService.follow(followUserId);
        return ResultUtils.success();
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{followUserId}")
    public BusinessResponse<?> unfollow(@PathVariable Long followUserId) {
        followService.unfollow(followUserId);
        return ResultUtils.success();
    }

    /**
     * 查询是否已关注该用户
     */
    @GetMapping("/{followUserId}")
    public BusinessResponse<Boolean> isFollow(@PathVariable Long followUserId) {
        return ResultUtils.success(followService.isFollow(followUserId));
    }

}
