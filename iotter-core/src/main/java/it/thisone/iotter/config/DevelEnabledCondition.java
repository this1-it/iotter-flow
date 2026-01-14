package it.thisone.iotter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DevelEnabledCondition  implements Condition{
	private static Logger logger = LoggerFactory.getLogger(Constants.MQTT.LOG4J_CATEGORY);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		ClassPathResource resource = new ClassPathResource("quartz.properties");
		// enabled only for development
		boolean enabled = !resource.exists();
		logger.info("DevelEnabledCondition enabled: {}", enabled);
		return false;
	}

}
