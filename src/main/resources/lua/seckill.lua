-- 1. 参数列表
-- 优惠券id
local voucherId = ARGC[1]
-- 用户id
local userId = ARGV[2]

-- 2. 数据key
-- 库存key
local stockKey = 'seckill_voucher_stock' .. voucherId
-- 订单key
local orderKey = 'seckill_voucher_order' .. voucherId

-- 3. 业务脚本
-- 判断库存是否足够
if (tonumber(redis.call('get', stockKey)) <= 0) then
    return 1
end
-- 判断用户是否重复下单
if (redis.call('sismember', orderKey, userId) == 1) then
    return 2;
end
-- 扣减库存
redis.call('incrby', stockKey, -1)
-- 下单
redis.call('add', orderKey, userId)
return 0