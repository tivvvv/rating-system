package com.tiv.rating.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.entity.ShopType;
import com.tiv.rating.system.mapper.ShopTypeMapper;
import com.tiv.rating.system.service.ShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> listShopType() {

        // 1. 查询redis缓存
        String shopTypeListCache = stringRedisTemplate
                .opsForValue()
                .get(RedisConstants.SHOP_TYPE_LIST);
        if (StrUtil.isNotBlank(shopTypeListCache)) {
            return JSONUtil.toList(JSONUtil.parseArray(shopTypeListCache), ShopType.class);
        }
        // 2. 缓存不存在,查询数据库
        List<ShopType> typeList = this.query()
                .orderByAsc("sort")
                .list();
        if (CollUtil.isEmpty(typeList)) {
            return Collections.emptyList();
        }
        // 3. 缓存
        stringRedisTemplate
                .opsForValue()
                .set(RedisConstants.SHOP_TYPE_LIST, JSONUtil.toJsonStr(typeList), RedisConstants.SHOP_TYPE_TTL + RandomUtil.randomInt(RedisConstants.SHOP_TYPE_TTL), TimeUnit.DAYS);

        return typeList;
    }

}




