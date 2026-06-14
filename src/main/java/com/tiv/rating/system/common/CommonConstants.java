package com.tiv.rating.system.common;

public interface CommonConstants {

    String AUTHORIZATION = "Authorization";

    String NICKNAME_PREFIX = "user_";

    /**
     * 分页默认每页条数
     */
    Long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 分页每页最大条数
     */
    Long MAX_PAGE_SIZE = 100L;

    /**
     * 附近店铺的默认查询半径/m
     */
    Double DEFAULT_GEO_SEARCH_RADIUS_METERS = 5000D;

}
