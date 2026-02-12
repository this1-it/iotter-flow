package it.thisone.iotter.config;

import java.util.Properties;

import javax.annotation.PostConstruct;

//import org.codehaus.jackson.map.AnnotationIntrospector;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.SerializationConfig.Feature;
//import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
//import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;


@Configuration
@ComponentScan({ "it.thisone.iotter.rest" })
public class BillingConfig {
	
	

	private final Logger logger = LoggerFactory.getLogger(BillingConfig.class);

	@PostConstruct
	public void init() {
		logger.debug("BillingConfig initialized.");
	}

	@Bean
	@Qualifier("billingsProperties")
	public Properties billingsProperties() {
		Properties properties = null;
		try {
			ClassPathResource resource = new ClassPathResource(
					"billings.properties");
			if (!resource.exists()) {
				resource = new ClassPathResource("billings.default.properties");
			}
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (Exception e) {
			logger.error("unable to retrieve billings.properties", e);
		}
		return properties;
	}
	
	

	
	
}
