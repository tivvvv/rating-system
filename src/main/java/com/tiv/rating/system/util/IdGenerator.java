package com.tiv.rating.system.util;

import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 全局ID生成器
 */
@Component
public class IdGenerator {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 开始时间戳
     */
    private static final long BEGIN_TIMESTAMP = 1778136326L;

    /**
     * 序列号的位数
     */
    private static final int COUNT_BITS = 32;

    public long nextId(String keyPrefix) {
        // 1. 生成时间戳
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;

        // 2. 生成序列号
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment(keyPrefix + ":" + date);
        if (count == null) {
            throw new BusinessException(BusinessCodeEnum.SYSTEM_ERROR, "自增序列号失败");
        }

        // 3. 拼接并返回
        return timestamp << COUNT_BITS | count;
    }

}