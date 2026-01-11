package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.entity.ShopType;
import com.tiv.rating.system.service.ShopTypeService;
import com.tiv.rating.system.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private ShopTypeService shopTypeService;

    @GetMapping("/list")
    public BusinessResponse<List<ShopType>> listShopType() {
        return ResultUtils.success(shopTypeService.listShopType());
    }

}
