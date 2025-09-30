package com.tiv.rating.system.dto;

import lombok.Data;

@Data
public class UserDTO {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String icon;

}
