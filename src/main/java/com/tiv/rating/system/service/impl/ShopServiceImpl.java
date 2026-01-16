package com.tiv.rating.system.service.impl;

import cn.hutool.core.lang.TypeReference;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    public Shop getShopById(Long id) {
        // 1. 查询redis缓存
        String shopKey = String.format("%s_%s", RedisConstants.SHOP, id);
        String shopCache = stringRedisTemplate
                .opsForValue()
                .get(shopKey);
        // 2. 缓存命中
        if (StrUtil.isNotBlank(shopCache)) {
            RedisData<Shop> redisData = JSONUtil.toBean(shopCache, new TypeReference<RedisData<Shop>>() {
            }, false);
            if (LocalDateTime.now().isBefore(redisData.getExpireTime())) {
                // 2.1 缓存未过期,直接返回
                return redisData.getData();
            }
            // 2.2 缓存已过期,重建缓存
            return rebuildShopCache(id, redisData.getData());
        }

        // 3. 缓存命中空值,返回null
        if ("".equals(shopCache)) {
            return null;
        }

        // 4. 缓存不存在,重建缓存
        return rebuildShopCache(id, null);
    }

    private Shop rebuildShopCache(Long shopId, Shop outdated) {
        String shopKey = String.format("%s_%s", RedisConstants.SHOP, shopId);
        String lockKey = String.format("%s_%s", RedisConstants.LOCK_SHOP, shopId);
        try {
            // 1. 尝试获取锁
            Boolean isLock = tryLock(lockKey);
            if (!isLock) {
                // 2. 获取锁失败,直接返回过期的店铺信息
                return outdated;
            }

            // 3. 获取锁成功,二次检测缓存是否存在
            String shopCache = stringRedisTemplate
                    .opsForValue()
                    .get(shopKey);
            if (StrUtil.isNotBlank(shopCache)) {
                RedisData<Shop> redisData = JSONUtil.toBean(shopCache, new TypeReference<RedisData<Shop>>() {
                }, false);
                if (LocalDateTime.now().isBefore(redisData.getExpireTime())) {
                    // 3.1 缓存未过期,直接返回
                    return redisData.getData();
                }
            }

            // 4. 缓存命中空值,返回null
            if ("".equals(shopCache)) {
                return null;
            }

            // 5. 缓存不存在,异步查询数据库重建缓存
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                this.cacheShop(shopId, RedisConstants.SHOP_TTL + RandomUtil.randomLong(RedisConstants.SHOP_TTL));
            });

        } catch (Exception e) {
            log.error("getShopById--重建缓存异常");
            throw new BusinessException(BusinessCodeEnum.SYSTEM_ERROR, "getShopById--重建缓存异常");
        } finally {
            // 9. 释放锁
            unlock(lockKey);
        }
        return outdated;
    }

    @Override
    public void cacheShop(Long shopId, Long expireSeconds) {
        // 1. 获取店铺
        Shop shop = getById(shopId);
        String shopKey = String.format("%s_%s", RedisConstants.SHOP, shopId);
        if (shop == null) {
            // 7. 数据库中不存在,将空值写入redis
            stringRedisTemplate
                    .opsForValue()
                    .set(shopKey, "", RedisConstants.NULL_TTL + RandomUtil.randomLong(RedisConstants.NULL_TTL), TimeUnit.SECONDS);
            return;
        }
        // 2. 封装逻辑过期时间
        RedisData<Shop> redisData = RedisData.<Shop>builder()
                .data(shop)
                .expireTime(LocalDateTime.now().plusSeconds(expireSeconds))
                .build();
        // 3. 写入redis
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(redisData));
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


    private Boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

}




