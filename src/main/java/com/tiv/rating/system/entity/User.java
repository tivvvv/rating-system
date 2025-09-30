package com.tiv.rating.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 用户表
 *
 * @TableName user
 */
@Data
@TableName(value = "user")
public class User implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 头像
     */
    @TableField(value = "icon")
    private String icon;

    /**
     * 所在城市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 个人介绍
     */
    @TableField(value = "introduce")
    private String introduce;

    /**
     * 粉丝数
     */
    @TableField(value = "fan_count")
    private Integer fanCount;

    /**
     * 关注数
     */
    @TableField(value = "followee_count")
    private Integer followeeCount;

    /**
     * 性别 0:保密,1:男,2:女
     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField(value = "birthday")
    private Date birthday;

    /**
     * 积分
     */
    @TableField(value = "point")
    private Integer point;

    /**
     * 会员级别,0-9级,0为未开通会员
     */
    @TableField(value = "level")
    private Integer level;

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