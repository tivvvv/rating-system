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
import org.springframework.transaction.annotation.Transactional;

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

        // 2. 缓存命中空值
        if (shopCache != null) {
            throw new BusinessException(BusinessCodeEnum.NOT_FOUND_ERROR, "店铺不存在");
        }

        // 3. 缓存不存在,查询数据库
        Shop shop = getById(id);
        if (shop == null) {
            // 4. 数据库中不存在,将空值写入redis
            stringRedisTemplate.opsForValue().set(shopKey, "", RedisConstants.NULL_TTL, TimeUnit.MINUTES);
            throw new BusinessException(BusinessCodeEnum.NOT_FOUND_ERROR, "店铺不存在");
        }
        // 5. 缓存
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), RedisConstants.SHOP_TTL, TimeUnit.MINUTES);

        return shop;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShop(Shop shop) {
        Long shopId = shop.getId();
        if (shopId == null) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "店铺id为空");
        }

        // 1. 更新数据库
        updateById(shop);
        // 2. 清除缓存
        stringRedisTemplate.delete(String.format("%s_%s", RedisConstants.SHOP, shopId));
    }

}




