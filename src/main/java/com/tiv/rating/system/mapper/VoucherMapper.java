package com.tiv.rating.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiv.rating.system.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description 针对表【voucher(优惠券表)】的数据库操作Mapper
 * @Entity com.tiv.rating.system.entity.Voucher
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVouchersOfShop(@Param("shopId") Long shopId);

}