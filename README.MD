### Comom——公共模块

#### config

- 项目配置统一管理，application-xxx.yml作为唯一的配置文件，所有配置项在项目启动载入映射类——Config;

- dev——开发环境；test——测试环境；prod——生产环境；

  启动参数添加spring.profiles.active=xxx,加载特定环境的配置；

- Spring 可以加载Jar外的配置文件，部署项目后修改配置文件重新启动项目即可应用；

- 配置文件使用

  - 配置新增

     ```java
     1.新增配置项 
     2.Config配置类添加域，注@Value("${config.redis.servers}")引入配置值:
     ```

  - 项目使用

     ```java
       //1.引入特定配置模块 
     @Autowired
     private Config.Redis redisConfig;

       //2.全局上下文获取
     Context.config().XXX;
     ```


#### cache

### Commom-web——web公共模块

### SSO——用户模块

