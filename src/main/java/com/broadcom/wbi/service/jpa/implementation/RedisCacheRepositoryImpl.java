package com.broadcom.wbi.service.jpa.implementation;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Component
public class RedisCacheRepositoryImpl implements RedisCacheRepository<String, String> {

    @Autowired
    private RedisTemplate<String, String> template;

    @Autowired
    private RevisionSearchService revisionSearchService;


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
        System.out.println("Deleting cache key " + key);
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

//    public void multiDelete(List<String> keys) {
//        if (keys != null && !keys.isEmpty()) {
//            for (String key : keys) {
//                delete(key);
//            }
//        }
//    }

    public RedisConnection getConnection() {
        return template.getConnectionFactory().getConnection();
    }

    public void deleteWildCard(String wildcard) {
        Set<String> keys = template.keys(wildcard);
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys)
                delete(key);
        }
    }

//    public void multiDelete(String name) {
//        if (name != null && !name.isEmpty()) {
//            Set<byte[]> keys = template.getConnectionFactory().getConnection().keys(name.getBytes());
//            Iterator<byte[]> it = keys.iterator();
//            while (it.hasNext()) {
//                byte[] data = (byte[]) it.next();
//                String key = new String(data, 0, data.length);
//                delete(key);
//            }
//
//            template.getConnectionFactory().getConnection().close();
//        }
//    }
//
//    public List<String> searchKey(String name) {
//        if (name != null && !name.isEmpty()) {
//            List<String> ret = new ArrayList<String>();
//            Set<byte[]> keys = template.getConnectionFactory().getConnection().keys(name.getBytes());
//            Iterator<byte[]> it = keys.iterator();
//            while (it.hasNext()) {
//                byte[] data = (byte[]) it.next();
//                ret.add(new String(data, 0, data.length));
//            }
//            template.getConnectionFactory().getConnection().close();
//            if (ret.size() > 0)
//                return ret;
//        }
//        return null;
//    }

    public void clearCache(int id, String key, String resetType) {
        try {
            if (!key.isEmpty()) {
                delete(id + "_" + key);
            } else {
                deleteWildCard(id + "_*");
                if (resetType.equalsIgnoreCase("program")) {
                    List<RevisionSearch> rsl = revisionSearchService.findByProgram(id);
                    if (rsl != null) {
                        String type = "";
                        String seg = "";
                        for (RevisionSearch rs : rsl) {
                            deleteWildCard(rs.getId() + "_*");
                            type = rs.getProgram_type().toLowerCase();
                            seg = rs.getSegment().toLowerCase();
                        }
                        if (!type.trim().isEmpty()) {
                            if (type.equalsIgnoreCase("software"))
                                type = "chip";
                            deleteWildCard(type + "_*");
                        }
                        if (!seg.trim().isEmpty()) {
                            deleteWildCard(seg + "_*");
                        }
                    }
                } else if (resetType.equalsIgnoreCase("revision")) {
                    RevisionSearch rs = revisionSearchService.findById(Integer.toString(id));
                    String type = "";
                    String seg = "";
                    if (rs != null) {
                        type = rs.getProgram_type().toLowerCase();
                        seg = rs.getSegment().toLowerCase();
                    }
                    if (!type.trim().isEmpty()) {
                        if (type.equalsIgnoreCase("software"))
                            type = "chip";
                        deleteWildCard(type + "_*");
                    }
                    if (!seg.trim().isEmpty()) {
                        deleteWildCard(seg + "_*");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//	@Override
//	public void flushall() {
//		template.getConnectionFactory().getConnection().flushAll();
//		
//	}
}
