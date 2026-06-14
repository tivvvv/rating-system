package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.dto.ShopQueryDTO;
import com.tiv.rating.system.entity.Shop;
import com.tiv.rating.system.vo.ShopVO;

import java.util.List;

public interface ShopService extends IService<Shop> {

    Shop getShopById(Long id);

    void updateShop(Shop shop);

    void removeShop(Long id);

    void cacheShop(Long shopId, Long expireSeconds);

    Long addShop(Shop shop);

    List<ShopVO> queryShopByType(Long typeId, ShopQueryDTO query);

    void rebuildShopGeo();

}
