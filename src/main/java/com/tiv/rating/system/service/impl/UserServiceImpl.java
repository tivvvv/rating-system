package com.tiv.rating.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.rating.system.common.CommonConstants;
import com.tiv.rating.system.common.RedisConstants;
import com.tiv.rating.system.dto.LoginDTO;
import com.tiv.rating.system.dto.UserDTO;
import com.tiv.rating.system.entity.User;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.mapper.UserMapper;
import com.tiv.rating.system.service.UserService;
import com.tiv.rating.system.util.RegexUtils;
import com.tiv.rating.system.util.ResultUtils;
import com.tiv.rating.system.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendCode(String phone) {
        // 1. 校验手机号格式
        if (!RegexUtils.isPhoneValid(phone)) {
            ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, "手机号格式错误");
        }

        // 2. 生成验证码
        String code = RandomUtil.randomString(6);

        // 3. 保存验证码到redis中
        stringRedisTemplate.opsForValue().set(String.format("%s_%s", RedisConstants.LOGIN_CODE, phone), code, 10, TimeUnit.MINUTES);

        // 4. 发送验证码
        log.debug("sendCode--手机号:{},验证码:{}", phone, code);
    }

    @Override
    public String login(LoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        // 1. 校验手机号格式
        if (!RegexUtils.isPhoneValid(phone)) {
            ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, "手机号格式错误");
        }

        // 2. 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(String.format("%s_%s", RedisConstants.LOGIN_CODE, phone));
        if (cacheCode == null || !cacheCode.equals(loginDTO.getCode())) {
            ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, "验证码错误");
        }

        // 3. 根据手机号查询用户
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 4. 不存在则创建用户
            user = createUserWithPhone(phone);
        }

        // 5. 生成随机token作为登录令牌
        String token = UUID.randomUUID().toString(true);

        // 6. 保存用户信息到redis中,以hash结构
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userDTOMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(String.format("%s_%s", RedisConstants.LOGIN_TOKEN, token), userDTOMap);
        stringRedisTemplate.expire(String.format("%s_%s", RedisConstants.LOGIN_TOKEN, token), RedisConstants.LOGIN_TOKEN_TTL + RandomUtil.randomInt(RedisConstants.LOGIN_TOKEN_TTL), TimeUnit.MINUTES);

        // 7. 返回token
        return token;
    }

    @Override
    public void sign() {
        // 1. 获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 2. 获取当前日期
        LocalDateTime now = LocalDateTime.now();
        String signKey = RedisConstants.SIGN_KEY + userId + now.format(DateTimeFormatter.ofPattern(":yyyyMM"));

        // 3. 获取现在是当前月的第几天
        int dayOfMonth = now.getDayOfMonth();

        // 4. 写入redis bitmap
        stringRedisTemplate.opsForValue().setBit(signKey, dayOfMonth - 1, true);
    }

    @Override
    public Integer signCount() {
        // 1. 获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 2. 获取当前日期
        LocalDateTime now = LocalDateTime.now();
        String signKey = RedisConstants.SIGN_KEY + userId + now.format(DateTimeFormatter.ofPattern(":yyyyMM"));

        // 3. 获取现在是当前月的第几天
        int dayOfMonth = now.getDayOfMonth();

        // 4. 获取本月签到记录,结果为十进制数字
        List<Long> list = stringRedisTemplate.opsForValue()
                .bitField(signKey,
                        BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (CollectionUtil.isEmpty(list)) {
            return 0;
        }
        Long result = list.get(0);
        if (result == null || result == 0) {
            return 0;
        }
        // 5. 计算连续签到天数
        int count = 0;
        // 最后一位是0说明签到中断
        while ((result & 1) != 0) {
            count++;
            // 数字右移一位
            result >>>= 1;
        }
        return count;
    }

    private User createUserWithPhone(String phone) {
        User user = User.builder()
                .phone(phone)
                .nickname(CommonConstants.NICKNAME_PREFIX + RandomUtil.randomString(10))
                .build();
        save(user);
        return user;
    }

}




