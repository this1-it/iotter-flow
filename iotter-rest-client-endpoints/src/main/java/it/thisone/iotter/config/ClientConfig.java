package it.thisone.iotter.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import it.thisone.iotter.backend.ClientUserService;
import it.thisone.iotter.rest.ClientUserEndpoint;


@Configuration
@EnableAsync
@ComponentScan(basePackageClasses = {ClientUserEndpoint.class, ClientUserService.class})
public class ClientConfig {


	private final Logger logger = LoggerFactory
			.getLogger(ClientConfig.class);

	@PostConstruct
	public void init() {
		logger.debug("ClientConfig initialized.");
	}


}