package org.fao.geonet.util.spring;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Custom JobFactory to add application context to JobDataMap.
 * 
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
		implements ApplicationContextAware {

	private transient AutowireCapableBeanFactory beanFactory;

	private ApplicationContext applicationContext;

	public void setApplicationContext(final ApplicationContext context) {
		applicationContext = context;
		beanFactory = applicationContext.getAutowireCapableBeanFactory();
	}

	@Override
	protected Object createJobInstance(final TriggerFiredBundle bundle)
			throws Exception {
		bundle.getJobDetail().getJobDataMap()
				.put("applicationContext", applicationContext);
		final Object job = super.createJobInstance(bundle);
		beanFactory.autowireBean(job);
		return job;
	}
}
