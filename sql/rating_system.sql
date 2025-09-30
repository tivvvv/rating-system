SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `phone`          varchar(11)  NOT NULL COMMENT '手机号',
    `password`       varchar(128) NOT NULL DEFAULT '' COMMENT '密码',
    `nickname`       varchar(32)  NOT NULL DEFAULT '' COMMENT '昵称',
    `icon`           varchar(255) NOT NULL DEFAULT '' COMMENT '头像',
    `city`           varchar(64)  NULL COMMENT '所在城市',
    `introduce`      varchar(128) NULL COMMENT '个人介绍',
    `fan_count`      int(10)      NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `followee_count` int(10)      NOT NULL DEFAULT 0 COMMENT '关注数',
    `gender`         tinyint(1)   NOT NULL DEFAULT 0 COMMENT '性别 0:保密,1:男,2:女',
    `birthday`       date         NULL COMMENT '生日',
    `point`          int(10)      NOT NULL DEFAULT 0 COMMENT '积分',
    `level`          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '会员级别,0-9级,0为未开通会员',
    `create_time`    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) COMMENT '用户表';

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id`        bigint(20) NOT NULL COMMENT '用户id',
    `follow_user_id` bigint(20) NOT NULL COMMENT '关注的用户id',
    `create_time`    timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`)
) COMMENT '关注表';

-- ----------------------------
-- Table structure for sign
-- ----------------------------
DROP TABLE IF EXISTS `sign`;
CREATE TABLE `sign`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id`     bigint(20) NOT NULL COMMENT '用户id',
    `year`        year       NOT NULL COMMENT '签到的年',
    `month`       tinyint(2) NOT NULL COMMENT '签到的月',
    `date`        date       NOT NULL COMMENT '签到的日期',
    `is_backup`   tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否补签',
    `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) COMMENT '签到表';

-- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog`
(
    `id`            bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id`       bigint(20)    NOT NULL COMMENT '用户id',
    `shop_id`       bigint(20)    NOT NULL COMMENT '店铺id',
    `title`         varchar(255)  NOT NULL COMMENT '笔记标题',
    `content`       varchar(2048) NOT NULL COMMENT '笔记内容',
    `like_count`    int(10)       NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` int(10)       NOT NULL DEFAULT 0 COMMENT '评论数',
    `create_time`   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_shop_id` (`shop_id`)
) COMMENT '笔记表';

-- ----------------------------
-- Table structure for blog_image
-- ----------------------------
DROP TABLE IF EXISTS `blog_image`;
CREATE TABLE `blog_image`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `blog_id`     bigint(20)   NOT NULL COMMENT '笔记id',
    `image`       varchar(512) NOT NULL COMMENT '笔记图片',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_blog_id` (`blog_id`)
) COMMENT '笔记图片表';

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id`     bigint(20)   NOT NULL COMMENT '用户id',
    `blog_id`     bigint(20)   NOT NULL COMMENT '笔记id',
    `root_id`     bigint(20)   NULL COMMENT '关联的一级评论id,null为一级评论',
    `answer_id`   bigint(20)   NULL COMMENT '回复的评论id,null为一级评论',
    `content`     varchar(255) NOT NULL COMMENT '评论内容',
    `like`        int(10)      NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态 0:被删除,1:正常,2:被举报,3:被封禁',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_blog_id` (`blog_id`)
) COMMENT '评论表';

-- ----------------------------
-- Table structure for shop
-- ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`          varchar(128) NOT NULL COMMENT '店铺名称',
    `type_id`       bigint(20)   NOT NULL COMMENT '店铺类型id',
    `area`          varchar(128) NULL COMMENT '商圈',
    `address`       varchar(255) NOT NULL COMMENT '地址',
    `latitude`      double       NOT NULL COMMENT '经度',
    `longitude`     double       NOT NULL COMMENT '纬度',
    `avg_price`     double       NOT NULL COMMENT '均价',
    `sold`          int(10)      NOT NULL DEFAULT 0 COMMENT '销量',
    `comment_count` int(10)      NOT NULL DEFAULT 0 COMMENT '评论数',
    `score`         double       NULL COMMENT '店铺评分,1-5分',
    `open_hours`    varchar(32)  NOT NULL COMMENT '营业时间',
    `create_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_id` (`type_id`)
) COMMENT '店铺表';

-- ----------------------------
-- Table structure for shop_type
-- ----------------------------
DROP TABLE IF EXISTS `shop_type`;
CREATE TABLE `shop_type`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`        varchar(32)  NOT NULL COMMENT '类型名称',
    `icon`        varchar(255) NOT NULL COMMENT '类型图标',
    `sort`        int(3)       NOT NULL COMMENT '顺序',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) COMMENT '店铺类型表';

-- ----------------------------
-- Table structure for shop_image
-- ----------------------------
DROP TABLE IF EXISTS `shop_image`;
CREATE TABLE `shop_image`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `shop_id`     bigint(20)   NOT NULL COMMENT '店铺id',
    `image`       varchar(512) NOT NULL COMMENT '店铺图片',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`)
) COMMENT '店铺图片表';

-- ----------------------------
-- Table structure for voucher
-- ----------------------------
DROP TABLE IF EXISTS `voucher`;
CREATE TABLE `voucher`
(
    `id`             bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `shop_id`        bigint(20)    NULL COMMENT '商铺id',
    `title`          varchar(255)  NOT NULL COMMENT '优惠券标题',
    `sub_title`      varchar(255)  NULL COMMENT '副标题',
    `rule`           varchar(1024) NOT NULL COMMENT '使用规则',
    `require_value`  double        NOT NULL COMMENT '门槛金额',
    `discount_value` double        NOT NULL COMMENT '抵扣金额',
    `type`           tinyint(1)    NOT NULL DEFAULT 0 COMMENT '类型 0:普通券,1:秒杀券',
    `status`         tinyint(1)    NOT NULL DEFAULT 1 COMMENT '状态 1:上架,2:下架,3:过期',
    `create_time`    timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`)
) COMMENT '优惠券表';

-- ----------------------------
-- Table structure for voucher_order
-- ----------------------------
DROP TABLE IF EXISTS `voucher_order`;
CREATE TABLE `voucher_order`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `user_id`     bigint(20) NOT NULL COMMENT '下单用户id',
    `voucher_id`  bigint(20) NOT NULL COMMENT '优惠券id',
    `pay_type`    tinyint(1) NOT NULL COMMENT '支付方式 1:余额支付,2:支付宝,3:微信',
    `status`      tinyint(1) NOT NULL COMMENT '订单状态 1:未支付,2:已支付,3:已核销,4:已取消,5:退款中,6:已退款',
    `order_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `pay_time`    timestamp  NULL COMMENT '支付时间',
    `use_time`    timestamp  NULL COMMENT '核销时间',
    `refund_time` timestamp  NULL COMMENT '退款时间',
    `update_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_voucher_id` (`voucher_id`)
) COMMENT '优惠券订单表';

-- ----------------------------
-- Table structure for voucher_seckill
-- ----------------------------
DROP TABLE IF EXISTS `voucher_seckill`;
CREATE TABLE `voucher_seckill`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `voucher_id`  bigint(20) NOT NULL COMMENT '优惠券的id',
    `stock`       int(10)    NOT NULL COMMENT '库存',
    `begin_time`  timestamp  NULL COMMENT '生效时间',
    `end_time`    timestamp  NULL COMMENT '失效时间',
    `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_voucher_id` (`voucher_id`)
) COMMENT '优惠券秒杀表';
