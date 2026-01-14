package it.thisone.iotter.provisioning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.integration.IClassPathScanner;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.rest.model.ModbusProvisioning;
import it.thisone.iotter.util.BacNet;

public class ProvisioningFactory implements Serializable, IClassPathScanner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1276827443992545367L;

	
	public static List<IProvisioningProvider> findProviders() {
		String basePackages = "it.thisone.iotter.provisioning";
		List<IProvisioningProvider> providers = new ArrayList<IProvisioningProvider>();
		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(
				bdr);
		TypeFilter tf = new AssignableTypeFilter(IProvisioningProvider.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan(basePackages);
		IProvisioningProvider provider = null;
		String[] beans = bdr.getBeanDefinitionNames();
		for (int i = 0; i < beans.length; i++) {
			BeanDefinition bd = bdr.getBeanDefinition(beans[i]);
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				provider = (IProvisioningProvider) clazz.newInstance();
				providers.add(provider);
				
			} catch (ClassCastException |ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
			}
		}
		return providers;
	}
	
	


}
