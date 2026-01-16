package com.tiv.rating.system.common;

public interface RedisConstants {

    String LOGIN_CODE = "login_code";

    String LOGIN_TOKEN = "login_token";

    Integer LOGIN_TOKEN_TTL = 60;

    String SHOP = "shop";

    Long SHOP_TTL = 30 * 60L;

    String SHOP_TYPE_LIST = "shop_type_list";

    Long SHOP_TYPE_TTL = 7 * 24 * 60 * 60L;

    Long NULL_TTL = 3 * 60L;

    String LOCK_SHOP = "lock_shop";

}
