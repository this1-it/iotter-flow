package it.thisone.iotter.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import it.thisone.iotter.exporter.IExportProvider;

public class ExportProviderFactory implements Serializable, IClassPathScanner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1276827443992545367L;

	public static Logger logger = LoggerFactory
			.getLogger(ExportProviderFactory.class);
	
	
	public static List<IExportProvider> findProviders() {
		String basePackages = "it.thisone.iotter.export";
		List<IExportProvider> providers = new ArrayList<IExportProvider>();
		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(
				bdr);
		TypeFilter tf = new AssignableTypeFilter(IExportProvider.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan(basePackages);
		IExportProvider provider = null;
		String[] beans = bdr.getBeanDefinitionNames();
		for (int i = 0; i < beans.length; i++) {
			BeanDefinition bd = bdr.getBeanDefinition(beans[i]);
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				provider = (IExportProvider) clazz.newInstance();
				providers.add(provider);
				
			} catch (ClassCastException |ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				logger.error(basePackages, e);
			}
		}
		
		if (providers.isEmpty()) {
			logger.error("No IExportProvider found in packages {} ", basePackages);
		}
		
		return providers;
	}

}
