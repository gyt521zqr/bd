package com.magao.backend.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类
 * <p>
 * 封装Redis常用的操作方法，提供简洁的API：
 * 1. String操作（set/get/delete/expire）
 * 2. Hash操作（hSet/hGet/hGetAll/hDelete）
 * 3. List操作（lPush/lRange/lRem）
 * 4. Set操作（sAdd/sMembers/sRemove）
 * 5. 通用操作（expire/ttl/exists/incr）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    /** Redis模板 */
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== String操作 ====================

    /**
     * 设置缓存（永久有效）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置缓存（仅当key不存在时，原子操作）
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true表示设置成功（key不存在），false表示key已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        return result != null ? result : false;
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存（带类型转换）
     *
     * @param key  缓存键
     * @param clazz 目标类型
     * @param <T>  泛型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     * @return 删除的数量
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存键
     * @return true表示存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * @param key     缓存键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     *
     * @param key 缓存键
     * @return 剩余过期时间（秒），-1表示永久有效，-2表示不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 自增操作
     *
     * @param key 缓存键
     * @return 自增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增操作（指定增量）
     *
     * @param key   缓存键
     * @param delta 增量值
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ==================== Hash操作 ====================

    /**
     * 设置Hash字段
     *
     * @param key     Hash键
     * @param hashKey Hash字段键
     * @param value   Hash字段值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 获取Hash字段值
     *
     * @param key     Hash键
     * @param hashKey Hash字段键
     * @return Hash字段值
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 获取Hash所有字段
     *
     * @param key Hash键
     * @return Hash所有键值对
     */
    public java.util.Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除Hash字段
     *
     * @param key      Hash键
     * @param hashKeys Hash字段键数组
     * @return 删除的数量
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    // ==================== List操作 ====================

    /**
     * 从列表左侧推入元素
     *
     * @param key   列表键
     * @param value 元素值
     * @return 列表长度
     */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 获取列表指定范围的元素
     *
     * @param key   列表键
     * @param start 起始索引
     * @param end   结束索引
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ==================== Set操作 ====================

    /**
     * 添加元素到Set集合
     *
     * @param key    Set键
     * @param values 元素数组
     * @return 添加的数量
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取Set集合所有元素
     *
     * @param key Set键
     * @return 元素集合
     */
    public java.util.Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 从Set集合移除元素
     *
     * @param key    Set键
     * @param values 元素数组
     * @return 移除的数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }
}
