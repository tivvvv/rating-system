package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.entity.Shop;
import com.tiv.rating.system.service.ShopService;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private ShopService shopService;

    @GetMapping("/{id}")
    public BusinessResponse<Shop> getShopById(@PathVariable Long id) {
        return ResultUtils.success(shopService.getShopById(id));
    }

    @PutMapping
    public BusinessResponse<?> updateShop(@RequestBody Shop shop) {
        shopService.updateShop(shop);
        return ResultUtils.success();
    }

}
