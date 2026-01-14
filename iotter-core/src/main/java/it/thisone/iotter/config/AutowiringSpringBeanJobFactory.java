package it.thisone.iotter.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * This JobFactory autowires automatically the created quartz bean with spring @Autowired
 * dependencies.
 * 
 * 
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

	private transient AutowireCapableBeanFactory beanFactory;

	@Override
	public void setApplicationContext(final ApplicationContext context) {
		beanFactory = context.getAutowireCapableBeanFactory();
	}

	@Override
	protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
		Object job = null;
		Exception executionException = null;
		int count = 0;
		while (count < 3) {
			try {
				job = super.createJobInstance(bundle);
				beanFactory.autowireBean(job);
				executionException = null;
				break;
			} catch (Exception e) {
				count++;
				executionException = e;
			}
		}
		if (executionException != null) {
			throw executionException;
		}
		return job;
	}
}