package com.tiv.rating.system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺查询条件
 */
@Data
@NoArgsConstructor
public class ShopQueryDTO {

    /**
     * 页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 查询半径/m
     */
    private Double radius;

    /**
     * 是否携带完整的地理位置查询条件
     */
    public boolean hasGeo() {
        return longitude != null && latitude != null;
    }

}
