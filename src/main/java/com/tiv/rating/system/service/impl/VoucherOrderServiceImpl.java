package com.tiv.rating.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.entity.VoucherOrder;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.VoucherOrderMapper;
import com.tiv.rating.system.service.SeckillVoucherService;
import com.tiv.rating.system.service.VoucherOrderService;
import com.tiv.rating.system.util.IdGenerator;
import com.tiv.rating.system.util.UserHolder;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private IdGenerator idGenerator;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Integer> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Integer.class);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long seckillVoucher(Long voucherId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 2. 执行lua脚本
        Integer result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), voucherId.toString(), userId.toString());
        if (result != 0) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, result == 1 ? "库存不足" : "已购买过该优惠券");
        }
        return idGenerator.nextId("order");
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