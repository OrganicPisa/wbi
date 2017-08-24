package com.broadcom.wbi.service.jpa;

import org.springframework.data.redis.connection.RedisConnection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RedisCacheRepository<K, V> {
    void put(K key, V value);

    void multiPut(Map<K, V> map);

    V get(K key);

    List<V> multiGet(Collection<K> keylist);

    void delete(K key);

    void setExpire(K key, long ts);

    Boolean hasKey(K key);

    void multiDelete(List<K> keylist);

    void multiDelete(K name);

    List<String> searchKey(K name);

//    void clearCache(int id, String key, String resetType);

    RedisConnection getConnection();

}
