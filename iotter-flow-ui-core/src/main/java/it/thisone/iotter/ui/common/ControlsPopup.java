package it.thisone.iotter.ui.common;

import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Embedded;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.PopupView;

public class ControlsPopup extends PanelFlow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3868360389375898886L;
	private PopupView popview;
	private Component content;

	@SuppressWarnings("serial")
	public ControlsPopup(Component component) {
		super();
		content = component;
		popview = new PopupView(new PopupContent());
		popview.setHideOnMouseOut(false);
		// setImmediate(true);
		HorizontalLayout hl = new HorizontalLayout();
		Embedded icon = new Embedded();
		icon.setSource(new ThemeResource("img/cog.png"));
		icon.addClickListener(new ClickListener() {
		    @Override
		    public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
		    	popview.setPopupVisible(true);
		    }
		});
		hl.addComponent(icon);
		hl.addComponent(popview);		
		
		setWidth("40px");
		setHeight("40px");
		setContent(hl);
	}

	class PopupContent implements PopupView.Content {
		private static final long serialVersionUID = 1634116040954182604L;

		@Override
		public final Component getPopupComponent() {
			return buildPopup();
		}

		@Override
		public final String getMinimizedValueAsHTML() {
			return ""; // show nothing
		}
	}

	public Component buildPopup() {
		return content;
	};

}
