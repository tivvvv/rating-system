package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.service.VoucherOrderService;
import com.tiv.rating.system.util.ResultUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private VoucherOrderService voucherOrderService;

    @PostMapping("/seckill/{voucherId}")
    public BusinessResponse<Long> seckillVoucher(@PathVariable Long voucherId) {
        return ResultUtils.success(voucherOrderService.seckillVoucher(voucherId));
    }

}