package com.tiv.rating.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.CommonConstants;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.common.RedisData;
import com.tiv.rating.system.dto.ShopQueryDTO;
import com.tiv.rating.system.entity.Shop;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.ShopMapper;
import com.tiv.rating.system.service.ShopService;
import com.tiv.rating.system.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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
    public Long addShop(Shop shop) {
        // 1. 保存店铺
        save(shop);
        // 2. 落库后再按店铺类型写入redis geo
        if (shop.getTypeId() != null && shop.getLongitude() != null && shop.getLatitude() != null) {
            Long shopId = shop.getId();
            String geoKey = getGeoKey(shop.getTypeId());
            Point point = new Point(shop.getLongitude(), shop.getLatitude());
            runAfterCommit(() -> stringRedisTemplate.opsForGeo().add(geoKey, point, shopId.toString()));
        }
        return shop.getId();
    }

    /**
     * 在当前事务提交后执行指定动作
     */
    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    @Override
    public List<ShopVO> queryShopByType(Long typeId, ShopQueryDTO query) {
        // 页码兜底
        Long current = query.getCurrent();
        long pageNum = (current == null || current < 1) ? 1L : current;
        // 每页条数兜底
        Long size = query.getSize();
        long pageSize = (size == null || size < 1)
                ? CommonConstants.DEFAULT_PAGE_SIZE
                : Math.min(size, CommonConstants.MAX_PAGE_SIZE);

        // 不需要按坐标查询
        if (!query.hasGeo()) {
            return queryByTypeFromDb(typeId, pageNum, pageSize);
        }

        try {
            return queryNearbyFromGeo(typeId, query, pageNum, pageSize);
        } catch (DataAccessException e) {
            log.warn("queryShopByType--redis geo查询失败, 降级为数据库按类型查询, typeId={}", typeId, e);
            return queryByTypeFromDb(typeId, pageNum, pageSize);
        }
    }

    private List<ShopVO> queryByTypeFromDb(Long typeId, long pageNum, long pageSize) {
        Page<Shop> page = query()
                .eq("type_id", typeId)
                .page(new Page<>(pageNum, pageSize));
        return page.getRecords()
                .stream()
                .map(shop -> new ShopVO(shop, null))
                .collect(Collectors.toList());
    }

    /**
     * 从redis geo按距离升序查询附近店铺
     */
    private List<ShopVO> queryNearbyFromGeo(Long typeId, ShopQueryDTO query, long pageNum, long pageSize) {
        long from = (pageNum - 1) * pageSize;
        long end = pageNum * pageSize;
        double radius = (query.getRadius() != null && query.getRadius() > 0)
                ? query.getRadius()
                : CommonConstants.DEFAULT_GEO_SEARCH_RADIUS_METERS;

        // 1. 查询redis geo
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        getGeoKey(typeId),
                        GeoReference.fromCoordinate(query.getLongitude(), query.getLatitude()),
                        new Distance(radius, RedisGeoCommands.DistanceUnit.METERS),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                                .includeDistance()
                                .sortAscending()
                                .limit(end));
        if (results == null) {
            return Collections.emptyList();
        }

        // 2. 逻辑分页,跳过前from条
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> pageResults = results.getContent()
                .stream()
                .skip(from)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(pageResults)) {
            return Collections.emptyList();
        }

        // 3. 查询店铺信息
        List<Long> shopIds = pageResults.stream()
                .map(result -> Long.valueOf(result.getContent().getName()))
                .collect(Collectors.toList());
        Map<Long, Shop> shopMap = listByIds(shopIds)
                .stream()
                .collect(Collectors.toMap(Shop::getId, Function.identity()));

        List<ShopVO> shopVOList = new ArrayList<>(pageResults.size());
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : pageResults) {
            Shop shop = shopMap.get(Long.valueOf(result.getContent().getName()));
            if (shop != null) {
                shopVOList.add(new ShopVO(shop, result.getDistance().getValue()));
            }
        }
        return shopVOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShop(Shop shop) {
        Long shopId = shop.getId();
        if (shopId == null) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "店铺id为空");
        }
        Shop oldShop = getById(shopId);
        // 1. 更新数据库
        updateById(shop);
        // 2. 清除缓存
        stringRedisTemplate.delete(String.format("%s_%s", RedisConstants.SHOP, shopId));
        // 3. 提交后同步geo
        syncGeoAfterUpdate(shopId, oldShop, shop);
    }

    /**
     * 店铺更新后同步redis geo
     */
    private void syncGeoAfterUpdate(Long shopId, Shop oldShop, Shop update) {
        if (oldShop == null) {
            return;
        }
        Long oldTypeId = oldShop.getTypeId();
        Double oldLng = oldShop.getLongitude();
        Double oldLat = oldShop.getLatitude();
        Long newTypeId = update.getTypeId() != null ? update.getTypeId() : oldTypeId;
        Double newLng = update.getLongitude() != null ? update.getLongitude() : oldLng;
        Double newLat = update.getLatitude() != null ? update.getLatitude() : oldLat;

        // 坐标与类型都没变,无需同步
        if (Objects.equals(oldTypeId, newTypeId)
                && Objects.equals(oldLng, newLng)
                && Objects.equals(oldLat, newLat)) {
            return;
        }

        boolean oldHasGeo = oldTypeId != null && oldLng != null && oldLat != null;
        boolean newHasGeo = newTypeId != null && newLng != null && newLat != null;

        String oldGeoKey = oldHasGeo ? getGeoKey(oldTypeId) : null;
        String newGeoKey = newHasGeo ? getGeoKey(newTypeId) : null;
        Point newPoint = newHasGeo ? new Point(newLng, newLat) : null;

        runAfterCommit(() -> {
            if (oldHasGeo && !oldGeoKey.equals(newGeoKey)) {
                stringRedisTemplate.opsForGeo().remove(oldGeoKey, shopId.toString());
            }
            if (newHasGeo) {
                stringRedisTemplate.opsForGeo().add(newGeoKey, newPoint, shopId.toString());
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeShop(Long id) {
        if (id == null) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "店铺id为空");
        }
        Shop shop = getById(id);
        if (shop == null) {
            return;
        }
        // 1. 删除数据库记录
        removeById(id);
        // 2. 清除缓存
        stringRedisTemplate.delete(String.format("%s_%s", RedisConstants.SHOP, id));
        // 3. 提交后清理geo成员
        if (shop.getTypeId() != null) {
            String geoKey = getGeoKey(shop.getTypeId());
            String member = id.toString();
            runAfterCommit(() -> stringRedisTemplate.opsForGeo().remove(geoKey, member));
        }
    }

    @Override
    public void rebuildShopGeo() {
        // 1. 按类型分组
        Map<Long, List<Shop>> shopsByType = list()
                .stream()
                .filter(shop -> shop.getTypeId() != null
                        && shop.getLongitude() != null
                        && shop.getLatitude() != null)
                .collect(Collectors.groupingBy(Shop::getTypeId));

        // 2. 按类型重建
        shopsByType.forEach((typeId, shops) -> {
            String geoKey = getGeoKey(typeId);
            String tempKey = geoKey + ":rebuild";
            List<RedisGeoCommands.GeoLocation<String>> locations = shops.stream()
                    .map(shop -> new RedisGeoCommands.GeoLocation<>(
                            shop.getId().toString(),
                            new Point(shop.getLongitude(), shop.getLatitude())))
                    .collect(Collectors.toList());
            // 在临时key上建好后rename原子替换,避免出现数据空窗期
            stringRedisTemplate.delete(tempKey);
            stringRedisTemplate.opsForGeo().add(tempKey, locations);
            stringRedisTemplate.rename(tempKey, geoKey);
        });
        log.info("rebuildShopGeo--重建完成,涉及{}个店铺类型", shopsByType.size());
    }

    private Boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    private String getGeoKey(Long shopTypeId) {
        return RedisConstants.SHOP_GEO_KEY + shopTypeId;
    }

}




