package it.thisone.iotter.config;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import it.thisone.iotter.backend.ClientUserService;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.rest.SATClientEndpoint;


@Configuration
@ComponentScan(basePackageClasses = {SATClientEndpoint.class})
public class SATClientConfig  extends Initializator{


	private final Logger logger = LoggerFactory
			.getLogger(SATClientConfig.class);
	
	
	public static final String ADMIN_NAME = "Sat";
	public static final String NETWORK_NAME = ADMIN_NAME;


	@Autowired
	private ClientUserService clientUserService;
	
	@PostConstruct
	public void init() {
		logger.debug("CODClientConfig initialized.");
	}
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
		}
	}


}