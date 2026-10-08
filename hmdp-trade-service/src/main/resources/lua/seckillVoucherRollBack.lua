-- Redis 5 之前脚本默认按原样复制，含 TIME 这类非确定性命令时必须在写命令前启用效果复制（Redis 5+ 已是默认行为）
if redis.replicate_commands then redis.replicate_commands() end
local stockKey = KEYS[1]
local seckillUserKey = KEYS[2]
local traceLogKey = KEYS[3]
local voucherId = ARGV[1]
local userId = (ARGV[2])
local orderId = ARGV[3]
local seckillVoucherOrderOperate = tonumber(ARGV[4])
local traceId = ARGV[5]
local logType = ARGV[6]
local beforeQty = tonumber(ARGV[7])
local changeQty = tonumber(ARGV[8])
local afterQty = tonumber(ARGV[9])
local stock = redis.call('get', stockKey);
-- 幂等：库存 key 可能已被前一次回滚失效（删除即代表"已回滚"，下次从 DB 重载），
-- 此时不能再视为失败，否则批量丢弃会引发大量虚假"回滚最终失败"与告警风暴
if stock then
    redis.call('del', stockKey)
end
if seckillVoucherOrderOperate == 1 then
    if (redis.call('sismember', seckillUserKey, userId) == 1) then
        redis.call('srem', seckillUserKey, userId)
    end
end
local timeArr = redis.call('TIME')
local nowMillis = tonumber(timeArr[1]) * 1000 + math.floor(tonumber(timeArr[2]) / 1000)
local logEntry = cjson.encode({
    logType = logType,
    ts = nowMillis,
    orderId = orderId,
    traceId = traceId,
    userId = userId,
    voucherId = voucherId,
    beforeQty = beforeQty,
    changeQty = changeQty,
    afterQty = afterQty
})
redis.call('hset', traceLogKey, traceId, logEntry)
return 0
