package com.three.context;

import com.three.config.Config;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>全局上下文</p>
 * Created by wangziqing on 2017/5/17 0017.
 */
public class Context implements ApplicationContextAware{
    // Spring应用上下文环境
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Context.applicationContext=applicationContext;
    }
    public static ApplicationContext springContext(){
        return applicationContext;
    }
    //静态方法使用配置文件
    public static Config config(){
        return applicationContext.getBean(Config.class);
    }
}
