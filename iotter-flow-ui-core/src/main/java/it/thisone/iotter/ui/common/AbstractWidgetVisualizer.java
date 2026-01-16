package it.thisone.iotter.ui.common;

import com.google.common.eventbus.Subscribe;
//import com.vaadin.flow.component.AbsoluteLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;

import it.thisone.iotter.persistence.ifc.IWidget;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.main.IMainUI;
import it.thisone.iotter.ui.uitask.UIRunnable;

@SuppressWarnings("serial")
public abstract class AbstractWidgetVisualizer extends BaseComponent {

	public static String NAME = "visualization";
	
//	private static Logger logger = LoggerFactory.getLogger(AbstractWidgetVisualizer.class);

	protected IWidget widget;
	
	private int x;
	
	private int y;

	// // Bug #307 [Cassandra] too many connection errors
	// a delay has been used to avoid concurrency on cassandra
	// such delay is calculated on graphwidget position in page
	private int position;
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public AbstractWidgetVisualizer(IWidget component) {
		// Bug #217 [VAADIN] some visualizations do not show all configured charts
		super(NAME, component.getLabel() + " " + component.getId() );
		widget = component;
		//setWidth(100, Unit.PERCENTAGE);
		calculateCssPosition();
	}
	
	/**
	 * Bug #113 [VAADIN] Widget Designer / Visualizer show elements in a different way
	 */
	public void calculateCssPosition() {
		
		int canvasWidth = ((IMainUI) UI.getCurrent()).getCanonicalWindowWidth();
		int canvasHeight = ((IMainUI) UI.getCurrent()).getCanonicalWindowHeight();
		
		int height = resize(widget.getHeight(), canvasHeight);
		int width = resize(widget.getWidth(), canvasWidth);
		
		y = resize(widget.getY(), canvasHeight);
		x = resize(widget.getX(), canvasWidth);

		
//		logger.debug("placeholder id: {}, X: {}, Y: {}, Width: {}, Height {}", //
//				widget.getId(), //
//				x, y, //
//				width, height); //
		
//		setWidth(width, Unit.PIXELS);
//		setHeight(height, Unit.PIXELS);
	}
	
	private int resize(float dimension, int canonical) {
		return Math.round(dimension * canonical);
	}


	protected abstract Component buildVisualization();

	public IWidget getWidget() {
		return widget;
	}


	/**
	 * set width and height of component
	 * @param windowWidth
	 * @param windowHeight
	 * @return cssPosition in absolute layout
	 */
	public String getCssPosition() {
		return getCssPosition(x, y);
	}

	public String getCssPosition(int x, int y) {
		// TODO assign properly "top","left","right" and "bottom" to specify the position.
		return String.format("top:%dpx;left:%dpx", y, x);
	}
	
	/**
	 * some iussue cannot add component
	 * @param top
	 * @param left
	 * @param right
	 * @param bottom
	 * @param zindex
	 * @return
	 */
	public String getCssPosition(int top, int left, int right, int bottom, int zindex) {
		return String.format("top:%dpx;left:%dpx;right:%dpx;bottom:%dpx;z-index:%d", left, top, right, bottom, zindex);
	}
	
//	public AbsoluteLayout getMainLayout() {
//		HasComponents layout = getParent();
//		if (layout != null && layout instanceof AbsoluteLayout) {
//			return (AbsoluteLayout)layout;
//		}
//		return null;
//	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	/*
	 * first step of refreshWithUiAccess
	 */
	public abstract boolean refresh();

	/*
	 * second step of refreshWithUiAccess
	 */
	public abstract void draw();

	
	/**
	 * method implementation should use {@link UIExecutor} that execute 
	 * two step runnable {@link UIRunnable} that can perform work in the background 
	 * and then update the UI when the UI lock has been obtained.
	 * @param event
	 */
	@Subscribe
	public abstract void refreshWithUiAccess(final WidgetRefreshEvent event);
	
//	@Override
//	public void attach() {
//		super.attach();
//		register();
//	}
//
//	@Override
//	public void detach() {
//		super.detach();
//		unregister();
//	}
	
	public void register() {
		UIUtils.getUIEventBus().register(this);
	}

	public void unregister() {
		UIUtils.getUIEventBus().unregister(this);
	}
	
}
