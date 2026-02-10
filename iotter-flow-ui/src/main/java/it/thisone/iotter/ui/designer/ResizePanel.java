package it.thisone.iotter.ui.designer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;

/**
 * @author Vittorio De Zuane
 */
@SuppressWarnings("serial")
public class ResizePanel extends Div {

	private final Map<ResizedListener, Registration> resizedRegistrations = new HashMap<>();
	private final Map<MovedListener, Registration> movedRegistrations = new HashMap<>();
	private float leftPx;
	private float topPx;
	private float widthPx;
	private float heightPx;

	public ResizePanel() {
		getStyle().set("position", "absolute");
	}

	public void addComponent(Component c) {
		add(c);
	}

	public void removeComponent(Component c) {
		remove(c);
	}

	public void replaceComponent(Component oldComponent, Component newComponent) {
		// TODO(flow-migration): replaceComponent requires ordered layout semantics in Flow.
	}

	public float getLeft() {
		return leftPx;
	}

	public float getTop() {
		return topPx;
	}

	public void setLeft(float left) {
		leftPx = left;
		getStyle().set("left", left + "px");
	}

	public void setTop(float top) {
		topPx = top;
		getStyle().set("top", top + "px");
	}

	public void setBoundaryPanel(Div layout) {
		// TODO(flow-migration): absolute layout emulation - verify container positioning rules.
		layout.getStyle().set("position", "relative");
	}

	@Override
	public void setWidth(float width, Unit unit) {
		super.setWidth(width, unit);
		if (unit == Unit.PIXELS) {
			widthPx = width;
		}
	}

	@Override
	public void setHeight(float height, Unit unit) {
		super.setHeight(height, unit);
		if (unit == Unit.PIXELS) {
			heightPx = height;
		}
	}

	public float getWidthPx() {
		return widthPx;
	}

	public float getHeightPx() {
		return heightPx;
	}

	protected void fireResized() {
		fireEvent(new ResizedEvent(this));
	}

	protected void fireMoved() {
		fireEvent(new MovedEvent(this));
	}

	public static class ResizedEvent extends ComponentEvent<ResizePanel> {
		public ResizedEvent(ResizePanel source) {
			super(source, false);
		}

		public float getWidth() {
			return getSource().getWidthPx();
		}

		public float getHeight() {
			return getSource().getHeightPx();
		}
	}

	public static class MovedEvent extends ComponentEvent<ResizePanel> {
		public MovedEvent(ResizePanel source) {
			super(source, false);
		}

		public float getX() {
			return getSource().getLeft();
		}

		public float getY() {
			return getSource().getTop();
		}
	}

	public interface ResizedListener extends ComponentEventListener<ResizedEvent>, Serializable {
		@Override
		default void onComponentEvent(ResizedEvent event) {
			onResized(event);
		}

		void onResized(ResizedEvent event);
	}

	public interface MovedListener extends ComponentEventListener<MovedEvent>, Serializable {
		@Override
		default void onComponentEvent(MovedEvent event) {
			onMoved(event);
		}

		void onMoved(MovedEvent event);
	}

	public void addResizedListener(ResizedListener listener) {
		Registration registration = addListener(ResizedEvent.class, listener);
		resizedRegistrations.put(listener, registration);
	}

	public void addMovedListener(MovedListener listener) {
		Registration registration = addListener(MovedEvent.class, listener);
		movedRegistrations.put(listener, registration);
	}

	public void removeResizedListener(ResizedListener listener) {
		Registration registration = resizedRegistrations.remove(listener);
		if (registration != null) {
			registration.remove();
		}
	}

	public void removeMovedListener(MovedListener listener) {
		Registration registration = movedRegistrations.remove(listener);
		if (registration != null) {
			registration.remove();
		}
	}

    public Iterator<Component> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

}
