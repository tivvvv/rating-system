package com.tiv.rating.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 店铺表
 *
 * @TableName shop
 */
@Data
@TableName(value = "shop")
public class Shop implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店铺名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 店铺类型id
     */
    @TableField(value = "type_id")
    private Long typeId;

    /**
     * 商圈
     */
    @TableField(value = "area")
    private String area;

    /**
     * 地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 经度
     */
    @TableField(value = "latitude")
    private Double latitude;

    /**
     * 纬度
     */
    @TableField(value = "longitude")
    private Double longitude;

    /**
     * 均价
     */
    @TableField(value = "avg_price")
    private Double avgPrice;

    /**
     * 销量
     */
    @TableField(value = "sold")
    private Integer sold;

    /**
     * 评论数
     */
    @TableField(value = "comment_count")
    private Integer commentCount;

    /**
     * 店铺评分,1-5分
     */
    @TableField(value = "score")
    private Double score;

    /**
     * 营业时间
     */
    @TableField(value = "open_hours")
    private String openHours;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}