package it.thisone.iotter.ui.visualizers;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.IFrame;

import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;

public class WebPageAdapter extends AbstractWidgetVisualizer {

	private static final long serialVersionUID = -7774350046808287086L;

	public WebPageAdapter(GraphicWidget widget) {
		super(widget);
		Component visualization = buildVisualization();
		setRootComposition(visualization);
	}
	

	@Override
	protected Component buildVisualization() {
		String url = ((GraphicWidget) getWidget()).getUrl();
		
		IFrame component = new IFrame(url);
        component.setSizeFull();
		
		
		return component;
	}

	@Override
	public boolean refresh() {
		return false;
	}


	@Override
	public void draw() {
		// Flow synchronizes server-side state changes without explicit markAsDirty calls.
	}




	
}
