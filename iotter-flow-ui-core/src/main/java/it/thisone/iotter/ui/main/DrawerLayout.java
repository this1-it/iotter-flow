package it.thisone.iotter.ui.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.themes.ValoTheme;

import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.eventbus.CloseOpenWindowsEvent;

public class DrawerLayout extends Div{
/*
	private VerticalLayout content;
	public DrawerLayout() {
		super();
		showBottom();
		close();
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		content = new VerticalLayout();
		content.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		layout.addComponent(content);
		layout.setExpandRatio(content, 1f);
		addComponent(layout);
		
	}
*/	
	
	private PanelFlow panel;
	
	public DrawerLayout() {
		super();
		setResponsive(true);
		close();
		panel = new PanelFlow();
		panel.setStyleName(ValoTheme.PANEL_BORDERLESS);
		panel.setSizeFull();
		addComponent(panel);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void close() {
		if (isEnabled()) {
			setEnabled(false);
			removeStyleName("drawer-visible");
		}
		UIUtils.getUIEventBus().post(new CloseOpenWindowsEvent());
	}
	
	public void open() {
		if (!isEnabled()) {
			setEnabled(true);
			addStyleName("drawer-visible");
		}
	}
	
	public void setContent(Component content) {
		panel.setContent(content);
	}
	
	public String getContentId() {
		return panel.getContent()!=null ? panel.getContent().getId():null;
	}
	
	public void removeAllStyles() {
		removeStyleName("drawer-visible");
		removeStyleName("drawer-wrapper");
		removeStyleName("drawer");
		removeStyleName("drawer-sidebar-wrapper");
		removeStyleName("drawer-sidebar");
	}
	
	public void showBottom() {
		removeAllStyles();
		setStyleName("drawer-wrapper");
		addStyleName("drawer");
	}	
	
	public void showSide() {
		removeAllStyles();
		setStyleName("drawer-sidebar-wrapper");
		addStyleName("drawer-sidebar");
	}	

}
