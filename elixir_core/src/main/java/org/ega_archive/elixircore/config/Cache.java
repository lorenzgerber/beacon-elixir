package org.ega_archive.elixircore.config;

import com.google.common.cache.CacheBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class Cache implements CachingConfigurer {
  
  @Value("${service.cache.timeout}")
  private int cacheTimeout;
  
  @Value("${service.cache.size}")
  private int cacheSize;

  @Bean
  @Override
  public CacheManager cacheManager() {
    //Cache for saving the services urls
    GuavaCacheManager cacheManager = new GuavaCacheManager("serviceURL");
    cacheManager.setCacheBuilder(CacheBuilder.newBuilder().expireAfterWrite(cacheTimeout, TimeUnit.MINUTES)
                                     .maximumSize(cacheSize));
    return cacheManager;

    // SimpleCacheManager cacheManager = new SimpleCacheManager();
    // cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("serviceURL")));
    // return cacheManager;
  }

  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new SimpleKeyGenerator();
  }

}
