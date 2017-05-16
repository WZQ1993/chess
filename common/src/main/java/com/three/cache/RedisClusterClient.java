package com.three.cache;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.three.config.Config;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.util.SafeEncoder;

/**
 * <p>redis封装</p>
 *
 * @author wangziqing
 */
@Component
public class RedisClusterClient implements InitializingBean {
    private JedisCluster jedisCluster;

    @Autowired
    private Config.Redis redisConfig;

    private int maxTotal = 1000;//可用连接实例的最大数目，默认值为1000；如果赋值为-1，则表示不限制；

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    private int maxWait = 1000;//等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    private static final String EX = "EX";

    private static final String NX = "NX";


    /**
     * 批量存HASH
     *
     * @param key
     * @param hash
     * @return
     */
    public String hmset(String key, Map<String, String> hash) {
        return this.jedisCluster.hmset(key, hash);
    }

    /**
     * 存HASH
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public long hset(String key, String field, String value) {
        return this.jedisCluster.hset(key, field, value);
    }

    /**
     * 存set 多个
     *
     * @param key
     * @return
     */
    public long sAdds(String key, String[] value) {
        if (value == null || value.length == 0) {
            return 0;
        }
        return this.jedisCluster.sadd(key, value);
    }

    /**
     * 存set
     *
     * @param key
     * @return
     */
    public long sAdd(String key, String value) {
        return this.jedisCluster.sadd(key, value);
    }

    /**
     * 是否集合成员
     *
     * @param key
     * @return
     */
    public boolean sIsMember(String key, String member) {
        return this.jedisCluster.sismember(key, member);
    }

    /**
     * 返回集合中的所有成员
     *
     * @param key
     * @return
     */
    public Set<String> sMembers(String key) {
        return this.jedisCluster.smembers(key);
    }

    /**
     * 获取排序列表(倒序)
     * 0-a 倒序
     * -a -> 0 正序
     *
     * @param key
     * @return
     */
    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        return this.jedisCluster.zrevrangeWithScores(key, start, end);
    }

    /**
     * key  是否存在
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return this.jedisCluster.exists(key);
    }

    /**
     * 添加排序列表
     *
     * @param key
     * @return
     */
    public double zincrby(String key, double score, String member) {
        return this.jedisCluster.zincrby(key, score, member);
    }

    /**
     * 获取score
     *
     * @param key
     * @return
     */
    public Double zscore(String key, String member) {
        return this.jedisCluster.zscore(key, member);
    }

    /**
     * 获取set大小
     *
     * @param key
     * @return
     */
    public long sCard(String key) {
        return this.jedisCluster.scard(key);
    }

    /**
     * 设置超时缓存
     */
    public void setex(String key, Object t, int seconds) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(t);
            bytes = bo.toByteArray();
            bo.close();
            oo.close();
            this.jedisCluster.setex(key.getBytes(), seconds, bytes);
        } catch (Exception e) {

        }
    }

    public byte[] get(byte[] key) {
        return this.jedisCluster.get(key);
    }

    /**
     * 获取HASH
     *
     * @param key
     * @param field
     * @return
     */
    public String hget(String key, String field) {
        return this.jedisCluster.hget(key, field);
    }

    /**
     * 批量获取HASH
     *
     * @param key
     * @return
     */
    public Map<String, String> hgetAll(String key) {
        return this.jedisCluster.hgetAll(key);
    }

    /**
     * 原子增量
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public long hincrBy(String key, String field, long value) {
        return this.jedisCluster.hincrBy(key, field, value);
    }


    /**
     * 删除hash
     *
     * @param key
     * @param field
     * @return
     */
    public long hdel(String key, String field) {
        return this.jedisCluster.hdel(key, field);
    }

    /**
     * 原子增量
     *
     * @param key
     * @param addvalue
     * @return
     */
    public long incrBy(String key, long addvalue) {
        return this.jedisCluster.incrBy(key, addvalue);
    }

    /**
     * 原子增量 加设置失效时间 ,如果不存在用def的值保存
     *
     * @param key
     * @param addvalue
     * @return
     */
    public long incrBy(String key, long addvalue, long def, int expire) {
        if (1 == this.jedisCluster.expire(key, expire)) {
            return this.jedisCluster.incrBy(key, addvalue);
        } else {
            return this.jedisCluster.incrBy(key, def);
        }
    }

    /**
     * 原子增量 如果不存在用def的值保存
     *
     * @param key
     * @param addvalue
     * @return
     */
    public long incrBy(String key, long addvalue, long def) {
        if (true == this.jedisCluster.exists(key)) {
            return this.jedisCluster.incrBy(key, addvalue);
        } else {
            return this.jedisCluster.incrBy(key, def);
        }
    }

    /**
     * get
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return this.jedisCluster.get(key);
    }

    /**
     * del
     *
     * @param key
     * @return
     */
    public boolean del(String key) {
        long res = this.jedisCluster.del(key);
        return res == 1 ? true : false;
    }

    /**
     * 超时set 已存在值不会覆盖
     *
     * @param key
     * @param data
     * @param time 超时时间
     * @return
     */
    public String setExpx(String key, String data, int time) {
        return this.jedisCluster.set(key, data, NX, EX, time);
    }

    /**
     * 超时set 已存在值会覆盖
     *
     * @param key
     * @param data
     * @param time 超时时间
     * @return
     */
    public String setEx4Str(String key, String data, int time) {
        return this.jedisCluster.setex(key, time, data);
    }

    /**
     * 取缓存内key对应的值
     *
     * @param <T>
     */
    public <T> T getT(String key, Class<T> clazz) {
        try {
            byte[] bytes = this.jedisCluster.get(key.getBytes());
            if (bytes == null) {
                return null;
            }
            return JSON.parseObject(SafeEncoder.encode(bytes), clazz);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 设置超时缓存
     */
    public void setex(String key, byte[] value, int seconds) {
        this.jedisCluster.setex(key.getBytes(), seconds, value);
    }


    /**
     * 设置 无超时缓存
     */
    public void set(String key, String value) {
        this.jedisCluster.set(key, value);
    }

    public void setex(byte[] key, byte[] value, int seconds) {
        this.jedisCluster.setex(key, seconds, value);
    }


    /**
     * 设置值并设置超时时间
     *
     * @param key
     * @param data
     * @param time 超时时间
     * @return
     */

    public boolean setnx(String key, String data, int time) {
        Long setnx = this.jedisCluster.setnx(key, data);
        if (setnx == 1) {
            this.jedisCluster.expire(key, time);
            return true;
        }
        return setnx == 1 ? true : false;
    }


    public long rpush(byte[] key, byte[] value) {
        return this.jedisCluster.rpush(key, value);
    }

    public long rpush(String key, String... val) {
        return this.jedisCluster.rpush(key, val);
    }


    public long llen(byte[] key) {
        return this.jedisCluster.llen(key);
    }


    public String ltrim(byte[] key, long start, long end) {
        return this.jedisCluster.ltrim(key, start, end);
    }


    public byte[] lpop(byte[] key) {
        return this.jedisCluster.lpop(key);
    }

    public byte[] rpop(byte[] key) {
        return this.jedisCluster.rpop(key);
    }


    public long rpush(byte[] key, byte[] value, JedisPool jedisPool) {
        return this.jedisCluster.rpush(key, value);
    }


    public byte[] lpop(byte[] key, JedisPool jedisPool) {
        return this.jedisCluster.lpop(key);
    }


    public byte[] rpop(byte[] key, JedisPool jedisPool) {
        return this.jedisCluster.rpop(key);
    }


    public List<byte[]> lrange(byte[] key, JedisPool jedisPool) {
        return this.jedisCluster.lrange(key, 0, -1);
    }


    public List<byte[]> lrange(byte[] key, long start, long end) {
        return this.jedisCluster.lrange(key, start, end);
    }


    public String ltrim(byte[] key, JedisPool jedisPool) {
        return this.jedisCluster.ltrim(key, 0, 0);
    }

    /**
     * 为键设置超时时间
     *
     * @param key
     * @param seconds
     * @return
     */

    public long expire(String key, int seconds) {
        return this.jedisCluster.expire(key, seconds);
    }

    /**
     * 入队列
     *
     * @param key
     * @return
     */

    public long lpush(String key, String... val) {
        return this.jedisCluster.lpush(key, val);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        //Jedis Cluster will attempt to discover cluster nodes automatically
        String[] servers = redisConfig.servers.split(";|,");
        for (String str : servers) {
            String[] ap = str.split(":");
            HostAndPort hp = new HostAndPort(ap[0], Integer.valueOf(ap[1]));
            jedisClusterNodes.add(hp);
        }
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(maxTotal); //可用连接实例的最大数目，默认值为8；如果赋值为-1，则表示不限制；
        config.setMaxIdle(100); //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值是8。
        config.setMinIdle(10); //控制一个pool最少有多少个状态为idle(空闲的)的jedis实例，默认值是0。
        config.setMaxWaitMillis(maxWait);//等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        jedisCluster = new JedisCluster(jedisClusterNodes, config);
    }

    public RedisClusterInterface getJedis() {
        RedisClusterInterface service = (RedisClusterInterface) creatProxyInstance();
        return service;
    }

    public Object creatProxyInstance() {
        return Proxy.newProxyInstance(RedisClusterInterface.class.getClassLoader(), new Class[]{RedisClusterInterface.class}, new ProxyFactory(jedisCluster));
    }

    class ProxyFactory implements InvocationHandler {
        private JedisCluster jedisCluster;

        public ProxyFactory(JedisCluster jedisCluster) {
            this.jedisCluster = jedisCluster;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if (method.getName() == "toString") {
                return this.toString();
            }
            Object result = null;
            try {
                result = method.invoke(jedisCluster, args);
            } catch (Exception e) {
                throw e;
            }
            return result;
        }
    }
}
