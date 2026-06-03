package com.tiv.rating.system.util;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class SimpleRedisLock implements Lock {

    private String key;

    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "lock:";

    private static final String THREAD_ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    @Override
    public boolean tryLock(Long timeout) {
        String threadId = THREAD_ID_PREFIX + Thread.currentThread().getId();
        return BooleanUtil.isTrue(stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + key, threadId, timeout, TimeUnit.SECONDS));
    }

    @Override
    public void unlock() {
        String threadId = THREAD_ID_PREFIX + Thread.currentThread().getId();
        String lockThreadId = stringRedisTemplate.opsForValue().get(KEY_PREFIX + key);
        if(threadId.equals(lockThreadId)) {
            // 释放锁
            stringRedisTemplate.delete(KEY_PREFIX + key);
        }

    }

}
