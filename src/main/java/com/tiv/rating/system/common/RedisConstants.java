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

    String SECKILL_VOUCHER_STOCK = "seckill_voucher_stock";

    Long SECKILL_VOUCHER_STOCK_TTL = 7 * 24 * 60 * 60L;

    String SECKILL_VOUCHER_ORDER = "seckill_voucher_order";

    String QUEUE_NAME = "rating_system:stream:orders";

    String BLOG_LIKE = "blog_like";

}
