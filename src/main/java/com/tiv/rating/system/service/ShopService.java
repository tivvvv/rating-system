package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.entity.Shop;

public interface ShopService extends IService<Shop> {

    Shop getShopById(Long id);

    void updateShop(Shop shop);

}
