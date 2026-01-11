package com.tiv.rating.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.entity.Shop;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.ShopMapper;
import com.tiv.rating.system.service.ShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Shop getShopById(String id) {
        // 1. 查询redis缓存
        String shopKey = String.format("%s_%s", RedisConstants.SHOP, id);
        String shopCache = stringRedisTemplate.opsForValue().get(shopKey);
        if (StrUtil.isNotBlank(shopCache)) {
            return JSONUtil.toBean(shopCache, Shop.class);
        }
        // 2. 缓存不存在,查询数据库
        Shop shop = getById(id);
        if (shop == null) {
            throw new BusinessException(BusinessCodeEnum.NOT_FOUND_ERROR, "店铺不存在");
        }
        // 3. 缓存
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), RedisConstants.SHOP_TTL, TimeUnit.DAYS);

        return shop;
    }

}




