package it.thisone.iotter.ui.providers;

import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;

public interface GraphicWidgetProvider {

	public String getName();
	
	public AbstractWidgetVisualizer getVisualizer(GraphicWidget widget);
	

	public AbstractBaseEntityForm<GraphicWidget> getForm(GraphicWidget widget);

	
	public int maxParameters();

	public float[] defaultSize();
	
	
}
