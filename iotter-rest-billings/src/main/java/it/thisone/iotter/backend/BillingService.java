package it.thisone.iotter.backend;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.quartz.DeactivateJob;
import it.thisone.iotter.rest.model.billings.BillingDevice;
import it.thisone.iotter.rest.model.billings.RenewDevice;
import it.thisone.iotter.rest.model.billings.RenewToken;
import it.thisone.iotter.rest.model.billings.ResponseRenew;
import it.thisone.iotter.rest.model.billings.ResponseToken;

@Service
public class BillingService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7046035787644246691L;

	public static final String API_RENEW_TOKEN = "/api/renew_token.php";

	public static final String API_STATO_IMPIANTI_UTENTE = "/api/stato_impianti_utente.php";

	public static Logger logger = LoggerFactory.getLogger(BillingService.class);

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private UserService userService;

	@Autowired
	@Qualifier("billingsProperties")
	private Properties properties;

	@Autowired
	public Client jerseyClient;

	public List<BillingDevice> getActivatedDevices(String owner) {
		DeviceCriteria criteria = new DeviceCriteria();
		if (owner != null) {
			criteria.setOwner(owner);
		}
		criteria.setActivated(true);
		List<Device> entities = deviceService.search(criteria, 0, 10000);
		List<BillingDevice> devices = new ArrayList<>();
		for (Device entity : entities) {
			BillingDevice device = new BillingDevice();
			device.setSerial(entity.getSerial());
			device.setLabel(entity.getLabel());
			device.setOwner(entity.getOwner());
			device.setActivation(entity.getActivationDate().getTime() / 1000);
			devices.add(device);
		}
		return devices;
	}

	public boolean validateApiKey(String apiKey) {
		return properties.getProperty("renewal_platform_api_key").equals(apiKey);
	}

	public String apiKey() {
		return properties.getProperty("renewal_platform_api_key");
	}
	public String termsAndConditionsResource() {
		return properties.getProperty("renewal_terms_and_conditions");
	}
	

	
	public List<RenewDevice> readRenews(String owner) throws BackendServiceException {
		try {
			String restResource = properties.getProperty("renewal_platform");
			WebTarget target = jerseyClient.target(restResource);
			target = target.path(API_STATO_IMPIANTI_UTENTE);
			if (owner != null) {
				target = target.queryParam("uuid", owner);				
			}
			Response response = target.request(MediaType.APPLICATION_JSON_TYPE).header("api-key", apiKey())
					.get(Response.class);
			ResponseRenew result = response.readEntity(ResponseRenew.class, null);
			if (result != null && result.getData() != null && result.getData().getPlant() != null) {
				List<RenewDevice> renews = result.getData().getPlant();
				List<RenewDevice> items = new ArrayList<RenewDevice>();
				for (RenewDevice renew : renews) {
					Device device = deviceService.findBySerialCached(renew.getSerial()); 
					if (device != null && device.getOwner().equals(renew.getOwner())) {
						items.add(renew);
					}					
				}
				
				return items;
			}
		} catch (Exception e) {
			logger.error(API_STATO_IMPIANTI_UTENTE, e);
			throw new BackendServiceException(e);
		}
		return new ArrayList<>();
	}

	public RenewToken renewToken(String owner, String serial, String action) throws BackendServiceException {
		try {
			String restResource = properties.getProperty("renewal_platform");
			WebTarget target = jerseyClient.target(restResource);
			target = target.path(API_RENEW_TOKEN);
			target = target.queryParam("serial", serial);
			Response response = target.request(MediaType.APPLICATION_JSON_TYPE).header("api-key", apiKey())
					.get(Response.class);
			ResponseToken result = response.readEntity(ResponseToken.class, null);
			if (result.getData() != null && result.getData().getUrl() != null) {
				userService.deleteToken(owner, action);
				userService.createUserToken(owner, action, result.getData().getToken(), 1);
				return result.getData();
			}
		} catch (Exception e) {
			logger.error(API_RENEW_TOKEN, e);
			throw new BackendServiceException(e);
		}
		return null;
	}

	public void scheduleDeactivateJob(Set<String> owners) {
		if (owners.isEmpty()) return;
		Date date = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
		Trigger trigger = newTrigger().withIdentity(DeactivateJob.JOB_NAME, "Billings").startAt(date).build();
		JobDataMap jobDataMap = trigger.getJobDataMap();
		jobDataMap.put(DeactivateJob.OWNERS, owners);
		JobKey jobKey = new JobKey(trigger.getKey().getName(), trigger.getKey().getGroup());
		JobDetail jobDetail = newJob(DeactivateJob.class).withIdentity(jobKey).build();
		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			logger.error(DeactivateJob.JOB_NAME, e);
		}
	}

}
