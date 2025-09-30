package com.tiv.rating.system.dto;

import lombok.Data;

@Data
public class LoginDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 密码
     */
    private String password;

}
