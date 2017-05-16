package com.three.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *	<p>redis 分布式锁简单实现,
 *		后续改为redisson</p>
 *	@author wangziqing
 */
@Component
public class RedisLocker implements Locker {
	private Logger logger = LoggerFactory.getLogger(RedisLocker.class);
	@Autowired
	private RedisClusterClient redisClient; 
	private static final String LOCK_KEY_PREX="redis_lock_key_prex:";
	@Override
	public boolean obtainLock(String lockKey,int seconds) {
		if(seconds>0){
			boolean result = getLock(lockKey,seconds);
			if (result) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	private boolean getLock(String lockKey,int seconds) {
		return redisClient.setnx(LOCK_KEY_PREX + lockKey, "1", seconds);
	}

	/**
	 * 释放锁,请确保此方法最终被调用.
	 * 
	 * @param lockKey
	 * @return
	 */
	@Override
	public boolean releaseLock(String lockKey) {

		boolean result = redisClient.del(LOCK_KEY_PREX + lockKey);
		if (result) {
			logger.info("release an distribut lock with key:{}", lockKey);
		} else {
			logger.info("release an distribut lock with key:{} encounter an error", lockKey);
		}
		return result;
	}
}
