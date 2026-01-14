package it.thisone.iotter.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

import it.thisone.iotter.config.Constants;

@Component
public class EventBusSubscriberRegistrar implements BeanPostProcessor, ApplicationContextAware {
	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	@SuppressWarnings("unused")
	private ApplicationContext context;

	@Autowired
	private EventBus eventBus;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean.getClass().isAnnotationPresent(RegisterWithEventBus.class)){
			logger.debug("EventBusSubscriberRegistrar registering {} on eventbus", bean.getClass().getName());
			eventBus.register(bean);			
		}
		return bean;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}