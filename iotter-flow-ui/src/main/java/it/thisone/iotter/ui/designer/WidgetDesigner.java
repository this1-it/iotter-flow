package it.thisone.iotter.ui.designer;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BrowserWindowResizeEvent;
import com.vaadin.flow.component.page.BrowserWindowResizeListener;

import it.thisone.iotter.ui.designer.ResizePanel;
import it.thisone.iotter.ui.designer.ResizePanel.MovedEvent;
import it.thisone.iotter.ui.designer.ResizePanel.MovedListener;
import it.thisone.iotter.ui.designer.ResizePanel.ResizedEvent;
import it.thisone.iotter.ui.designer.ResizePanel.ResizedListener;
import it.thisone.iotter.ui.main.IMainUI;

@SuppressWarnings("serial")
public class WidgetDesigner extends VerticalLayout implements IParkingPlace {
	private static Logger logger = LoggerFactory
			.getLogger(WidgetDesigner.class);

	private Div mainLayout;
	private Div mainPanel;

	private int canvasWidth;
	private int canvasHeight;
	private int unAvailableHeight;

	private String backgroundStyleName;

	public WidgetDesigner(int height) {
		if (height < 0)
			height = 0;
		unAvailableHeight = height;
		buildMainLayout();
	}

	public void standardMainLayout() {
		initCanvasSize();
		mainLayout.setWidth(canvasWidth, Unit.PIXELS);
		mainLayout.setHeight(canvasHeight, Unit.PIXELS);
		if (backgroundStyleName != null) {
			mainLayout.removeClassName(backgroundStyleName);
		}
	}

	private void fixPlaceHolder(IPlaceHolder placeHolder) {

		if (placeHolder.getX() > canvasWidth)
			placeHolder.setX(0);

		// if (placeHolder.getY() > canvasHeight)
		// placeHolder.setY(0);

		// if (placeHolder.getPixelWidth() <= 0)
		// 	placeHolder.setPixelWidth((int) placeHolder.getWidth());

		// if (placeHolder.getPixelHeight() <= 0)
		// 	placeHolder.setPixelHeight((int) placeHolder.getHeight());

		if (placeHolder.getPixelWidth() >= canvasWidth) {
			placeHolder.setPixelWidth(canvasWidth - 10);
		}

		if (placeHolder.getPixelHeight() >= canvasHeight) {
			placeHolder.setPixelHeight(canvasHeight - 10);
		}

		// if (placeHolder.getPixelHeight() <= 0) {
		// 	placeHolder.setPixelHeight((int) placeHolder.getHeight());
		// }

	}

	@Override
	public ToggleMenuListener createToggleMenuListener() {
		// TODO(flow-migration): replace IMainUI usage with injected state/service.
		return new ToggleMenuListener() {
			@Override
			public void toggleMenu(ToggleMenuEvent event) {
				int canonicalWidth = ((IMainUI) UI.getCurrent())
						.getCanonicalWindowWidth();
				if (canvasWidth != canonicalWidth) {
					changeCanvasSize(canonicalWidth);
				}
			}
		};
	}

	@Override
	public BrowserWindowResizeListener createBrowserWindowResizeListener() {
		// TODO(flow-migration): replace IMainUI usage with injected state/service.
		return new BrowserWindowResizeListener() {
			@Override
			public void browserWindowResized(BrowserWindowResizeEvent event) {
				int height = calculatePanelHeight();
				mainPanel.setHeight(height, Unit.PIXELS);
				int canonicalWidth = ((IMainUI) UI.getCurrent())
						.getCanonicalWindowWidth();
				if (canvasWidth != canonicalWidth) {
					changeCanvasSize(canonicalWidth);
				}
			}
		};
	}

	/**
	 * WARNING ! height has been calculated empirically !
	 */
	private int calculatePanelHeight() {
		// TODO(flow-migration): confirm Page browser height API in Flow.
		// int height = ((IMainUI) UI.getCurrent()).getUnAvailableHeight()
		// 		+ unAvailableHeight;
		// return UI.getCurrent().getPage().getBrowserWindowHeight() - height;
		return 0;
	}

	private void buildMainLayout() {
		initCanvasSize();
		mainPanel = new Div();
		mainPanel.addClassName("panel-borderless");
		mainPanel.getStyle().set("overflow", "auto");
		// Bug #222 [VAADIN] GroupWidgetDesigner not working as expected: 
		// canvas height does not grow laying out new components
		mainPanel.setHeight(calculatePanelHeight(), Unit.PIXELS);
		mainLayout = new Div();
		mainLayout.getStyle().set("position", "relative");
		mainPanel.removeAll();
		mainPanel.add(mainLayout);
		mainLayout.setWidth(canvasWidth, Unit.PIXELS);
		mainLayout.setHeight(canvasHeight, Unit.PIXELS);
		mainLayout.addClassName("widget-designer");
		add(mainPanel);
	}


	private void initCanvasSize() {
		// TODO(flow-migration): replace IMainUI usage with injected state/service.
		// canvasWidth = ((IMainUI) UI.getCurrent()).getCanonicalWindowWidth();
		// canvasHeight = UI.getCurrent().getPage().getWebBrowser()
		// 		.getScreenHeight();
	}

	public Iterator<Component> placeHolders() {
		if (mainLayout != null)
			return mainLayout.getChildren().iterator();
		return null;
	}

	@Override
	public void addPlaceHolder(IPlaceHolder placeHolder) {
		int height = placeHolder.getY() + placeHolder.getPixelHeight();
		fixPlaceHolder(placeHolder);
		logger.debug("placeholder id: {}, X: {}, Y: {}, Width: {}, Height {}", //
				placeHolder.getIdentifier(), //
				placeHolder.getX(), placeHolder.getY(), //
				placeHolder.getPixelWidth(), placeHolder.getPixelHeight()); //
		
		// Bug #223 [VAADIN] GroupWidgetDesigner not working as expected: 
		// widgets are show stacked opening designer
		final ResizePanel panel = new ResizePanel();
		panel.setBoundaryPanel(mainLayout);

		// TODO(flow-migration): ensure IPlaceHolder exposes a Flow Component instance.
		panel.addComponent((Component) placeHolder);
		mainLayout.add(panel);

		panel.setTop(placeHolder.getY());
		panel.setLeft(placeHolder.getX());
		panel.setWidth(placeHolder.getPixelWidth(), Unit.PIXELS);
		panel.setHeight(placeHolder.getPixelHeight(), Unit.PIXELS);


		panel.addResizedListener(new ResizedListener() {
			@Override
			public void onResized(ResizedEvent event) {
				try {
					Iterator<Component> i = panel.iterator();
					while (i.hasNext()) {
						Component c = i.next();
						if (c instanceof IPlaceHolder) {
							// TODO(flow-migration): inject UIEventBus and replace UIUtils usage.

							((IPlaceHolder) c).setPixelWidth((int) event
									.getWidth());
							((IPlaceHolder) c).setPixelHeight((int) event
									.getHeight());
							logger.debug(
									"resized event width: {} , height: {} ",
									event.getWidth(), event.getHeight());
						}
					}
				} catch (Exception e) {
					logger.debug("onResized", e);
				}
			}

		});

		panel.addMovedListener(new MovedListener() {
			@Override
			public void onMoved(MovedEvent event) {
				try {
					Iterator<Component> i = panel.iterator();
					while (i.hasNext()) {
						Component c = i.next();
						if (c instanceof IPlaceHolder) {
							// TODO(flow-migration): inject UIEventBus and replace UIUtils usage.

							((IPlaceHolder) c).setX((int) event.getX());
							((IPlaceHolder) c).setY((int) event.getY());
							logger.debug("moved event X: {} , Y: {} ",
									event.getX(), event.getY());
						}
					}
				} catch (Exception e) {
					logger.debug("onMoved", e);
				}
			}
		});

		if (height > canvasHeight) {
			changeCanvasHeight(height);
		}

	}

	@Override
	public boolean containPlaceHolder(IPlaceHolder placeHolder){
		ResizePanel found = getResizePanel(placeHolder.getIdentifier());
		return (found != null);
	}

	@Override
	public void removePlaceHolder(IPlaceHolder placeHolder) {
		/**
		 * fixed java.util.ConcurrentModificationException
		 */
		ResizePanel found = getResizePanel(placeHolder.getIdentifier());
		if (found != null) {
			mainLayout.remove(found);
		}
	}

	@Override
	public void changePlaceHolder(IPlaceHolder placeHolder) {
		ResizePanel resizeAble = getResizePanel(placeHolder.getIdentifier());
		if (resizeAble != null) {
			fixPlaceHolder(placeHolder);

			resizeAble.setTop(placeHolder.getY());
			resizeAble.setLeft(placeHolder.getX());
			resizeAble.setWidth(placeHolder.getPixelWidth(), Unit.PIXELS);
			resizeAble.setHeight(placeHolder.getPixelHeight(), Unit.PIXELS);

			logger.debug(
					"placeholder id: {}, X: {}, Y: {}, Width: {}, Height {}", //
					placeHolder.getIdentifier(), //
					placeHolder.getX(), placeHolder.getY(), //
					placeHolder.getPixelWidth(), placeHolder.getPixelHeight()); //

			int height = placeHolder.getY() + placeHolder.getPixelHeight();
			if (height > canvasHeight) {
				changeCanvasHeight(height);
			}

		}

	}

	private ResizePanel getResizePanel(String identifier) {
		if (mainLayout == null)
			return null;
		Iterator<Component> i = mainLayout.getChildren().iterator();
		while (i.hasNext()) {
			ResizePanel resizePanel = (ResizePanel) i.next();
			Iterator<Component> iter = resizePanel.getChildren().iterator();
			while (iter.hasNext()) {
				Component cc = iter.next();
				if (cc instanceof IPlaceHolder) {
					if (((IPlaceHolder) cc).getIdentifier() == identifier) {
						return resizePanel;
					}
				}
			}
		}

		return null;
	}

	private void changeCanvasSize(int canonicalWidth) {
		// TODO(flow-migration): replace IMainUI usage with injected state/service.
		float aspectRatio = ((IMainUI) UI.getCurrent()).getAspectRatio();
		int canonicalHeight = (int) (canonicalWidth * aspectRatio);

		int currentHeight = canvasHeight;
		canvasHeight = (int) (canvasWidth * aspectRatio);
		logger.debug("changeCanvasSize width from {} to {}", canvasWidth,
				canonicalWidth);

		Iterator<Component> i = mainLayout.getChildren().iterator();
		while (i.hasNext()) {
			ResizePanel resizeAble = (ResizePanel) i.next();
			Iterator<Component> iter = resizeAble.getChildren().iterator();
			while (iter.hasNext()) {
				Component component = iter.next();
				if (component instanceof IPlaceHolder) {
					IPlaceHolder placeHolder = (IPlaceHolder) component;

					int x = resize(placeHolder.getX(), canvasWidth,
							canonicalWidth);
					int pixelWidth = resize(placeHolder.getPixelWidth(),
							canvasWidth, canonicalWidth);

					int y = resize(placeHolder.getY(), canvasHeight,
							canonicalHeight);
					int pixelHeight = resize(placeHolder.getPixelHeight(),
							canvasHeight, canonicalHeight);

					placeHolder.setX(x);
					placeHolder.setY(y);
					placeHolder.setPixelWidth(pixelWidth);
					placeHolder.setPixelHeight(pixelHeight);

					logger.debug(
							"placeholder id: {}, X: {}, Y: {}, Width: {}, Height {}", //
							placeHolder.getIdentifier(), //
							placeHolder.getX(),
							placeHolder.getY(), //
							placeHolder.getPixelWidth(),
							placeHolder.getPixelHeight()); //

					resizeAble.setTop(placeHolder.getY());
					resizeAble.setLeft(placeHolder.getX());
					resizeAble.setWidth(placeHolder.getPixelWidth(),
							Unit.PIXELS);
					resizeAble.setHeight(placeHolder.getPixelHeight(),
							Unit.PIXELS);
				}
			}
		}
		canvasWidth = canonicalWidth;
		canvasHeight = (int) (((float) currentHeight / (float) canvasHeight) * canonicalHeight);
		mainLayout.setWidth(canvasWidth, Unit.PIXELS);
		mainLayout.setHeight(canvasHeight, Unit.PIXELS);
	}

	private int resize(int dimension, int original, int canonical) {
		return Math
				.round((((float) dimension / (float) original) * canonical));
	}

	private void changeCanvasHeight(int value) {
		canvasHeight = value;
		mainLayout.setWidth(canvasWidth, Unit.PIXELS);
		mainLayout.setHeight(canvasHeight, Unit.PIXELS);
	}

	@Override
	public int getScrollLeft() {
		String value = mainPanel.getElement().getProperty("scrollLeft");
		return value == null ? 0 : Integer.parseInt(value);
	}

	@Override
	public void setScrollLeft(int scrollLeft) {
		// Bug #182 [VAADIN] runtime exception in WidgetDesigner
		if (scrollLeft < 0)
			return;
		mainPanel.getElement().setProperty("scrollLeft", scrollLeft);
	}

	@Override
	public int getScrollTop() {
		String value = mainPanel.getElement().getProperty("scrollTop");
		return value == null ? 0 : Integer.parseInt(value);
	}

	@Override
	public void setScrollTop(int scrollTop) {
		// Bug #182 [VAADIN] runtime exception in WidgetDesigner on setScrollTop
		if (scrollTop < 0)
			return;
		mainPanel.getElement().setProperty("scrollTop", scrollTop);
	}


}
