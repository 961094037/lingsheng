package com.example.demo.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Redis工具类
 *
 * @author sairobo
 * @email tech@sairobo.com
 * @date 2017-07-17 21:12
 */
@Getter
@Component
public class RedisUtils {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ValueOperations<String, String> valueOperations;
    @Autowired
    private HashOperations<String, String, Object> hashOperations;
    @Autowired
    private ListOperations<String, Object> listOperations;
    @Autowired
    private SetOperations<String, Object> setOperations;
    @Autowired
    private ZSetOperations<String, Object> zSetOperations;
    /**
     * 默认过期时长，单位：秒 30天
     */
    public final static long DEFAULT_EXPIRE = 60 * 60 * 24 * 30;
    /**
     * 不设置过期时长
     */
    public final static long NOT_EXPIRE = -1;
    
    /**
     * @param key
     * @param value
     * @param expire 单位秒
     */
    public void set(String key, Object value, long expire) {
        valueOperations.set(key, toJson(value));
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }
    
    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE);
    }
    
    public <T> T get(String key, Class<T> clazz, long expire) {
        String value = valueOperations.get(key);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value == null ? null : fromJson(value, clazz);
    }
    
    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, NOT_EXPIRE);
    }
    
    public <T> T get(String key, TypeReference<T> type) {
        return JSON.parseObject(get(key), type);
    }
    
    public String get(String key, long expire) {
        String value = valueOperations.get(key);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return value;
    }
    
    public Set<String> getAllKeys(String prefix) {
        return redisTemplate.keys(prefix + "*");
    }
    
    public String get(String key) {
        return get(key, NOT_EXPIRE);
    }
    
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * Object转成JSON数据
     */
    private String toJson(Object object) {
        if (object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double || object instanceof Boolean || object instanceof String) {
            return String.valueOf(object);
        }
        return JSON.toJSONString(object);
    }
    
    /**
     * JSON数据，转成Object
     */
    private <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
    
    public Long getIncr(String key, long expire) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long increment = entityIdCounter.getAndIncrement();
        entityIdCounter.expire(expire, TimeUnit.SECONDS);
        return increment;
    }
    
    /**
     * 获取自增数
     *
     * @param key
     * @return
     */
    public Long getIncr(String key) {
        return getIncr(key, DEFAULT_EXPIRE);
    }
    
    /**
     * 设置自增数初始值
     *
     * @param key
     * @param value
     * @param expire
     */
    public void setIncr(String key, long value, long expire) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.set(value);
        counter.expire(expire, TimeUnit.SECONDS);
    }
    
    public void setIncr(String key, long value) {
        setIncr(key, value, DEFAULT_EXPIRE);
    }
    
    /**
     * 模糊匹配返回key集合
     *
     * @param pattern
     * @return
     */
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }
    
    /**
     * 根据keys批量删除
     *
     * @param keys
     * @return
     */
    public Long delete(Set<String> keys) {
        return redisTemplate.delete(keys);
    }
    
    
    // **************** 初始化--排除那些key避免所有缓存被一次性删除 **************** /
    private final static Set<String> EXCLUDE_KEY_PATTERN = new LinkedHashSet<>();
    
    static {
        EXCLUDE_KEY_PATTERN.add("*");
        EXCLUDE_KEY_PATTERN.add("");
    }
    
    /**
     * 模糊匹配key批量删除
     *
     * @param pattern
     * @return
     */
    public Long multiDeleteOne(String pattern) {
        // logger.info("thread name " + Thread.currentThread().getName());
        Assert.notBlank(pattern);
        if (!EXCLUDE_KEY_PATTERN.contains(pattern.trim())) {
            Set<String> keys = redisTemplate.keys(pattern);
            return redisTemplate.delete(keys);
        } else {
            logger.error("严重错误: redis的参数模糊匹配违规! ERROR:redisTemplate.delete() with the illegal key pattern !");
            return 0L;
        }
    }
    
    /**
     * 同步: 模糊匹配key批量删除
     *
     * @param patterns
     * @return
     */
    public Long multiDelete(String... patterns) {
        Assert.notNull(patterns, "redis的key匹配参数不能为空!");
        // 同步操作--批量清除个人历史列表redis缓存
        LongAdder longAdder = new LongAdder();
        Arrays.stream(patterns).forEach(pattern -> {
            longAdder.add(this.multiDeleteOne(pattern));
        });
        return longAdder.longValue();
    }
    
    /**
     * 异步: 模糊匹配key批量删除
     *
     * @param patterns
     * @return
     */
    public void asyncMultiDelete(String... patterns) {
        ThreadPoolUtil.getPool().execute(() -> {
            this.multiDelete(patterns);
        });
    }
    
    
    public void setHashOperations(String key, String field, Object object) {
        hashOperations.put(key, field, JSON.toJSONString(object));
    }
    
    
    public <T> T getHashOperations(String key, String field, TypeReference<T> type) {
        Object o = hashOperations.get(key, field);
        return JSON.parseObject(String.valueOf(o), type);
    }
    
    /**
     * 获取剩余过期时间单位为：秒
     *
     * @param key
     * @return
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
    
    /**
     * 获取剩余过期时间单位自定义
     *
     * @param key
     * @param timeUnit
     * @return
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }
    
    public void setList(String key, Integer index, Object value, long expire) {
        listOperations.set(key, index, toJson(value));
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }
    
    public void leftPush(String key, Object value, long expire) {
        listOperations.leftPush(key, toJson(value));
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }
    
    public void rightPush(String key, Object value, long expire) {
        listOperations.rightPush(key, toJson(value));
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }
    
    public List getList(String key) {
        return listOperations.range(key, 0, listOperations.size(key));
    }
    
    
    public <T> List<T> getList(String key, Class<T> clazz) {
        List sl = listOperations.range(key, 0, listOperations.size(key));
        List<T> list = new ArrayList<T>();
        if (null != sl && sl.size() > 0) {
            String s = null;
            for (Object obj : sl) {
                s = (String) obj;
                list.add(JSON.parseObject(s, clazz));
            }
        }
        return list;
    }
    
    
    /**
     * setnx 功能
     *
     * @param key
     * @param value
     * @param expire 单位秒
     */
    public Boolean setIfAbsent(String key, Object value, long expire) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
        return result;
    }
    
    /**
     * exists 功能
     *
     * @param key
     */
    public Boolean exists(String key) {
        Long count = redisTemplate.countExistingKeys(Lists.newArrayList(key));
        return count > 0 ? true : false;
    }
    
    public void setSet(String key, Object value, long expire) {
        setOperations.add(key, toJson(value));
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }
    
    public <T> List<T> getSet2List(String key, Class<T> clazz) {
        Set<Object> members = setOperations.members(key);
        List<T> list = new ArrayList<T>();
        String s;
        if (CollectionUtil.isNotEmpty(members)) {
            s = null;
            for (Object obj : members) {
                s = (String) obj;
                list.add(JSON.parseObject(s, clazz));
            }
        }
        return list;
    }
}
