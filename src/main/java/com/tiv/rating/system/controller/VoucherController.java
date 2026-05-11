package com.tiv.rating.system.controller;

import com.tiv.rating.system.common.BusinessResponse;
import com.tiv.rating.system.entity.Voucher;
import com.tiv.rating.system.service.VoucherService;
import com.tiv.rating.system.util.ResultUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private VoucherService voucherService;

    /**
     * 添加秒杀优惠券
     *
     * @param voucher
     * @return
     */
    @PostMapping("/add/seckill")
    public BusinessResponse<Long> addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return ResultUtils.success(voucher.getId());
    }

    /**
     * 添加普通优惠券
     *
     * @param voucher
     * @return
     */
    @PostMapping("/add/common")
    public BusinessResponse<Long> addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return ResultUtils.success(voucher.getId());
    }

    /**
     * 查询店铺优惠券列表
     *
     * @param shopId
     * @return
     */
    @GetMapping("/list/{shopId}")
    public BusinessResponse<List<Voucher>> queryVouchersOfShop(@PathVariable("shopId") Long shopId) {
        return ResultUtils.success(voucherService.queryVouchersOfShop(shopId));
    }

}