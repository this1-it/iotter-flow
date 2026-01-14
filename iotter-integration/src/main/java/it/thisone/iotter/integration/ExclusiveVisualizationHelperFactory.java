package it.thisone.iotter.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class ExclusiveVisualizationHelperFactory implements Serializable,IClassPathScanner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1276827443992545367L;

	
	public static List<IExclusiveVisualizationHelper> findHelpers() {
		String basePackages = "it.thisone.iotter.integration";
		List<IExclusiveVisualizationHelper> providers = new ArrayList<IExclusiveVisualizationHelper>();
		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(
				bdr);
		TypeFilter tf = new AssignableTypeFilter(IExclusiveVisualizationHelper.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan(basePackages);
		IExclusiveVisualizationHelper provider = null;
		String[] beans = bdr.getBeanDefinitionNames();
		for (int i = 0; i < beans.length; i++) {
			BeanDefinition bd = bdr.getBeanDefinition(beans[i]);
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				provider = (IExclusiveVisualizationHelper) clazz.newInstance();
				providers.add(provider);
			} catch (ClassCastException |ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
			}
		}
		return providers;
	}

}
