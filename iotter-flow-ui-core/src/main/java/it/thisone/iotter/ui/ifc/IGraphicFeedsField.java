package it.thisone.iotter.ui.ifc;

import java.util.List;

import com.vaadin.flow.component.Component;

import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;

public interface IGraphicFeedsField extends Component {
	public void setGraphicWidget(GraphicWidget graph);
	public int getMaxParameters();
	public List<GraphicFeed> getValue();
	//public String getRequiredError();
	public boolean isConfigured();
}
