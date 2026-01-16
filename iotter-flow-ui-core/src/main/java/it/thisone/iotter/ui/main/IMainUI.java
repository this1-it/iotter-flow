package it.thisone.iotter.ui.main;

import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.security.core.session.SessionRegistry;

import it.thisone.iotter.eventbus.EventBusWrapper;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.integration.ServiceFactory;
import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.persistence.service.DatabaseMessageSource;
import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.ifc.IUiFactory;

public interface IMainUI  {

	public SessionRegistry getSessionRegistry();

	public String localize(String key);

	public String localize(String code, Object[] args, String defaultMessage);
	
	public boolean hasRole(String role);

	public boolean hasPermission(EntityPermission permission);
	
	public UserDetailsAdapter getUserDetails();
	
	public EntityManager getEntityManager();
	
	public ServiceFactory getServiceFactory();

	public Logger getLogger();
	
	public String getServerName();

	// public Layout getHeader();

	// public Layout getFooter();
	
	public CassandraService getCassandraService();

	public String getServerUrl();	

	public boolean isMobile();	
	
	public void logout();
	
	/**
	 * Bug #113 [VAADIN] Widget Designer / Visualizer show elements in a different way
	 * @return calculated value for saving x and width in designer
	 */
	public int getCanonicalWindowWidth();
	
	/**
	 * Bug #113 [VAADIN] Widget Designer / Visualizer show elements in a different way
	 * @return calculated value for saving y and height in designer
	 */
	public int getCanonicalWindowHeight();

	/**
	 * Bug #113 [VAADIN] Widget Designer / Visualizer show elements in a different way
	 * @return 
	 */
	public float getAspectRatio();
	
	public int getUnAvailableHeight();
	
	// public void addListener(ToggleMenuListener listener);
	
	// public void removeListener(ToggleMenuListener listener);
	
	public EventBusWrapper getUIEventBus();
	
	//public UIExecutor getUIExecutor();
	
	public ThreadPoolExecutor getThreadPoolExecutor();

	public void login(UserDetailsAdapter user);
	
	public Properties getAppProperties();

	//public DrawerLayout getDrawer();

	public TimeZone getTimeZone();

	public DatabaseMessageSource getDatabaseMessageSource();

	public GeoLocation getGeoLocation();
	
	public IUiFactory getUiFactory();
	
	public void startWidgetRefresher();
	
	//public CustomComponent getMenu();


	public <T> T getBean(Class<T> requiredType) throws BeansException;
}
