package com.tiv.rating.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 优惠券表
 *
 * @TableName voucher
 */
@Data
@TableName(value = "voucher")
public class Voucher implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商铺id
     */
    @TableField(value = "shop_id")
    private Long shopId;

    /**
     * 优惠券标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 副标题
     */
    @TableField(value = "sub_title")
    private String subTitle;

    /**
     * 使用规则
     */
    @TableField(value = "rule")
    private String rule;

    /**
     * 门槛金额
     */
    @TableField(value = "require_value")
    private Double requireValue;

    /**
     * 抵扣金额
     */
    @TableField(value = "discount_value")
    private Double discountValue;

    /**
     * 类型 0:普通券,1:秒杀券
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 状态 1:上架,2:下架,3:过期
     */
    @TableField(value = "status")
    private Integer status;

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