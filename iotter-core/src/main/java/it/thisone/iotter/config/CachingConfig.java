package it.thisone.iotter.config;

import jakarta.annotation.PostConstruct;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;


@Configuration
@EnableCaching
public class CachingConfig {
	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	@PostConstruct
	public void init() {
		logger.debug("CachingConfig initialized.");
	}

	@Bean
	public JCacheCacheManager cacheManager() {
		return new JCacheCacheManager(ehCacheManager());
	}

	@Bean(destroyMethod = "close")
	public CacheManager ehCacheManager() {
		CachingProvider provider = Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
		CacheManager cacheManager = provider.getCacheManager();

		createCache(cacheManager, "unit_of_measure", 255, 43200, 86400);
		createCache(cacheManager, "unit_of_measure_code", 255, 43200, 86400);
		createCache(cacheManager, "role", 10, 43200, 86400);
		createCache(cacheManager, "device", 4000, 43200, 86400);
		createCache(cacheManager, "modbus_registers", 1000, 43200, 86400);
		createCache(cacheManager, "datasink", 4000, 300, 500);
		createCache(cacheManager, "data_values", 400, 300, 900);
		createCache(cacheManager, "ticks", 500, 300, 900);
		createCache(cacheManager, "timestamps", 20, 300, 900);
		createCache(cacheManager, "messages", 200000, 43200, 86400);

		return cacheManager;
	}

	private void createCache(CacheManager cacheManager, String name, int maxEntries, int ttlIdleSeconds, int ttlSeconds) {
		CacheConfigurationBuilder<Object, Object> config = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Object.class, Object.class,
						ResourcePoolsBuilder.newResourcePoolsBuilder().heap(maxEntries, EntryUnit.ENTRIES))
				.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ttlSeconds)));

		cacheManager.createCache(name,
				Eh107Configuration.fromEhcacheCacheConfiguration(config.build()));
	}
}
