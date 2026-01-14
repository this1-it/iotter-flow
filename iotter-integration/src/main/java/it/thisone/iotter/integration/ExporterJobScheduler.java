package it.thisone.iotter.integration;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import it.thisone.iotter.config.DevelEnabledCondition;
import it.thisone.iotter.quartz.ExporterJob;

@Service
@Conditional(DevelEnabledCondition.class)
public class ExporterJobScheduler implements InitializingBean, DisposableBean{

	// 2025-06-04 10:48:52+0000
	@Autowired
	private Scheduler scheduler;

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//startupJob();
		cronJob();

	}

	public void startupJob() throws SchedulerException {
		Trigger trigger = newTrigger().withIdentity("StartupExporterJobScheduler").startNow().build();
		//JobDataMap jobDataMap = trigger.getJobDataMap();
		JobKey jobKey = new JobKey(trigger.getKey().getName(), trigger.getKey().getGroup());
		JobDetail jobDetail = newJob(ExporterJob.class).withIdentity(jobKey).build();
		scheduler.scheduleJob(jobDetail, trigger);
	}

	
	
	public void cronJob() throws SchedulerException {
		
		String exporterCron = "0 0 0/4 * * ?";
		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity("CronExporterJobScheduler")
				.withSchedule(CronScheduleBuilder.cronSchedule(exporterCron))
				.build();
		
		
		//JobDataMap jobDataMap = trigger.getJobDataMap();
		JobKey jobKey = new JobKey(trigger.getKey().getName(), trigger.getKey().getGroup());
		JobDetail jobDetail = newJob(ExporterJob.class).withIdentity(jobKey).build();
		scheduler.scheduleJob(jobDetail, trigger);
	}
	
}
