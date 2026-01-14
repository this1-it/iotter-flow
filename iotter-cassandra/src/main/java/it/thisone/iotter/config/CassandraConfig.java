package it.thisone.iotter.config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.DriverConfigLoaderBuilderConfigurer;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import it.thisone.iotter.cassandra.CassandraClient;
import it.thisone.iotter.cassandra.CassandraInitializator;
import it.thisone.iotter.exporter.CDataPoint;
import it.thisone.iotter.quartz.RollupCronJob;

@Configuration
@EnableCaching
@ComponentScan(basePackages = { "it.thisone.iotter.cassandra" })
//@EnableCassandraRepositories(basePackages = "it.thisone.iotter.cassandra.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {
	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);
	private static final String DATE_FORMAT = "dd/MM/yy HH:mm:ss.SSS ZZZ";

	@Autowired
	private Scheduler scheduler;

	@Override
	protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
		String clusterRole = System.getProperty(Constants.CLUSTER_ROLE, "master");
		boolean isMaster = clusterRole.trim().equalsIgnoreCase("master");
		if (!isMaster) {
			return Collections.emptyList();
		}
		Properties env = cassandraProperties();
		CreateKeyspaceSpecification keyspace = CreateKeyspaceSpecification
				.createKeyspace(env.getProperty("cassandra.keyspace"))
				.ifNotExists()
				.withSimpleReplication(Long.parseLong(env.getProperty("cassandra.replication_factor", "1")));
		List<CreateKeyspaceSpecification> specifications = new ArrayList<CreateKeyspaceSpecification>();
		specifications.add(keyspace);
		return specifications;
	}

	@PostConstruct
	public void init() {
		logger.info("CassandraConfig initialized.");

		Properties env = cassandraProperties();
		String rollUpCron = env.getProperty("cassandra.roll_up_cron", RollupCronJob.ROLL_UP_CRON);

		JobDetail job = JobBuilder
				.newJob(RollupCronJob.class)
				.withIdentity(RollupCronJob.ROLL_UP,
						RollupCronJob.ROLL_UP_GROUP).build();		

		if (rollUpCron.toUpperCase().contains("DISABLED")) {
			try {
				scheduler.deleteJob(job.getKey());
			} catch (SchedulerException e) {
			}
			logger.info("Rollup Job is disabled");
			return;
		}		
		
		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(RollupCronJob.ROLL_UP,
						RollupCronJob.ROLL_UP_GROUP)
				.withSchedule(
						CronScheduleBuilder
								.cronSchedule(rollUpCron))
				.build();
		Logger rollupLogger = LoggerFactory.getLogger(Constants.RollUp.ROLL_UP_LOG4J_CATEGORY);

		// schedule it
		try {
			if (!scheduler.isStarted()) {
				scheduler.start();
			}

			if (scheduler.checkExists(trigger.getKey())) {
				scheduler.rescheduleJob(trigger.getKey(), trigger);
				logger.info("Rollup Job rescheduled");
			}
			else {
				scheduler.scheduleJob(job, trigger);
				logger.info("Rollup Job scheduled");
			}
			
		} catch (SchedulerException e) {
			logger.error("Rollup job not started {}", e.getMessage());
			rollupLogger.error("Rollup job not started {}", e.getMessage());
		}

	}


	@Bean
	public SimpleDateFormat dateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf;
	}

	@Bean
	public CassandraInitializator cassandraInitializator() {
		return new CassandraInitializator();
	}


	@Bean
	@Qualifier("cassandraProperties")
	public Properties cassandraProperties() {
		Properties props = new Properties();
		try {
			// system property override
			String resourceName = System.getProperty("cassandra.properties", "cassandra.properties");
			Resource resource = new ClassPathResource(resourceName);
			if (!resource.exists()) {
				resource = new ClassPathResource("cassandra.default.properties");
			}
			props = PropertiesLoaderUtils.loadProperties(resource);
			
			String keySpace = props.getProperty("cassandra.keyspace");
			String replication = props.getProperty("cassandra.replication");
			String contactPoints = props.getProperty("cassandra.cluster");
			String port = props.getProperty("cassandra.native_transport_port", "9042");
			
			String rollUpCron = props.getProperty("cassandra.roll_up_cron", RollupCronJob.ROLL_UP_CRON);
			
			// system property override
			contactPoints = System.getProperty("cassandra.cluster", contactPoints);
			port = System.getProperty("cassandra.native_transport_port", port);
			keySpace = System.getProperty("cassandra.keyspace", keySpace);
			String[] nodes = StringUtils.split(contactPoints, ',');
			replication = String.format(replication, nodes.length);
			CassandraClient.KEYSPACE_NAME = keySpace;
			props.put("cassandra.cluster", contactPoints);
			props.put("cassandra.native_transport_port", port);
			props.put("cassandra.keyspace", keySpace);
			props.put("cassandra.replication", replication);
			props.put("cassandra.replication_factor", String.valueOf(nodes.length));
			props.put("cassandra.roll_up_cron", rollUpCron);
			
		} catch (Exception e) {
			logger.error("Cassandra Config reading properties", e);
		}
		return props;
	}

	@Override
	protected String getKeyspaceName() {
		Properties env = cassandraProperties();
		return env.getProperty("cassandra.keyspace");
	}

	@Override
	protected String getContactPoints() {
		return cassandraProperties().getProperty("cassandra.cluster");
	}

	@Override
	protected int getPort() {
		return Integer.parseInt(cassandraProperties().getProperty("cassandra.native_transport_port", "9042"));
	}

	@Override
	protected String getLocalDataCenter() {
		return cassandraProperties().getProperty("cassandra.local_datacenter", "dc1");
	}

	@Override
	protected DriverConfigLoaderBuilderConfigurer getDriverConfigLoaderBuilderConfigurer() {
		return builder -> {
			//builder.withClass(DefaultDriverOption.RETRY_POLICY_CLASS, CustomRetryPolicy.class);
			builder.withInt(DefaultDriverOption.CONNECTION_POOL_LOCAL_SIZE, 8);
			builder.withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, getLocalDataCenter());
		};
	}

    @Bean
	@Qualifier("cassandraExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(8);
        taskExecutor.setMaxPoolSize(128);
        //taskExecutor.setQueueCapacity(30);
        return taskExecutor;
    }
	

	@Bean(name = "cborMapper")
	public ObjectMapper cborMapper() {
		CBORFactory f = new CBORFactory();
		ObjectMapper mapper = new ObjectMapper(f);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		// ignore null fields globally
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);	
		return mapper;
	}
	@Bean(name = "dataPointListObjectReader")
	public ObjectReader dataPointListObjectReader() {
		return cborMapper().readerFor(new TypeReference<List<CDataPoint>>() {});

	}

    
//	@Bean
//	public CassandraMappingContext mappingContext() {
//		return new BasicCassandraMappingContext();
//	}
//
//	@Bean
//	public CassandraConverter converter() {
//		return new MappingCassandraConverter(mappingContext());
//	}
//
//	@Bean
//	public CassandraSessionFactoryBean session() throws Exception {
//		Properties env = cassandraProperties();
//		CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
//		session.setCluster(cluster().getObject());
//		session.setKeyspaceName(env.getProperty("cassandra.keyspace"));
//		session.setConverter(converter());
//		session.setSchemaAction(SchemaAction.NONE);
//		return session;
//	}
//	
//	@Bean
//	public CassandraOperations cassandraOperations() throws Exception {
//		return new CassandraTemplate(session().getObject());
//	}

}
