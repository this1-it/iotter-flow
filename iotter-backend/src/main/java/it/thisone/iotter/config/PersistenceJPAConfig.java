package it.thisone.iotter.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.thisone.iotter.persistence.service.EmptyDbInitializator;

/*
 * 
 * 
 * https://wiki.eclipse.org/EclipseLink/Examples/JPA/Logging
 * https://www.eclipse.org/forums/index.php/t/796147/
 * https://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Caching/Coordination
 * https://wiki.eclipse.org/EclipseLink/Examples/JPA/CustomSequencing
 * https://www.eclipse.org/forums/index.php/t/796147/
 * 
 * https://blogs.oracle.com/arungupta/entry/why_java_ee_6_is
 */

@Configuration
@EnableCaching
@EnableTransactionManagement(order = 10)
@ComponentScan({ "it.thisone.iotter.persistence" })
@EnableJpaRepositories(basePackages = "it.thisone.iotter.persistence.repository")
public class PersistenceJPAConfig {

	private static final String JAVA_COMP_ENV = "java:comp/env/";

	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	public PersistenceJPAConfig() {
		super();
	}

	@PostConstruct
	public void init() {
		logger.info("PersistenceJPAConfig initialized.");
	}

	// beans

	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
		Properties props = persistenceProperties();

		final LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName(props.getProperty("persistence.unit", "iotter"));
		factoryBean.setDataSource(dataSource());

		factoryBean.setPackagesToScan(new String[] { "it.thisone.iotter.persistence.model" });
		final EclipseLinkJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
		factoryBean.setJpaVendorAdapter(vendorAdapter);
		factoryBean.setJpaProperties(additionalProperties());
		return factoryBean;
	}

	@Bean
	@Qualifier("JPA")
	public DataSource dataSource() {
		Properties env = persistenceProperties();
		// This uses JNDI, you could create the data source in any way you want
		logger.info("Configuring JPA DataSource with properties: {}", env);

		String jndiName = env.getProperty("jpa.datasource");
		try {
			return lookUpDataSource(jndiName);
		} catch (Throwable e) {
			String message = String.format("dataSource not found %s", jndiName);
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
	}

	@Bean
	public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	final Properties additionalProperties() {
		final Properties properties = new Properties();
		// properties.put("eclipselink.logging.level", "FINE");
		// properties.put("eclipselink.ddl-generation",
		// "drop-and-create-tables");
		// properties.put("eclipselink.ddl-generation",
		// "create-or-extend-tables");
		// properties.put("eclipselink.ddl-generation", "none");

		String clusterRole = System.getProperty(Constants.CLUSTER_ROLE, "master");

		boolean isMaster = clusterRole.trim().equalsIgnoreCase("master");
		Properties env = persistenceProperties();

		String ddlGeneration = env.getProperty(PersistenceUnitProperties.DDL_GENERATION);
		if (ddlGeneration != null && isMaster) {

			logger.info("Configuring JPA DataSource for DDL generation: {} ", ddlGeneration);

			properties.put(PersistenceUnitProperties.DDL_GENERATION, ddlGeneration);
			properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE,
					PersistenceUnitProperties.DDL_DATABASE_GENERATION);
		}
		String loggingLevel = env.getProperty(PersistenceUnitProperties.LOGGING_LEVEL);
		if (loggingLevel != null) {
			properties.put(PersistenceUnitProperties.LOGGING_LEVEL, loggingLevel);
		}

		// String cacheShared = env
		// .getProperty(PersistenceUnitProperties.CACHE_SHARED_DEFAULT);
		// if (cacheShared != null) {
		// properties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT,
		// cacheShared);
		// }

		properties.put(PersistenceUnitProperties.BATCH_WRITING, "JDBC");
		properties.put(PersistenceUnitProperties.WEAVING, "false");

		properties.put("eclipselink.logging.level.cache", SessionLog.FINER_LABEL);

		String cache = env.getProperty(Constants.AMQ.CACHE_COORDINATION, "");
		if (cache.equalsIgnoreCase("jms")) {
			properties.put("eclipselink.cache.coordination.protocol", "jms");
			properties.put("eclipselink.cache.coordination.naming-service", "jms");
			properties.put("eclipselink.cache.coordination.jms.topic",
					JAVA_COMP_ENV + Constants.AMQ.JMS_PERSISTENCE_TOPIC);
			properties.put("eclipselink.cache.coordination.jms.factory",
					JAVA_COMP_ENV + Constants.AMQ.JMS_CONNECTION_FACTORY);

			properties.put("eclipselink.cache.coordination.jms.reuse-topic-publisher", "true");
		}

		properties.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, "it.thisone.iotter.config.UUIDSequence");

		return properties;
	}

	private DataSource lookUpDataSource(String jndiName) throws NamingException, SQLException {
		logger.info("lookUp DataSource {}", jndiName);
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup(JAVA_COMP_ENV);
		DataSource datasource = (DataSource) envCtx.lookup(jndiName);
		Connection connection = datasource.getConnection();
		connection.close();
		return datasource;
	}

	private Properties persistenceProperties() {
		Properties props = new Properties();
		try {
			String resourceName = System.getProperty("persistence.properties", "persistence.properties");
			Resource resource = new ClassPathResource(resourceName);

			if (!resource.exists()) {
				resource = new ClassPathResource("persistence.default.properties");
			}
			props = PropertiesLoaderUtils.loadProperties(resource);
			String jpa = System.getProperty("jpa.datasource", props.getProperty("jpa.datasource"));
			props.put("jpa.datasource", jpa);

		} catch (Exception e) {
			logger.error("Persistence Config reading properties", e);
		}
		return props;
	}

}
