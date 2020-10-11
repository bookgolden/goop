package com.laoyang.product.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.util.HashMap;
import java.util.Map;


@EnableConfigurationProperties(CacheProperties.class)
@Configuration
@EnableCaching
public class RedisConfig {


    /**
     * Redisson单节点模式
     * @return
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
        return Redisson.create(config);
    }



    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }


    /**
     * 自定义Redis缓存管理器、并接管yml配置文件
     *
     * @param cacheProperties
     * @return
     */
    @Bean
    RedisCacheConfiguration redisCacheConfigurationInit(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }

        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }

        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }


    /**
     * 缓存管理器
     */
//    @Bean
//    CacheManager cacheManager(RedissonClient redissonClient) {
//        Map<String, CacheConfig> config = new HashMap<String, CacheConfig>();
//        // 创建一个名称为"testMap"的缓存，过期时间ttl为24分钟，同时最长空闲时maxIdleTime为12分钟。
//        config.put("testMap", new CacheConfig(24*60*1000, 12*60*1000));
//        return new RedissonSpringCacheManager(redissonClient, config);
//    }


    public static class Details{
        /**
         *将方法的运行结果进行缓存；以后再要相同的数据，直接从缓存中获取，不用调用方法；
         * CacheManager管理多个Cache组件的，对缓存的真正CRUD操作在Cache组件中，每一个缓存组件有自己唯一一个名字；
         *
         *
         * 原理：
         *   1、自动配置类；CacheAutoConfiguration
         *   2、缓存的配置类
         *          org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration   [Redis]
         *          org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration【默认】
         *   3、哪个配置类默认生效：SimpleCacheConfiguration；
         *   4、给容器中注册了一个CacheManager：ConcurrentMapCacheManager
         *   5、可以获取和创建ConcurrentMapCache类型的缓存组件；他的作用将数据保存在ConcurrentMap中；
         *
         *   运行流程：
         *   @Cacheable：
         *   1、方法运行之前，先去查询Cache（缓存组件），按照cacheNames指定的名字获取；
         *      （CacheManager先获取相应的缓存），第一次获取缓存如果没有Cache组件会自动创建。
         *   2、去Cache中查找缓存的内容，使用一个key，默认就是方法的参数；
         *      key是按照某种策略生成的；默认是使用keyGenerator生成的，默认使用SimpleKeyGenerator生成key；
         *          SimpleKeyGenerator生成key的默认策略；
         *                  如果没有参数；key=new SimpleKey()；
         *                  如果有一个参数：key=参数的值
         *                  如果有多个参数：key=new SimpleKey(params)；
         *   3、没有查到缓存就调用目标方法；
         *   4、将目标方法返回的结果，放进缓存中
         *
         *   @Cacheable标注的方法执行之前先来检查缓存中有没有这个数据，默认按照参数的值作为key去查询缓存，
         *   如果没有就运行方法并将结果放入缓存；以后再来调用就可以直接使用缓存中的数据；
         *
         *   核心：
         *      1）、使用CacheManager【ConcurrentMapCacheManager】按照名字得到Cache【ConcurrentMapCache】组件
         *      2）、key使用keyGenerator生成的，默认是SimpleKeyGenerator
         *
         *
         *   几个属性：
         *      cacheNames/value：指定缓存组件的名字;将方法的返回结果放在哪个缓存中，是数组的方式，可以指定多个缓存；
         *
         *      key：缓存数据使用的key；可以用它来指定。默认是使用方法参数的值  1-方法的返回值
         *              编写SpEL； #i d;参数id的值   #a0  #p0  #root.args[0]
         *              getEmp[2]
         *
         *      keyGenerator：key的生成器；可以自己指定key的生成器的组件id
         *              key/keyGenerator：二选一使用;
         *
         *      cacheManager：指定缓存管理器；或者cacheResolver指定获取解析器
         *
         *      condition：指定符合条件的情况下才缓存；
         *              ,condition = "#id>0"
         *          condition = "#a0>1"：第一个参数的值》1的时候才进行缓存
         *
         *      unless:否定缓存；当unless指定的条件为true，方法的返回值就不会被缓存；可以获取到结果进行判断
         *              unless = "#result == null"
         *              unless = "#a0==2":如果第一个参数的值是2，结果不缓存；
         *      sync：是否使用同步模式
         * @param id
         * @return
         *
         */
        @Cacheable(value = {"emp"},key = "#id "/*,keyGenerator = "MyKeyGenerator" condition = "#a0>1",unless = "#a0==2"*/)
        public Details getEmp(Integer id){
            return null;
        }

        /**
         * @CachePut：既调用方法，又更新缓存数据；同步更新缓存
         * 修改了数据库的某个数据，同时更新缓存；
         * 运行时机：
         *  1、先调用目标方法
         *  2、将目标方法的结果缓存起来
         *
         * 测试步骤：
         *  1、查询1号员工；查到的结果会放在缓存中；
         *          key：1  value：lastName：张三
         *  2、以后查询还是之前的结果
         *  3、更新1号员工；【lastName:zhangsan；gender:0】
         *          将方法的返回值也放进缓存了；
         *          key：传入的employee对象  值：返回的employee对象；
         *  4、查询1号员工？
         *      应该是更新后的员工；
         *          key = "#employee.id":使用传入的参数的员工id；
         *          key = "#result.id"：使用返回后的id
         *             @Cacheable的key是不能用#result
         *      为什么是没更新前的？【1号员工没有在缓存中更新】
         *
         */

        @CachePut(value = "emp",key = "#result.emp_id")
        public Details updateEmp(Details details){
            return details;
        }

        /**
         * @CacheEvict：缓存清除
         *  key：指定要清除的数据
         *  allEntries = true：指定清除这个缓存区域中所有的数据
         *  beforeInvocation = false：缓存的清除是否在方法之前执行
         *      默认代表缓存清除操作是在方法执行之后执行;如果出现异常缓存就不会清除
         *
         *  beforeInvocation = true：
         *      代表清除缓存操作是在方法运行之前执行，无论方法是否出现异常，缓存都清除

         */
        @CacheEvict(value="emp", key = "#id")
        public void deleteEmp(Integer id){
            System.out.println(id+"的记录被删除了");
        }



        /**
         *  @Caching 定义复杂的缓存规则
         *      可以同时定查询的cacheable
         *      更新的CachePut
         *      删除的CacheEvict
         * @param lastName
         * @return
         */
        @Caching(
                cacheable = {
                        @Cacheable(value="emp",key = "#lastName")
                },
                put = {
                        @CachePut(value="emp",key = "#lastName"),
                        @CachePut(value="emp",key = "#lastName")
                }
        )
        public Details getEmpByLastName(String lastName){
            return null;
        }


    }
}


