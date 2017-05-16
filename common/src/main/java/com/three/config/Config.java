package com.three.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>配置文件映射</p>
 *	@author wangziqing
 */
@Component
public class Config {
    //配置模块
    @Autowired
    private Redis redis;
    @Autowired
    private Server server;

    @Component
    public class Redis{//配置模块详细注入
        @Value("${config.redis.servers}")
        public String servers;
    }
    @Component
    public class Server{
        @Value("${config.server.key_1}")
        public String key_1;
    }
}
