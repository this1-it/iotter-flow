package it.thisone.iotter.ui.common;

import com.vaadin.server.FontIcon;

import it.thisone.iotter.enums.GraphicWidgetType;

public class WidgetTypeInstance {
	private GraphicWidgetType type;
	private String provider;
	private FontIcon icon;
	public WidgetTypeInstance(GraphicWidgetType type, String provider, FontIcon icon) {
		super();
		this.type = type;
		this.provider = provider;
		this.icon = icon;
	}
	public GraphicWidgetType getType() {
		return type;
	}
	public void setType(GraphicWidgetType type) {
		this.type = type;
	}
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public FontIcon getIcon() {
		return icon;
	}
	public void setIcon(FontIcon icon) {
		this.icon = icon;
	}
}
