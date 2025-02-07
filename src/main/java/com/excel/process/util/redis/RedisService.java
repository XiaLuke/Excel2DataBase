package com.excel.process.util.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnBean(RedisTemplate.class)
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long DEFAULT_TIME = 1800L;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_TIME, TimeUnit.SECONDS);
    }

    public void set(String key, Object value, long time, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, time, unit);
    }

    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }

    public Boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }
}
