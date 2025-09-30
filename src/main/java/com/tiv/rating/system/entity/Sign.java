package com.tiv.rating.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 签到表
 *
 * @TableName sign
 */
@Data
@TableName(value = "sign")
public class Sign implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 签到的年
     */
    @TableField(value = "year")
    private Object year;

    /**
     * 签到的月
     */
    @TableField(value = "month")
    private Integer month;

    /**
     * 签到的日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 是否补签
     */
    @TableField(value = "is_backup")
    private Integer isBackup;

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