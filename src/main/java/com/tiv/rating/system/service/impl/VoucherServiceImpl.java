package com.tiv.rating.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.entity.Voucher;
import com.tiv.rating.system.entity.VoucherSeckill;
import com.tiv.rating.system.mapper.VoucherMapper;
import com.tiv.rating.system.service.VoucherSeckillService;
import com.tiv.rating.system.service.VoucherService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private VoucherSeckillService voucherSeckillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillVoucher(Voucher voucher) {
        this.save(voucher);
        VoucherSeckill voucherSeckill = VoucherSeckill.builder()
                .voucherId(voucher.getId())
                .stock(voucher.getStock())
                .beginTime(voucher.getBeginTime())
                .endTime(voucher.getEndTime())
                .build();
        voucherSeckillService.save(voucherSeckill);
        stringRedisTemplate.opsForValue()
                .set(String.format("%s_%s", RedisConstants.VOUCHER_SECKILL_STOCK, voucher.getId()), String.valueOf(voucher.getStock()), RedisConstants.VOUCHER_SECKILL_STOCK_TTL);
    }

    @Override
    public List<Voucher> queryVouchersOfShop(Long shopId) {
        return getBaseMapper().queryVouchersOfShop(shopId);
    }

}