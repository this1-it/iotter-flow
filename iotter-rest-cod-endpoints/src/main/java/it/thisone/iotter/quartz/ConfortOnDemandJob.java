package it.thisone.iotter.quartz;

import java.io.Serializable;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.thisone.iotter.rest.ConfortOnDemandService;

@Service
@DisallowConcurrentExecution
public class ConfortOnDemandJob implements InterruptableJob, Serializable {
	public static final String COD_EVENT = "cod-event";
	public static final String COD_JOB_NAME = "cod-job";
	public static final String COD_JOB_GROUP = "cod-job-group";
	public static final String COD_JOB_CRON = "cod-job-cron";
	
	@Autowired
	private ConfortOnDemandService service;



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		service.executeCronJob();
	}
	



	@Override
	public void interrupt() throws UnableToInterruptJobException {
	}

}
