package com.tiv.rating.system.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.common.RedisData;
import com.tiv.rating.system.entity.Shop;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.ShopMapper;
import com.tiv.rating.system.service.ShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Shop getShopById(Long id) {
        // 1. 查询redis缓存
        String shopKey = String.format("%s_%s", RedisConstants.SHOP, id);
        String shopCache = stringRedisTemplate
                .opsForValue()
                .get(shopKey);
        if (StrUtil.isNotBlank(shopCache)) {
            return JSONUtil.toBean(shopCache, Shop.class);
        }

        // 2. 缓存命中空值
        if ("".equals(shopCache)) {
            return null;
        }

        // 3. 缓存不存在,重建缓存
        String lockKey = String.format("%s_%s", RedisConstants.LOCK_SHOP, id);
        Shop shop = null;
        try {
            Boolean isLock = tryLock(lockKey);
            if (!isLock) {
                // 4. 获取锁失败,休眠后重试
                Thread.sleep(50);
                return getShopById(id);
            }

            // 5. 获取锁成功,二次检测缓存是否存在
            shopCache = stringRedisTemplate
                    .opsForValue()
                    .get(shopKey);
            if (StrUtil.isNotBlank(shopCache)) {
                return JSONUtil.toBean(shopCache, Shop.class);
            }

            if ("".equals(shopCache)) {
                return null;
            }

            // 6. 缓存不存在,查询数据库
            shop = getById(id);
            if (shop == null) {
                // 7. 数据库中不存在,将空值写入redis
                stringRedisTemplate
                        .opsForValue()
                        .set(shopKey, "", RedisConstants.NULL_TTL + RandomUtil.randomInt(RedisConstants.NULL_TTL), TimeUnit.MINUTES);
                return null;
            }
            // 8. 存在,缓存
            stringRedisTemplate
                    .opsForValue()
                    .set(shopKey, JSONUtil.toJsonStr(shop), RedisConstants.SHOP_TTL + RandomUtil.randomInt(RedisConstants.SHOP_TTL), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("getShopById--重建缓存异常");
            throw new BusinessException(BusinessCodeEnum.SYSTEM_ERROR, "getShopById--重建缓存异常");
        } finally {
            // 9. 释放锁
            unlock(lockKey);
        }
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

    @Override
    public void cacheShop(Long shopId, Long expireSeconds) {
        // 1. 获取店铺
        Shop shop = getById(shopId);
        if (shop == null) {
            return;
        }
        // 2. 封装逻辑过期时间
        RedisData redisData = RedisData
                .builder()
                .data(shop)
                .expireTime(LocalDateTime.now().plusSeconds(expireSeconds))
                .build();
        // 3. 写入redis
        stringRedisTemplate.opsForValue().set(String.format("%s_%s", RedisConstants.SHOP, shopId), JSONUtil.toJsonStr(redisData));
    }

    private Boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

}




