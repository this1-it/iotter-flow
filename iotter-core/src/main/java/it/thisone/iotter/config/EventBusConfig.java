package it.thisone.iotter.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.google.common.eventbus.EventBus;

@Configuration
@ComponentScan({ "it.thisone.iotter.eventbus"})
public class EventBusConfig {
	
	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);
	
	@PostConstruct
	public void init() {
		logger.info("EventBusConfig initialized.");
	}
	
	@Bean
	public EventBus eventBus() {
		EventBus eventBus = new EventBus();
		return eventBus;
	}
	
	
//
//	@Bean
//	public AsyncEventBus asyncEventBus() {
//		// Bug #377 Threads are leaking in Tomcat
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(100);
//        executor.setAllowCoreThreadTimeOut(true);
//        executor.setThreadNamePrefix("AsyncExecutor-");
//        executor.initialize();
//		AsyncEventBus asyncEventBus = new AsyncEventBus(executor);
//		return asyncEventBus;
//	}
//	
//	
//	@Bean
//	@DependsOn({ "eventBus", "asyncEventBus" })
//	public EventBusWrapper eventBusWrapper() {
//		EventBusWrapper eventBusWrapper = new EventBusWrapper();
//		return eventBusWrapper;
//	}
	
}