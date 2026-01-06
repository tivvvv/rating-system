package com.tiv.rating.system.util;

import com.tiv.rating.system.dto.UserDTO;

/**
 * 用户信息ThreadLocal
 */
public class UserHolder {

    private static final ThreadLocal<UserDTO> THREAD_LOCAL = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        THREAD_LOCAL.set(user);
    }

    public static void removeUser() {
        THREAD_LOCAL.remove();
    }

    public static UserDTO getUser() {
        return THREAD_LOCAL.get();
    }

}
