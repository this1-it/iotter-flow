package it.thisone.iotter.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import it.thisone.iotter.common.MessageSourceWithFallback;
//import it.thisone.iotter.security.EntityPermission;
//import it.thisone.iotter.security.PermissionsToRole;

import it.thisone.iotter.util.Utils;

@Configuration
public class AppConfig {
    private final Logger logger = LoggerFactory.getLogger(AppConfig.class);
	
    @Autowired 
    ServletContext servletContext;    
    
	@PostConstruct
	public void init() {
		logger.debug("AppConfig initialized. Messages loaded");
//		PermissionsToRole.initialize();
//		PermissionsToRole.remove(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.CREATE);
//		PermissionsToRole.remove(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.REMOVE);
	}

	@Bean
	public ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource() {
		MessageSourceWithFallback messageSource = new MessageSourceWithFallback();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setFallbackToSystemLocale(false);
		return messageSource;
	}
	
    @Bean
    @Qualifier("appProperties")
    public Properties appProperties() {
    	Properties properties = new Properties();
		try {
			// system property override
			String resourceName = System.getProperty("app.properties",
					"app.properties");
			Resource resource = new ClassPathResource(resourceName);
			if (!resource.exists()) {
				resource = new ClassPathResource("app.default.properties");
			}
			properties.putAll(PropertiesLoaderUtils.loadProperties(resource));
			InputStream inputStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			if (inputStream != null) {
				Manifest manifest = new Manifest(inputStream);
				properties.putAll(Utils.loadProperties(manifest));
			}
			
		} catch (Exception e) {
			logger.error("unable to retrieve app.properties", e);
		}
		return properties;
    }



}
