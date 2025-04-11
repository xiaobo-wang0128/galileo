package org.armada.galileo.auto_code.util.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.*;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;
import redis.clients.jedis.util.SafeEncoder;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

//@Component
public class JedisUtil {

    private AtomicBoolean hasInit = new AtomicBoolean(false);

    private Logger log = LoggerFactory.getLogger(JedisUtil.class);

    private JedisPool pool = null;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.database}")
    private Integer database;

    @PostConstruct
    public void init() {

        if (!hasInit.compareAndSet(false, true)) {
            return;
        }
        GenericObjectPoolConfig<Jedis> poolCfg = new GenericObjectPoolConfig<Jedis>();
        poolCfg.setMinIdle(5);
        poolCfg.setMaxIdle(10);
        poolCfg.setMaxTotal(100);
        poolCfg.setMaxWaitMillis(10000);
        poolCfg.setTestOnBorrow(false);
        poolCfg.setMinEvictableIdleTimeMillis(10000);
        poolCfg.setTestWhileIdle(false);

        try {
            if (CommonUtil.isEmpty(password)) {
                pool = new JedisPool(poolCfg, host, Integer.valueOf(port), 10000);
            } else {
                pool = new JedisPool(poolCfg, host, Integer.valueOf(port), (int) poolCfg.getMaxWaitMillis(), password, database, false);
            }

            Jedis j = pool.getResource();

            j.ping();
            j.close();

            log.info("[redis] redis 连接正常 host:{}, port:{}, password:{}", host, port, password);
        } catch (Exception e) {
            log.error("redis 连接失败 host:{}, port:{}, password:{}", host, port, password);
            log.error(e.getMessage(), e);
            throw new BizException("[reids]操作异常");
        }

    }

    public String set(CacheType cacheType, String key, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();

            // log.info("[redis] set redisKey: " + redisKey + ", value:" + value);

            return j.set(redisKey, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }


//
//    public String set(CacheType cacheType, String key, String value, String nxxx, String expx, long time) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            j.set
//            return j.set(redisKey, value, nxxx, expx, time);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//
//    }

//    public String set(CacheType cacheType, String key, String value, String nxxx) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            return j.set(redisKey, value, nxxx);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//    }

    public JedisPool getPool() {
        return pool;
    }

    public String get(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();

            String v = j.get(redisKey);
            // log.info("[redis] get redisKey: " + redisKey + ", value:" + v);

            return v;

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Boolean exists(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.exists(redisKey);
        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long persist(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.persist(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String type(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.type(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long expire(CacheType cacheType, String key, int seconds) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.expire(redisKey, seconds);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long expire(String key, long seconds) {
        Jedis j = null;
        try {
            j = pool.getResource();

            return j.expire(key, seconds);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long pexpire(CacheType cacheType, String key, long milliseconds) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.pexpire(redisKey, milliseconds);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }

    public Long expireAt(CacheType cacheType, String key, long unixTime) {
        Jedis j = null;
        try {
            j = pool.getResource();
            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.expireAt(redisKey, unixTime);
        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }

    public Object eval(String script, List<String> keys, List<String> args) {
        Jedis j = null;
        try {
            j = pool.getResource();
            Object eval = j.eval(script, keys, args);
            return eval;
        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }

    public Long pexpireAt(CacheType cacheType, String key, long millisecondsTimestamp) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.pexpireAt(redisKey, millisecondsTimestamp);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long ttl(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.ttl(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long pttl(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();
            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.pttl(redisKey);
        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }

    public Boolean setbit(CacheType cacheType, String key, long offset, boolean value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.setbit(redisKey, offset, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

//    public Boolean setbit(CacheType cacheType, String key, long offset, String value) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            return j.setbit(redisKey, offset, value);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//
//    }

    public Boolean getbit(CacheType cacheType, String key, long offset) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.getbit(redisKey, offset);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long setrange(CacheType cacheType, String key, long offset, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.setrange(redisKey, offset, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String getrange(CacheType cacheType, String key, long startOffset, long endOffset) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.getrange(redisKey, startOffset, endOffset);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String getSet(CacheType cacheType, String key, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.getSet(redisKey, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long setnx(CacheType cacheType, String key, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.setnx(redisKey, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }


    public static enum ExtendCommand implements ProtocolCommand {

        SETNXEX;
        private final byte[] raw;

        ExtendCommand() {
            raw = SafeEncoder.encode(name());
        }

        @Override
        public byte[] getRaw() {
            return raw;
        }
    }

    public boolean setnxex(CacheType cacheType, String key, String value, long expire) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();

            log.info("[redis] setnxex key: {}, value: {}, expire:{} ", redisKey, value, expire);

            j.getClient().sendCommand(
                    Protocol.Command.SET,
                    SafeEncoder.encode(redisKey),
                    SafeEncoder.encode(value),
                    SafeEncoder.encode("ex"),
                    Protocol.toByteArray(expire),
                    SafeEncoder.encode("nx"));

            String codeRely = j.getClient().getStatusCodeReply();

            if ("OK".equals(codeRely)) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String setex(CacheType cacheType, String key, int seconds, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.setex(redisKey, seconds, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String psetex(CacheType cacheType, String key, long milliseconds, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.psetex(redisKey, milliseconds, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long decrBy(CacheType cacheType, String key, long integer) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.decrBy(redisKey, integer);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long decr(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.decr(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long incrBy(CacheType cacheType, String key, long integer) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.incrBy(redisKey, integer);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Double incrByFloat(CacheType cacheType, String key, double value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.incrByFloat(redisKey, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long incr(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.incr(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long append(CacheType cacheType, String key, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.append(redisKey, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String substr(CacheType cacheType, String key, int start, int end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.substr(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long hset(CacheType cacheType, String key, String field, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hset(redisKey, field, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String hget(CacheType cacheType, String key, String field) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hget(redisKey, field);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long hsetnx(CacheType cacheType, String key, String field, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hsetnx(redisKey, field, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String hmset(CacheType cacheType, String key, Map<String, String> hash) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hmset(redisKey, hash);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<String> hmget(CacheType cacheType, String key, String... fields) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hmget(redisKey, fields);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long hincrBy(CacheType cacheType, String key, String field, long value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hincrBy(redisKey, field, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Double hincrByFloat(CacheType cacheType, String key, String field, double value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hincrByFloat(redisKey, field, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Boolean hexists(CacheType cacheType, String key, String field) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hexists(redisKey, field);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long hdel(CacheType cacheType, String key, String... field) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hdel(redisKey, field);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long hlen(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hlen(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> hkeys(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hkeys(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<String> hvals(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hvals(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Map<String, String> hgetAll(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hgetAll(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long rpush(CacheType cacheType, String key, String... string) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.rpush(redisKey, string);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long lpush(CacheType cacheType, String key, String... string) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.lpush(redisKey, string);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long llen(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.llen(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<String> lrange(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.lrange(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String ltrim(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.ltrim(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String lindex(CacheType cacheType, String key, long index) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.lindex(redisKey, index);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String lset(CacheType cacheType, String key, long index, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.lset(redisKey, index, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long lrem(CacheType cacheType, String key, long count, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.lrem(redisKey, count, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String lpop(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.lpop(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String rpop(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.rpop(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long sadd(CacheType cacheType, String key, String... member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.sadd(redisKey, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> smembers(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.smembers(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long srem(CacheType cacheType, String key, String... member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.srem(redisKey, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String spop(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.spop(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> spop(CacheType cacheType, String key, long count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.spop(redisKey, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long scard(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.scard(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Boolean sismember(CacheType cacheType, String key, String member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.sismember(redisKey, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String srandmember(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.srandmember(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<String> srandmember(CacheType cacheType, String key, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.srandmember(redisKey, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long strlen(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.strlen(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zadd(CacheType cacheType, String key, double score, String member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zadd(redisKey, score, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zadd(CacheType cacheType, String key, double score, String member, ZAddParams params) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zadd(redisKey, score, member, params);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zadd(CacheType cacheType, String key, Map<String, Double> scoreMembers) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zadd(redisKey, scoreMembers);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zadd(CacheType cacheType, String key, Map<String, Double> scoreMembers, ZAddParams params) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zadd(redisKey, scoreMembers, params);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrange(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrange(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zrem(CacheType cacheType, String key, String... member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrem(redisKey, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Double zincrby(CacheType cacheType, String key, double score, String member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zincrby(redisKey, score, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Double zincrby(CacheType cacheType, String key, double score, String member, ZIncrByParams params) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zincrby(redisKey, score, member, params);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zrank(CacheType cacheType, String key, String member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrank(redisKey, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zrevrank(CacheType cacheType, String key, String member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrank(redisKey, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrevrange(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrange(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrangeWithScores(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeWithScores(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrevrangeWithScores(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeWithScores(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zcard(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zcard(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Double zscore(CacheType cacheType, String key, String member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zscore(redisKey, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<String> sort(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.sort(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<String> sort(CacheType cacheType, String key, SortingParams sortingParameters) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.sort(redisKey, sortingParameters);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zcount(CacheType cacheType, String key, double min, double max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zcount(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zcount(CacheType cacheType, String key, String min, String max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zcount(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrangeByScore(CacheType cacheType, String key, double min, double max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScore(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrangeByScore(CacheType cacheType, String key, String min, String max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScore(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrevrangeByScore(CacheType cacheType, String key, double max, double min) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScore(redisKey, max, min);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrangeByScore(CacheType cacheType, String key, double min, double max, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScore(redisKey, min, max, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrevrangeByScore(CacheType cacheType, String key, String max, String min) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScore(redisKey, max, min);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrangeByScore(CacheType cacheType, String key, String min, String max, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScore(redisKey, min, max, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrevrangeByScore(CacheType cacheType, String key, double max, double min, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScore(redisKey, max, min, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrangeByScoreWithScores(CacheType cacheType, String key, double min, double max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScoreWithScores(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrevrangeByScoreWithScores(CacheType cacheType, String key, double max, double min) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScoreWithScores(redisKey, max, min);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrangeByScoreWithScores(CacheType cacheType, String key, double min, double max, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScoreWithScores(redisKey, min, max, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrevrangeByScore(CacheType cacheType, String key, String max, String min, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScore(redisKey, max, min, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrangeByScoreWithScores(CacheType cacheType, String key, String min, String max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScoreWithScores(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrevrangeByScoreWithScores(CacheType cacheType, String key, String max, String min) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScoreWithScores(redisKey, max, min);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrangeByScoreWithScores(CacheType cacheType, String key, String min, String max, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByScoreWithScores(redisKey, min, max, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrevrangeByScoreWithScores(CacheType cacheType, String key, double max, double min, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScoreWithScores(redisKey, max, min, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<Tuple> zrevrangeByScoreWithScores(CacheType cacheType, String key, String max, String min, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByScoreWithScores(redisKey, max, min, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zremrangeByRank(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zremrangeByRank(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zremrangeByScore(CacheType cacheType, String key, double start, double end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zremrangeByScore(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zremrangeByScore(CacheType cacheType, String key, String start, String end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zremrangeByScore(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zlexcount(CacheType cacheType, String key, String min, String max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zlexcount(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrangeByLex(CacheType cacheType, String key, String min, String max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByLex(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrangeByLex(CacheType cacheType, String key, String min, String max, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrangeByLex(redisKey, min, max, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrevrangeByLex(CacheType cacheType, String key, String max, String min) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByLex(redisKey, max, min);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Set<String> zrevrangeByLex(CacheType cacheType, String key, String max, String min, int offset, int count) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zrevrangeByLex(redisKey, max, min, offset, count);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long zremrangeByLex(CacheType cacheType, String key, String min, String max) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zremrangeByLex(redisKey, min, max);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long linsert(CacheType cacheType, String key, ListPosition where, String pivot, String value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();

            return j.linsert(redisKey, where, pivot, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }

    public Long lpushx(CacheType cacheType, String key, String... string) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.lpushx(redisKey, string);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long rpushx(CacheType cacheType, String key, String... string) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.rpushx(redisKey, string);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

//    public List<String> blpop(CacheType cacheType, String key) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            return j.blpop(redisKey);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//
//    }

    public List<String> blpop(CacheType cacheType, int timeout, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.blpop(timeout, redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }
//
//    public List<String> brpop(CacheType cacheType, String key) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            return j.brpop(redisKey);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//    }

    public List<String> brpop(CacheType cacheType, int timeout, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.brpop(timeout, redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long del(CacheType cacheType, String subKLey) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(subKLey).toString();
            return j.del(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long del(String redisKey) {
        Jedis j = null;
        try {
            j = pool.getResource();

            return j.del(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public String echo(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.echo(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long move(CacheType cacheType, String key, int dbIndex) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.move(redisKey, dbIndex);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long bitcount(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.bitcount(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long bitcount(CacheType cacheType, String key, long start, long end) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.bitcount(redisKey, start, end);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long bitpos(CacheType cacheType, String key, boolean value) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.bitpos(redisKey, value);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long bitpos(CacheType cacheType, String key, boolean value, BitPosParams params) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.bitpos(redisKey, value, params);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

//    public ScanResult<Entry<String, String>> hscan(CacheType cacheType, String key, int cursor) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            return j.hscan(redisKey, cursor);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//
//    }
//
//    public ScanResult<String> sscan(CacheType cacheType, String key, int cursor) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            return j.sscan(redisKey, cursor);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//
//    }

//    public ScanResult<Tuple> zscan(CacheType cacheType, String key, int cursor) {
//        Jedis j = null;
//        try {
//            j = pool.getResource();
//             if(CommonUtil.isNotEmpty(password)){ j.auth(password);}
//            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
//            return j.zscan(redisKey, cursor);
//
//        } catch (Exception e) {
//            log.error("[reids]操作异常", e);
//            throw new BizException("[reids]操作异常");
//        } finally {
//            if (j != null) {
//                j.close();
//            }
//        }
//
//    }

    public ScanResult<Entry<String, String>> hscan(CacheType cacheType, String key, String cursor) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hscan(redisKey, cursor);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public ScanResult<Entry<String, String>> hscan(CacheType cacheType, String key, String cursor, ScanParams params) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.hscan(redisKey, cursor, params);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public ScanResult<String> sscan(CacheType cacheType, String key, String cursor) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.sscan(redisKey, cursor);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public ScanResult<String> sscan(CacheType cacheType, String key, String cursor, ScanParams params) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.sscan(redisKey, cursor, params);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public ScanResult<Tuple> zscan(CacheType cacheType, String key, String cursor) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zscan(redisKey, cursor);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public ScanResult<Tuple> zscan(CacheType cacheType, String key, String cursor, ScanParams params) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.zscan(redisKey, cursor, params);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long pfadd(CacheType cacheType, String key, String... elements) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.pfadd(redisKey, elements);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public long pfcount(CacheType cacheType, String key) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.pfcount(redisKey);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }

    public Long geoadd(CacheType cacheType, String key, double longitude, double latitude, String member) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.geoadd(redisKey, longitude, latitude, member);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Long geoadd(CacheType cacheType, String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.geoadd(redisKey, memberCoordinateMap);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Double geodist(CacheType cacheType, String key, String member1, String member2) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.geodist(redisKey, member1, member2);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public Double geodist(CacheType cacheType, String key, String member1, String member2, GeoUnit unit) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.geodist(redisKey, member1, member2, unit);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<String> geohash(CacheType cacheType, String key, String... members) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.geohash(redisKey, members);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<GeoCoordinate> geopos(CacheType cacheType, String key, String... members) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.geopos(redisKey, members);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<GeoRadiusResponse> georadius(CacheType cacheType, String key, double longitude, double latitude, double radius, GeoUnit unit) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.georadius(redisKey, longitude, latitude, radius, unit);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<GeoRadiusResponse> georadius(CacheType cacheType, String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.georadius(redisKey, longitude, latitude, radius, unit, param);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<GeoRadiusResponse> georadiusByMember(CacheType cacheType, String key, String member, double radius, GeoUnit unit) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.georadiusByMember(redisKey, member, radius, unit);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<GeoRadiusResponse> georadiusByMember(CacheType cacheType, String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.georadiusByMember(redisKey, member, radius, unit, param);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }

    }

    public List<Long> bitfield(CacheType cacheType, String key, String... arguments) {
        Jedis j = null;
        try {
            j = pool.getResource();

            String redisKey = new StringBuilder(cacheType.toString()).append("_").append(key).toString();
            return j.bitfield(redisKey, arguments);

        } catch (Exception e) {
            log.error("[reids]操作异常", e);
            throw new BizException("[reids]操作异常");
        } finally {
            if (j != null) {
                j.close();
            }
        }
    }

    public Integer getInt(CacheType cacheType, String key) {
        String v = get(cacheType, key);
        if (v == null) {
            return null;
        }
        return Integer.valueOf(v);
    }


}
