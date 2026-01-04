package com.tiv.rating.system.common;

/**
 * 正则表达式
 */
public interface RegexPatterns {

    /**
     * 手机号正则
     */
    String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";

    /**
     * 邮箱正则
     */
    String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 密码正则,6-12位的字母,数字,下划线
     */
    String PASSWORD_REGEX = "^\\w{6,12}$";

    /**
     * 验证码正则,6位数字或字母
     */
    String CODE_REGEX = "^[a-zA-Z\\d]{6}$";

}
