package com.tiv.rating.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.entity.VoucherOrder;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.VoucherOrderMapper;
import com.tiv.rating.system.service.SeckillVoucherService;
import com.tiv.rating.system.service.VoucherOrderService;
import com.tiv.rating.system.util.IdGenerator;
import com.tiv.rating.system.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private IdGenerator idGenerator;

    @Resource
    private RedissonClient redissonClient;

    private VoucherOrderService proxy;

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private static final DefaultRedisScript<Integer> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Integer.class);
    }

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long seckillVoucher(Long voucherId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser().getId();

        // 2. 生成订单id
        long orderId = idGenerator.nextId("order");

        // 3. 执行秒杀lua脚本
        int result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId), RedisConstants.QUEUE_NAME);
        if (result != 0) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, result == 1 ? "库存不足" : "已购买过该优惠券");
        }

        // 4. 初始化代理对象
        proxy = (VoucherOrderService) AopContext.currentProxy();
        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
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
        // 3. 保存订单
        save(voucherOrder);
    }

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 1. 获取redis消息队列中订单信息
                    List<MapRecord<String, Object, Object>> read = stringRedisTemplate.opsForStream().read(Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(RedisConstants.QUEUE_NAME, ReadOffset.lastConsumed()));
                    if (CollectionUtil.isEmpty(read)) {
                        continue;
                    }
                    // 2. 解析订单
                    MapRecord<String, Object, Object> record = read.get(0);
                    Map<Object, Object> map = record.getValue();
                    VoucherOrder order = BeanUtil.fillBeanWithMap(map, new VoucherOrder(), true);
                    // 3. 处理订单
                    handleVoucherOrder(order);
                    // 4. 确认消息处理
                    stringRedisTemplate.opsForStream().acknowledge(RedisConstants.QUEUE_NAME, "g1", record.getId());
                } catch (Exception e) {
                    handlePendingList();
                }
            }
        }

    }

    private void handleVoucherOrder(VoucherOrder order) {
        Long userId = order.getUserId();
        // 1. 获取锁
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        if (!lock.tryLock()) {
            log.error("用户 {} 订单处理获取锁失败", userId);
            return;
        }

        try {
            // 2. 保存订单
            proxy.createVoucherOrder(order);
        } finally {
            lock.unlock();
        }
    }

    private void handlePendingList() {
        while (true) {
            try {
                // 1. 获取pending list中订单信息
                List<MapRecord<String, Object, Object>> read = stringRedisTemplate.opsForStream().read(Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(RedisConstants.QUEUE_NAME, ReadOffset.from("0")));
                if (CollectionUtil.isEmpty(read)) {
                    break;
                }
                // 2. 解析订单
                MapRecord<String, Object, Object> record = read.get(0);
                Map<Object, Object> map = record.getValue();
                // 3. 确认消息处理
                stringRedisTemplate.opsForStream().acknowledge(RedisConstants.QUEUE_NAME, "g1", record.getId());
                // 4. 处理订单
                VoucherOrder order = BeanUtil.fillBeanWithMap(map, new VoucherOrder(), true);
                handleVoucherOrder(order);
            } catch (Exception e) {
                log.error("pending list 订单处理失败", e);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

}