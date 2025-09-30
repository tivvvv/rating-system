package com.tiv.rating.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 优惠券秒杀表
 *
 * @TableName voucher_seckill
 */
@Data
@TableName(value = "voucher_seckill")
public class VoucherSeckill implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券的id
     */
    @TableField(value = "voucher_id")
    private Long voucherId;

    /**
     * 库存
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 生效时间
     */
    @TableField(value = "begin_time")
    private Date beginTime;

    /**
     * 失效时间
     */
    @TableField(value = "end_time")
    private Date endTime;

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