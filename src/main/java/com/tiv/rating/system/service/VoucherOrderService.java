package com.tiv.rating.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.rating.system.entity.VoucherOrder;

public interface VoucherOrderService extends IService<VoucherOrder> {

    Long seckillVoucher(Long voucherId);

    Long createVoucherOrder(Long voucherId, Long userId);

}
