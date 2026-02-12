package it.thisone.iotter.config;

import java.util.UUID;

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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import it.thisone.iotter.backend.ClientUserService;
import it.thisone.iotter.cassandra.CassandraConfortOnDemand;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.quartz.ConfortOnDemandJob;
import it.thisone.iotter.rest.CODClientEndpoint;


@Configuration
@ComponentScan(basePackageClasses = {CODClientEndpoint.class})
public class CODConfig extends Initializator {


	public static final String CONFORT_ON_DEMAND_LOG4J_CATEGORY = "confortOnDemand";
	public static final String ADMIN_NAME = "Cod";
	public static final String NETWORK_NAME = ADMIN_NAME;
	
	private static Logger logger = LoggerFactory.getLogger(CONFORT_ON_DEMAND_LOG4J_CATEGORY);

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ClientUserService clientUserService;
	
	
	@Autowired
	private CassandraConfortOnDemand cassandra;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String clusterRole = System.getProperty(Constants.CLUSTER_ROLE, "master");
		boolean isMaster = clusterRole.trim().equalsIgnoreCase("master");

		if (isMaster) {
			User administrator = new User();
			administrator.setUsername(ADMIN_NAME);
			administrator.setPassword(UUID.randomUUID().toString());
			administrator.setFirstName(ADMIN_NAME);
			administrator.setLastName("Admin");
			administrator.setEmail("no-reply@this1.it");
			try {
				clientUserService.createAdministrator(administrator, NETWORK_NAME);
			} catch (Throwable e) {

			}
			cassandra.createTable();
		}
		
		
		
		JobDetail job = JobBuilder
				.newJob(ConfortOnDemandJob.class)
				.withIdentity(ConfortOnDemandJob.COD_JOB_CRON,
						ConfortOnDemandJob.COD_JOB_GROUP).build();

		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ConfortOnDemandJob.COD_JOB_CRON,
						ConfortOnDemandJob.COD_JOB_GROUP)
				.withSchedule(
						CronScheduleBuilder
								.cronSchedule(QuartzConfig.QUARTZ_CRON_EVERY_30_MINUTES))
				.build();

		// schedule it
		try {
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
			if (scheduler.checkExists(trigger.getKey())) {
				scheduler.rescheduleJob(trigger.getKey(), trigger);
				logger.debug("COD Job rescheduled");
			}
			else {
				scheduler.scheduleJob(job, trigger);
				logger.debug("COD Job scheduled");
			}


		} catch (SchedulerException e) {
			logger.error("COD job not started", e);
		}
		
		
		
	}
	

	
	


}