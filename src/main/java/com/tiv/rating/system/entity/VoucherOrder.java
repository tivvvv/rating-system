package com.tiv.rating.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 优惠券订单表
 *
 * @TableName voucher_order
 */
@Data
@TableName(value = "voucher_order")
public class VoucherOrder implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 下单用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 优惠券id
     */
    @TableField(value = "voucher_id")
    private Long voucherId;

    /**
     * 支付方式 1:余额支付,2:支付宝,3:微信
     */
    @TableField(value = "pay_type")
    private Integer payType;

    /**
     * 订单状态 1:未支付,2:已支付,3:已核销,4:已取消,5:退款中,6:已退款
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 下单时间
     */
    @TableField(value = "order_time")
    private Date orderTime;

    /**
     * 支付时间
     */
    @TableField(value = "pay_time")
    private Date payTime;

    /**
     * 核销时间
     */
    @TableField(value = "use_time")
    private Date useTime;

    /**
     * 退款时间
     */
    @TableField(value = "refund_time")
    private Date refundTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}