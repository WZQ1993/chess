package com.three.cache;

import redis.clients.jedis.BasicCommands;
import redis.clients.jedis.BinaryJedisClusterCommands;
import redis.clients.jedis.JedisCommands;

/**
 * @author summerao
 *
 */
public interface RedisClusterInterface extends JedisCommands,BasicCommands,BinaryJedisClusterCommands {

}
