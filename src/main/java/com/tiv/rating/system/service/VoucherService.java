package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.entity.Voucher;

import java.util.List;

public interface VoucherService extends IService<Voucher> {

    void addSeckillVoucher(Voucher voucher);

    List<Voucher> queryVouchersOfShop(Long shopId);

}
