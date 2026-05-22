package com.tiv.rating.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.entity.SeckillVoucher;
import com.tiv.rating.system.entity.VoucherOrder;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.VoucherOrderMapper;
import com.tiv.rating.system.service.SeckillVoucherService;
import com.tiv.rating.system.service.VoucherOrderService;
import com.tiv.rating.system.util.IdGenerator;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private IdGenerator idGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long seckillVoucher(Long voucherId) {
        // 1. 查询秒杀优惠券
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        // 2. 判断秒杀优惠券是否存在
        if (seckillVoucher == null) {
            throw new BusinessException(BusinessCodeEnum.NOT_FOUND_ERROR, "秒杀优惠券不存在");
        }
        // 3. 判断秒杀优惠券是否处于可用状态
        if (seckillVoucher.getBeginTime().after(new Date())) {
            throw new BusinessException(BusinessCodeEnum.FORBIDDEN_ERROR, "秒杀尚未开始");
        }
        if (seckillVoucher.getEndTime().before(new Date())) {
            throw new BusinessException(BusinessCodeEnum.FORBIDDEN_ERROR, "秒杀已结束");
        }
        // 4. 判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "库存不足");
        }
        // 5. 扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .update();
        if (!success) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "库存不足");
        }
        // 6. 创建订单
        Long orderId = idGenerator.nextId("order");
        Long userId = UserHolder.getUser().getId();
        VoucherOrder voucherOrder = VoucherOrder.builder()
                .id(orderId)
                .userId(userId)
                .voucherId(voucherId)
                .build();
        save(voucherOrder);
        return orderId;
    }

}