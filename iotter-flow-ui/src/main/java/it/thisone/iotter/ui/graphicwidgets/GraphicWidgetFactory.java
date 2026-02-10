package it.thisone.iotter.ui.graphicwidgets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.exceptions.ApplicationRuntimeException;
import it.thisone.iotter.integration.IClassPathScanner;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.ui.providers.GraphicWidgetProvider;
import it.thisone.iotter.util.EncryptUtils;

public class GraphicWidgetFactory implements Serializable,IClassPathScanner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static int maxParameters(GraphicWidget widget) {
		int maxParameters = 0;
		if (widget != null) {
			switch (widget.getType()) {
			case LAST_MEASURE:
				maxParameters = 1;
				break;
			case LAST_MEASURE_TABLE:
				maxParameters = UiConstants.MAX_PARAMETERS;
				break;
			case HISTOGRAM:
				maxParameters = 1;
				break;
			case TABLE:
				maxParameters = UiConstants.MAX_PARAMETERS;
				break;
			case EMBEDDED:
				maxParameters = UiConstants.MAX_PARAMETERS;
				break;
			case MULTI_TRACE:
				maxParameters = UiConstants.MAX_PARAMETERS;
				break;
			case CUSTOM:
				maxParameters = customWidgetMaxParameters(widget);
				break;
			default:
				maxParameters = 0;
				break;
			}
		}
		return maxParameters;
	}

	public static AbstractWidgetVisualizer createWidgetVisualizer(
			GraphicWidget widget) {
		AbstractWidgetVisualizer visualizer = null;
		if (widget != null) {
			if (GraphicWidgetType.CUSTOM.equals(widget.getType())) {
				visualizer = customWidgetVisualizer(widget);
			}

			widget.removeOrphanFeeds();
		}

		if (visualizer == null) {
			// TODO(flow-migration): replace fallback with specific visualizer implementations per widget type.
			visualizer = new UnsupportedWidgetVisualizer(widget);
		}

		return visualizer;
	}

	public static GraphicWidgetPlaceHolder createPlaceHolder(
			GraphicWidgetType type, String provider, int fullWidth, int x, int y) {
		float height = 550;
		float width = 550;
		switch (type) {
		case MULTI_TRACE:
			height = 550;
			width = fullWidth;
			break;
		case LAST_MEASURE_TABLE:
			height = 300;
			width = 500;
			break;
		case LAST_MEASURE:
			height = 200;
			width = 200;
			break;
		case LABEL:
			height = 50;
			width = fullWidth;
			break;

		case HISTOGRAM:
			height = 550;
			width = fullWidth;
			break;
		case CUSTOM:
			float[] size = customWidgetDefaultSize(provider);
			height = size[1];
			width = size[0];
			break;

		default:
			break;
		}

		GraphicWidget widget = new GraphicWidget();
		widget.setId(EncryptUtils.getUniqueId());
		widget.setType(type);
		widget.setProvider(provider);
		widget.setX(x);
		widget.setY(y);
		widget.setHeight(height);
		widget.setWidth(width);
		widget.getOptions().setFillColor(ChartUtils.quiteRandomHexColor());
		List<GraphicWidget> children = new ArrayList<GraphicWidget>();


		GraphicWidgetPlaceHolder placeHolder = new GraphicWidgetPlaceHolder(
				widget, children);

		return placeHolder;
	}

	public static AbstractBaseEntityForm<GraphicWidget> createWidgetEditor(
			GraphicWidget entity) {
		AbstractBaseEntityForm<GraphicWidget> editor = null;
		if (entity != null) {
			switch (entity.getType()) {
			case CUSTOM:
				editor = customWidgetEditor(entity);
				break;
			default:
				editor = new GraphicWidgetForm(entity);
				break;
			}
		}

		return editor;
	}


	// public static void createPlaceholderChildren(
	// 		GraphicWidgetPlaceHolder placeHolder) {
	// 	GraphicWidget widget = placeHolder.getWidget();
	// 	List<GraphicWidget> children = new ArrayList<GraphicWidget>();
	// 	if (!widget.getType().equals(GraphicWidgetType.STATION)) {
	// 		return;
	// 	}
	// 	if (!placeHolder.getChildren().isEmpty())
	// 		return;

	// 	String serial = widget.getDevice();
	// 	Device device = UIUtils.getServiceFactory().getDeviceService()
	// 			.findBySerial(serial);
	// 	List<Channel> channels = new ArrayList<Channel>();
	// 	// Bug #320 Station: il wizard selezione automaticamente un parametro
	// 	// che può essere disabilitato
	// 	for (Channel chnl : device.getChannels()) {
	// 		if (chnl.getConfiguration().isActive() && !chnl.isCrucial()) {
	// 			channels.add(chnl);
	// 		}
	// 	}
	// 	Collections.sort(channels, new ChannelComparator());

	// 	GraphicWidget title = new GraphicWidget();
	// 	title.setWidth(widget.getWidth());
	// 	title.setId(EncryptUtils.getUniqueId());
	// 	title.setDevice(serial);
	// 	title.setLabel(device.toString());
	// 	title.getOptions().setFillColor(ChartUtils.quiteRandomHexColor());
	// 	title.setParent(widget.getContainer());
	// 	title.setType(GraphicWidgetType.LABEL);
	// 	title.setGroupWidget(widget.getGroupWidget());

	// 	GraphicWidget lastMeasure = null;

	// 	if (!channels.isEmpty()) {
	// 		lastMeasure = new GraphicWidget();
	// 		lastMeasure.setId(EncryptUtils.getUniqueId());
	// 		lastMeasure.setWidth(widget.getWidth() / 3f);
	// 		lastMeasure.setDevice(serial);
	// 		lastMeasure.setParent(widget.getContainer());
	// 		lastMeasure.setType(GraphicWidgetType.LAST_MEASURE);
	// 		lastMeasure.setGroupWidget(widget.getGroupWidget());
	// 		addFeed(lastMeasure, channels.get(0));
	// 		lastMeasure.setLabel(channels.get(0).toString());
	// 	}

	// 	GraphicWidget lastMeasureTable = null;

	// 	if (channels.size() > 1) {
	// 		lastMeasureTable = new GraphicWidget();
	// 		lastMeasureTable.setWidth(widget.getWidth() / 3f);
	// 		lastMeasureTable.setId(EncryptUtils.getUniqueId());
	// 		lastMeasureTable.setDevice(serial);
	// 		lastMeasureTable.setParent(widget.getContainer());
	// 		lastMeasureTable.setType(GraphicWidgetType.LAST_MEASURE_TABLE);
	// 		lastMeasureTable.setGroupWidget(widget.getGroupWidget());
	// 		StringBuffer sb = new StringBuffer();
	// 		int bound = 0;
	// 		for (int i = 1; i < channels.size(); i++) {
	// 			Channel chnl = channels.get(i);
	// 			addFeed(lastMeasureTable, chnl);
	// 			sb.append(chnl.toString());
	// 			sb.append(" ");
	// 			bound++;
	// 			if (bound >= 4)
	// 				break;
	// 		}
	// 		lastMeasureTable.setLabel(sb.toString());
	// 	}

	// 	children.add(title);
	// 	if (lastMeasure != null) {
	// 		children.add(lastMeasure);
	// 	}
	// 	if (lastMeasureTable != null) {
	// 		children.add(lastMeasureTable);
	// 	}
	// 	placeHolder.setChildren(children);
	// }

	// private static void addFeed(GraphicWidget widget, Channel channel) {
	// 	GraphicFeed feed = new GraphicFeed();
	// 	feed.getOptions().setFillColor(ChartUtils.quiteRandomHexColor());
	// 	feed.setChannel(channel);
	// 	feed.setWidget(widget);
	// 	feed.setMeasure(channel.getDefaultMeasure());
	// 	widget.addFeed(feed);
	// }

	private static AbstractWidgetVisualizer customWidgetVisualizer(
			GraphicWidget widget) {
		AbstractWidgetVisualizer visualizer = null;
		for (GraphicWidgetProvider provider : findProviders("it.thisone.iotter.ui")) {
			if (provider != null && provider.getName().equalsIgnoreCase(widget.getProvider())) {
				visualizer = provider.getVisualizer(widget);
				break;
			}
		}
		return visualizer;
	}
	
	private static AbstractBaseEntityForm<GraphicWidget> customWidgetEditor(
			GraphicWidget widget) {
		AbstractBaseEntityForm<GraphicWidget> editor = null;
		for (GraphicWidgetProvider provider : findProviders("it.thisone.iotter.ui")) {
			if (provider != null && provider.getName().equalsIgnoreCase(widget.getProvider())) {
				editor = provider.getForm(widget);
				break;
			}
		}
		return editor;
	}

	private static int customWidgetMaxParameters(GraphicWidget widget) {
		int maxParameters = 0;
		for (GraphicWidgetProvider provider : findProviders("it.thisone.iotter.ui")) {
			if (provider != null && provider.getName().equalsIgnoreCase(widget.getProvider())) {
				maxParameters = provider.maxParameters();
				break;
			}
		}
		return maxParameters;
	}

	
	private static float[] customWidgetDefaultSize(String providerName) {
		float[] size = new float[] {0,0};
		for (GraphicWidgetProvider provider : findProviders("it.thisone.iotter.ui")) {
			if (provider != null && provider.getName().equalsIgnoreCase(providerName)) {
				size = provider.defaultSize();
				break;
			}
		}
		return size;
	}
	
	private static List<GraphicWidgetProvider> findProviders(String basePackages) {
		// http://stackoverflow.com/questions/435890/find-java-classes-implementing-an-interface
		// https://devnotesblog.wordpress.com/2011/03/20/adding-plug-ins-to-your-application-with-spring/
		
		List<GraphicWidgetProvider> providers = new ArrayList<GraphicWidgetProvider>();
		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr, false);
		TypeFilter tf = new AssignableTypeFilter(GraphicWidgetProvider.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan(basePackages);
		GraphicWidgetProvider provider = null;
		String[] beans = bdr.getBeanDefinitionNames();
		for (int i = 0; i < beans.length; i++) {
			BeanDefinition bd = bdr.getBeanDefinition(beans[i]);
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				if (GraphicWidgetProvider.class.isAssignableFrom(clazz)) {
					provider = (GraphicWidgetProvider) clazz.newInstance();
					providers.add(provider);
				}
				
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
			}
		}
		return providers;
	}

}
