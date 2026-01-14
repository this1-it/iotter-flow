package it.thisone.iotter.config;

import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public abstract class Initializator implements InitializingBean {

	protected Properties bootstrapProperties() {
		Properties props = new Properties();
		try {
			String resourceName = System.getProperty("bootstrap.properties",
					"bootstrap.properties");
			Resource resource = new ClassPathResource(resourceName);
			if (!resource.exists()) {
				resource = new ClassPathResource("bootstrap.default.properties");
			}
			props = PropertiesLoaderUtils.loadProperties(resource);
		} catch (Exception e) {
		}
		return props;
	}


}
