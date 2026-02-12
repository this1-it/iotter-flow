package it.thisone.iotter.quartz;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.thisone.iotter.backend.BillingService;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.rest.model.billings.RenewDevice;

@Service
@DisallowConcurrentExecution
public class DeactivateJob implements InterruptableJob, Serializable {

	@Autowired
	private BillingService billingService;
	@Autowired
	private DeviceService deviceService;
	
	public static final String OWNERS = "owners";
	public static final String JOB_NAME = "DeactivationJob";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	@Override
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Executing Job {} ", context.getJobDetail());
		Set<String> owners = (Set<String>) context.getTrigger().getJobDataMap().get(OWNERS);
		if (owners == null) return;
		long now = System.currentTimeMillis() / 1000;
		for (String owner : owners) {
			try {
				List<RenewDevice> renews = billingService.readRenews(owner);
				for (RenewDevice renew : renews) {
					boolean blocked = renew.getBlock() > 0 ? now >= renew.getBlock() : false ;
					Device device = deviceService.findBySerial(renew.getSerial());
					// Bug #1521
					if (device != null && device.getOwner().equals(owner) && !owner.equals(Constants.ROLE_PRODUCTION.toLowerCase())) {
						boolean changed = deviceService.deactivateDevice(device, blocked);
						if (changed) {
							logger.info("DeactivateJob: device {} owner {} status {}", device.getSerial(), device.getOwner(), device.getStatus().name());
						}
					}
				}
			} catch (BackendServiceException e) {
				logger.error(JOB_NAME, e);
			}
		}
	}



	@Override
	public void interrupt() throws UnableToInterruptJobException {
	}

}
