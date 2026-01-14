package it.thisone.iotter.config;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * http://xkcb.blogspot.it/2013/08/how-to-use-quartz-22-with-spring-32x-on.html
 * 
 * @author tisone
 *
 */

@Configuration
@ComponentScan({ "it.thisone.iotter.quartz" })
public class QuartzConfig {

	public static final String QUARTZ_SCHEDULER = "quartz-scheduler";
	public static final String QUARTZ_CRON_EVERY_10_SECONDS = "0/10 * * * * ?";
	public static final String QUARTZ_CRON_EVERY_30_SECONDS = "0/30 * * * * ?";
	public static final String QUARTZ_CRON_EVERY_5_MINUTES = "0 0/5 * * * ?";
	public static final String QUARTZ_CRON_EVERY_1_MINUTES = "0 0/1 * 1/1 * ? *";
	public static final String QUARTZ_CRON_EVERY_10_MINUTES = "0 0/10 * 1/1 * ? *";
	public static final String QUARTZ_CRON_EVERY_15_MINUTES = "0 0/15 * * * ?";
	public static final String QUARTZ_CRON_EVERY_30_MINUTES = "0 0/30 * * * ?";

	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		logger.info("QuartzConfig initialized.");
	}

	/*
	 * http://candidjava.com/tutorial/quartz-reuse-existing-data-source-connection-pool/
	 */
	@Bean
	public SchedulerFactoryBean quartzSchedulerFactory() {
		Properties props = quartzProperties();
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setAutoStartup(true);
		// custom job factory of spring with DI support for @Autowired !
		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		schedulerFactory.setJobFactory(jobFactory);
		schedulerFactory.setQuartzProperties(props);
		return schedulerFactory;
	}
	
	
	private DataSource lookUpDataSource(String jndiName) throws NamingException, SQLException {
		logger.info("lookUp DataSource {}", jndiName);
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env/");
		DataSource datasource = (DataSource) envCtx.lookup(jndiName);
		Connection connection = datasource.getConnection();
		connection.close();
		return datasource;
	}
	
	@Bean
	public Properties quartzProperties() {
		ClassPathResource resource = new ClassPathResource("quartz.properties");
		if (!resource.exists()) {
			resource = new ClassPathResource("quartz.default.properties");
		}
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(resource);
		Properties properties = null;
		try {
			propertiesFactoryBean.afterPropertiesSet();
			properties = propertiesFactoryBean.getObject();
		} catch (IOException e) {
			logger.error("Cannot load quartz.properties.");
		}

		return properties;
	}
}
