package com.three.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>配置文件映射</p>
 *	@author wangziqing
 */
@ConfigurationProperties(prefix = "chess")
public class Config {
    //配置模块
    @Autowired
    private Redis redis;
    @Autowired
    private Server server;

    @ConfigurationProperties(prefix = "chess.redis")
    public class Redis{//配置模块详细注入
        public String servers;
    }
    @ConfigurationProperties(prefix = "chess.buss")
    public class Server{
        public String key_1;
    }
}
