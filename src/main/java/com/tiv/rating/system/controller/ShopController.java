package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.dto.ShopQueryDTO;
import com.tiv.rating.system.entity.Shop;
import com.tiv.rating.system.service.ShopService;
import com.tiv.rating.system.util.ResultUtils;
import com.tiv.rating.system.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @GetMapping("/type/{typeId}")
    public BusinessResponse<List<ShopVO>> queryShopByType(@PathVariable Long typeId, @RequestBody ShopQueryDTO query) {
        return ResultUtils.success(shopService.queryShopByType(typeId, query));
    }

    @PostMapping
    public BusinessResponse<Long> addShop(@RequestBody Shop shop) {
        return ResultUtils.success(shopService.addShop(shop));
    }

    @PutMapping
    public BusinessResponse<?> updateShop(@RequestBody Shop shop) {
        shopService.updateShop(shop);
        return ResultUtils.success();
    }

    @DeleteMapping("/{id}")
    public BusinessResponse<?> removeShop(@PathVariable Long id) {
        shopService.removeShop(id);
        return ResultUtils.success();
    }

    @PostMapping("/geo/rebuild")
    public BusinessResponse<?> rebuildShopGeo() {
        shopService.rebuildShopGeo();
        return ResultUtils.success();
    }

    @PostMapping("/cache")
    public BusinessResponse<?> cacheShop(@RequestParam Long shopId, Long expireSeconds) {
        shopService.cacheShop(shopId, expireSeconds);
        return ResultUtils.success();
    }

}
