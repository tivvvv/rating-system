package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.entity.ShopType;

import java.util.List;

public interface ShopTypeService extends IService<ShopType> {

    List<ShopType> listShopType();

}
