package com.tiv.rating.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 评论表
 *
 * @TableName comment
 */
@Data
@TableName(value = "comment")
public class Comment implements Serializable {

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
     * 笔记id
     */
    @TableField(value = "blog_id")
    private Long blogId;

    /**
     * 关联的一级评论id,null为一级评论
     */
    @TableField(value = "root_id")
    private Long rootId;

    /**
     * 回复的评论id,null为一级评论
     */
    @TableField(value = "answer_id")
    private Long answerId;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 点赞数
     */
    @TableField(value = "like")
    private Integer like;

    /**
     * 状态 0:被删除,1:正常,2:被举报,3:被封禁
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