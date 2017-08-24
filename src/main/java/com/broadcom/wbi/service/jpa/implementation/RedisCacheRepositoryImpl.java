package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
public class RedisCacheRepositoryImpl implements RedisCacheRepository<String, String> {

    @Autowired
    private RedisTemplate<String, String> template;

//	@Autowired
//	private RevisionSearchService revSearchServ;


    public void put(String key, String value) {
        if (!template.hasKey(key))
            template.opsForValue().set(key, value);
    }

    public void multiPut(Map<String, String> keyValues) {
        template.opsForValue().multiSet(keyValues);
    }

    public String get(String key) {
        return template.opsForValue().get(key);
    }

    public List<String> multiGet(Collection<String> keys) {
        return template.opsForValue().multiGet(keys);
    }

    public void delete(String key) {
        if (hasKey(key))
            template.delete(key);
    }

    public void setExpire(String key, long time) {
        if (template.getExpire(key).longValue() < 0)
            template.expire(key, time, TimeUnit.SECONDS);
    }

    public Boolean hasKey(String key) {
        return template.hasKey(key);
    }

    public void multiDelete(List<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                delete(key);
            }
        }
    }

    public RedisConnection getConnection() {
        return template.getConnectionFactory().getConnection();
    }

    public void multiDelete(String name) {
        if (name != null && !name.isEmpty()) {
            Set<byte[]> keys = template.getConnectionFactory().getConnection().keys(name.getBytes());
            Iterator<byte[]> it = keys.iterator();
            while (it.hasNext()) {
                byte[] data = (byte[]) it.next();
                String key = new String(data, 0, data.length);
                delete(key);
            }
        }
    }

    public List<String> searchKey(String name) {
        if (name != null && !name.isEmpty()) {
            List<String> ret = new ArrayList<String>();
            Set<byte[]> keys = template.getConnectionFactory().getConnection().keys(name.getBytes());
            Iterator<byte[]> it = keys.iterator();
            while (it.hasNext()) {
                byte[] data = (byte[]) it.next();
                ret.add(new String(data, 0, data.length));
            }
            if (ret.size() > 0)
                return ret;
        }
        return null;
    }

//	public void clearCache(int id, String key, String resetType){
//		if (!key.isEmpty()) {
//			delete(id + "_" + key);
//		} else {
//			multiDelete(id+"_*");
//			if(resetType.equalsIgnoreCase("program")){
//				List<RevisionSearch> rsl = revSearchServ.findByProgram(id);
//				if (rsl != null) {
//					String type = "";
//					String seg = "";
//					for (RevisionSearch rs : rsl) {
//						multiDelete(rs.getId()+"_*");
//						type = rs.getType().toLowerCase();
//						seg = rs.getSegment().toLowerCase();
//					}
//					if (!type.trim().isEmpty()) {
//						multiDelete(type+"_*");
//					}
//					if (!seg.trim().isEmpty()) {
//						multiDelete("*"+seg+"_*");
//					}
//				}
//			}
//
//		}
//	}
//
//	@Override
//	public void flushall() {
//		template.getConnectionFactory().getConnection().flushAll();
//		
//	}
}
