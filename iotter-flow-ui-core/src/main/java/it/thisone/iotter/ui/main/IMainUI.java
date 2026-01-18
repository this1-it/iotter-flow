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

@Deprecated
public interface IMainUI {

	@Deprecated
	public SessionRegistry getSessionRegistry();

	@Deprecated
	public String localize(String key);

	@Deprecated
	public String localize(String code, Object[] args, String defaultMessage);

	@Deprecated
	public boolean hasRole(String role);

	@Deprecated
	public boolean hasPermission(EntityPermission permission);

	@Deprecated
	public UserDetailsAdapter getUserDetails();

	@Deprecated
	public EntityManager getEntityManager();

	@Deprecated
	public ServiceFactory getServiceFactory();

	@Deprecated
	public Logger getLogger();

	@Deprecated
	public String getServerName();

	// public Layout getHeader();

	// public Layout getFooter();
	@Deprecated
	public CassandraService getCassandraService();

	@Deprecated
	public String getServerUrl();

	@Deprecated
	public boolean isMobile();

	@Deprecated
	public void logout();

	/**
	 * Bug #113 [VAADIN] Widget Designer / Visualizer show elements in a different
	 * way
	 * 
	 * @return calculated value for saving x and width in designer
	 */
	@Deprecated
	public int getCanonicalWindowWidth();

	/**
	 * Bug #113 [VAADIN] Widget Designer / Visualizer show elements in a different
	 * way
	 * 
	 * @return calculated value for saving y and height in designer
	 */
	@Deprecated
	public int getCanonicalWindowHeight();

	/**
	 * Bug #113 [VAADIN] Widget Designer / Visualizer show elements in a different
	 * way
	 * 
	 * @return
	 */
	@Deprecated
	public float getAspectRatio();

	@Deprecated
	public int getUnAvailableHeight();

	// public void addListener(ToggleMenuListener listener);

	// public void removeListener(ToggleMenuListener listener);
	@Deprecated
	public EventBusWrapper getUIEventBus();

	// public UIExecutor getUIExecutor();
	@Deprecated
	public ThreadPoolExecutor getThreadPoolExecutor();

	@Deprecated
	public void login(UserDetailsAdapter user);

	@Deprecated
	public Properties getAppProperties();

	// public DrawerLayout getDrawer();
	@Deprecated
	public TimeZone getTimeZone();

	@Deprecated
	public DatabaseMessageSource getDatabaseMessageSource();

	@Deprecated
	public GeoLocation getGeoLocation();

	@Deprecated
	public IUiFactory getUiFactory();

	@Deprecated
	public void startWidgetRefresher();

	// public CustomComponent getMenu();

	@Deprecated
	public <T> T getBean(Class<T> requiredType) throws BeansException;
}
