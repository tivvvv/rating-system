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
import com.tiv.rating.system.util.SimpleRedisLock;
import com.tiv.rating.system.util.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

        // 5. 获取分布式锁
        Long userId = UserHolder.getUser().getId();
        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        if (!lock.tryLock(10 * 60L)) {
            throw new BusinessException(BusinessCodeEnum.FORBIDDEN_ERROR, "请勿重复下单");
        }

        try {
            // 6. 获取代理对象,避免事务失效
            VoucherOrderService proxy = (VoucherOrderService) AopContext.currentProxy();
            // 7. 创建订单
            return proxy.createVoucherOrder(voucherId, userId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucherOrder(Long voucherId, Long userId) {
        // 1. 一人一单
        int count = query().eq("voucher_id", voucherId)
                .eq("user_id", userId)
                .count();
        if (count > 0) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "用户已购买过该优惠券");
        }
        // 2. 扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!success) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "库存不足");
        }
        // 3. 创建订单
        Long orderId = idGenerator.nextId("order");
        VoucherOrder voucherOrder = VoucherOrder.builder()
                .id(orderId)
                .userId(userId)
                .voucherId(voucherId)
                .build();
        save(voucherOrder);
        return orderId;
    }

}