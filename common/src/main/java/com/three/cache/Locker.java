package com.three.cache;

public interface Locker {
	/**
	 * 请确保lockKey是唯一值
	 * 同时为确保锁key最终被clear,设置了1分钟失效时间。
	 * @param lockKey
	 * @return true : 获取锁成功 false：失败
	 */
	 boolean obtainLock(String lockKey,int seconds);
	
	/**
	 * 释放锁,请确保此方法最终被调用.
	 * @param lockKey
	 * @return
	 */
	 boolean releaseLock(String lockKey);
}
