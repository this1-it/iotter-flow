package it.thisone.iotter.ui.providers;



import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.provisioning.AernetXLSXParserConstants;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseForm;

import it.thisone.iotter.ui.visualizers.ControlPanelBaseAdapter;

public class ControlPanelBaseProvider implements GraphicWidgetProvider, AernetXLSXParserConstants {

	public static final float[] DEFAULT_SIZE = new float[] {12, 6};

	@Override
	public String getName() {
		return CONTROLPANEL;
	}

	@Override
	public AbstractWidgetVisualizer getVisualizer(GraphicWidget widget) {
		return new ControlPanelBaseAdapter(widget);
	}



	@Override
	public int maxParameters() {
		return 0;
	}

	@Override
	public float[] defaultSize() {
		return new float[] {DEFAULT_SIZE[0],DEFAULT_SIZE[1]} ;
	}

	@Override
	public AbstractBaseEntityForm<GraphicWidget> getForm(GraphicWidget widget) {
		return new ControlPanelBaseForm(widget);
	}

}
