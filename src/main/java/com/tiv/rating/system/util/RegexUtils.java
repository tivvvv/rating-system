package com.tiv.rating.system.util;

import cn.hutool.core.util.StrUtil;
import com.tiv.rating.system.common.RegexPatterns;

/**
 * 正则工具类
 */
public class RegexUtils {

    /**
     * 是否是有效手机号
     *
     * @param phone
     * @return
     */
    public static boolean isPhoneValid(String phone) {
        return match(phone, RegexPatterns.PHONE_REGEX);
    }

    /**
     * 是否是有效邮箱
     *
     * @param email
     * @return
     */
    public static boolean isEmailValid(String email) {
        return match(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * 是否是有效密码
     *
     * @param password
     * @return
     */
    public static boolean isPasswordValid(String password) {
        return match(password, RegexPatterns.PASSWORD_REGEX);
    }

    /**
     * 是否是有效验证码
     *
     * @param code
     * @return
     */
    public static boolean isCodeValid(String code) {
        return match(code, RegexPatterns.CODE_REGEX);
    }

    /**
     * 是否匹配正则表达式
     *
     * @param str
     * @param regex
     * @return
     */
    private static boolean match(String str, String regex) {
        if (StrUtil.isBlank(str)) {
            return false;
        }
        return str.matches(regex);
    }

}
