package it.thisone.iotter.config;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.thisone.iotter.quartz.HealthCheckJob;

@EnableAsync
@Configuration
@ComponentScan({ "it.thisone.iotter.integration",
	"it.thisone.iotter.provisioning"})
public class IntegrationConfig implements AsyncConfigurer {

	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

//	@Autowired
//	private EventBus eventBus;

	
	@Autowired
	private Scheduler scheduler;


	@Override
	public Executor getAsyncExecutor() {
		// AsyncExecutionInterceptor
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public <T> Future<T> submit(Callable<T> task) {
				return super.submit(task);
			}

		};
		
		/*
		 * keepAliveTime is used/effective "only" when threads in the pool has more than core size. 
		 * This is not applicable when threads in the pool is below core size
		 */
		executor.setCorePoolSize(Constants.AsyncExecutor.CORE_POOL_SIZE);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setMaxPoolSize(Constants.AsyncExecutor.MAX_POOL_SIZE);
		executor.setQueueCapacity(Constants.AsyncExecutor.QUEUE_CAPACITY);
		executor.setThreadNamePrefix(Constants.AsyncExecutor.THREAD_NAME_PREFIX);
		// executor.setThreadPriority(5);
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}

	
	@Bean
	public VelocityEngine velocityEngine() throws VelocityException, IOException {
		
		/*
		 * 
		 * http://stackoverflow.com/questions/1432468/how-to-use-string-as-velocity-template
		 */
		
		Properties props = new Properties();
		props.put("resource.loader", "class");
		props.put("class.resource.loader.class",
				  "org.apache.velocity.runtime.resource.loader." +
				  "ClasspathResourceLoader");
		props.put("input.encoding", "UTF-8");
		props.put("output.encoding", "UTF-8");
		VelocityEngine engine = new VelocityEngine(props);
		engine.init();
		return engine;
	}
	
	@Bean
	public JavaMailSender mailSender() {
		Properties properties = javaMailProperties();
		
		if (properties.getProperty("mail.smtp.host", "DISABLED").equalsIgnoreCase("DISABLED")) {
			logger.info("JavaMailSender is DISABLED");
			return new JavaMailSenderMock();
		}
		
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setJavaMailProperties(javaMailProperties());

		String username = properties.getProperty("mail.smtp.username");
		String password = properties.getProperty("mail.smtp.password");
		mailSender.setUsername(username);
		mailSender.setPassword(password);
		Logger logger = LoggerFactory.getLogger(Constants.Notifications.LOG4J_JAVAMAIL);
		Log4JavaMail log4JavaMail = new Log4JavaMail(logger, "utf-8");
		mailSender.getSession().setDebugOut(log4JavaMail);
		mailSender.getSession().setDebug(true);
		
		return mailSender;
	}

	@Bean
	@Qualifier("javaMailProperties")
	public Properties javaMailProperties() {
		Properties properties = null;
		try {
			ClassPathResource resource = new ClassPathResource(
					"smtp.properties");
			if (!resource.exists()) {
				resource = new ClassPathResource("smtp.default.properties");
			}
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (Exception e) {
			logger.error("unable to retrieve smtp.properties", e);
		}
		return properties;
	}

	@Bean
	@Qualifier("mailMessages")
	public Properties mailMessages() {
//		Properties properties = null;
//		try {
//			ClassPathResource resource = new ClassPathResource(
//					"mail_messages.xml");
//			if (!resource.exists()) {
//				resource = new ClassPathResource("mail_messages.default.xml");
//			}
//			properties = PropertiesLoaderUtils.loadProperties(resource);
//		} catch (Exception e) {
//			logger.error("unable to retrieve mail_messages.xml", e);
//		}

		Properties properties = null;
		try {
			ClassPathResource resource = new ClassPathResource(
					"mail_messages.properties");
			if (!resource.exists()) {
				resource = new ClassPathResource("mail_messages.default.properties");
			}
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (Exception e) {
			logger.error("unable to retrieve mail_messages.xml", e);
		}

		return properties;
	}

	@Bean(name = "mapper")
	@Primary
	public ObjectMapper mapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		return mapper;
	}

	@Bean(name = "gzipMapper")
	public ObjectMapper gzipMapper() {
		return mapper();
	}

	@PostConstruct
	public void init() {
		String clusterRole = System.getProperty(Constants.CLUSTER_ROLE,
				"master");

		logger.info("IntegrationConfig initialized {}.", clusterRole);

		JobDetail job = JobBuilder
				.newJob(HealthCheckJob.class)
				.withIdentity(HealthCheckJob.HEALTH_CHECK,
						HealthCheckJob.HEALTH_CHECK_GROUP).build();

		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(HealthCheckJob.HEALTH_CHECK,
						HealthCheckJob.HEALTH_CHECK_GROUP)
				.withSchedule(
						CronScheduleBuilder
								.cronSchedule(QuartzConfig.QUARTZ_CRON_EVERY_5_MINUTES))
				.build();

		// schedule it
		try {
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
			if (scheduler.checkExists(trigger.getKey())) {
				scheduler.rescheduleJob(trigger.getKey(), trigger);
				logger.info("HealthCheck Job rescheduled");
			}
			else {
				scheduler.scheduleJob(job, trigger);
				logger.info("HealthCheck Job scheduled");
			}


		} catch (SchedulerException e) {
			logger.error("HealthCheck job not started", e);
		}

	}
	

    @Bean(name = "bootstrapProperties")
	public Properties bootstrapProperties() {
		Properties props = new Properties();
		try {
			String resourceName = System.getProperty("bootstrap.properties",
					"bootstrap.properties");
			Resource resource = new ClassPathResource(resourceName);
			if (!resource.exists()) {
				resource = new ClassPathResource("bootstrap.default.properties");
			}
			props = PropertiesLoaderUtils.loadProperties(resource);
		} catch (Exception e) {
		}
		return props;
	}


}
