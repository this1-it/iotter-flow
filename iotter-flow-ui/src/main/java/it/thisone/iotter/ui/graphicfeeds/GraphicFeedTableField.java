package it.thisone.iotter.ui.graphicfeeds;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.customfield.CustomField;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;

import it.thisone.iotter.ui.common.fields.ChannelAcceptor;
import it.thisone.iotter.ui.graphicwidgets.GraphicWidgetFactory;
import it.thisone.iotter.ui.ifc.IGraphicFeedsField;

public class GraphicFeedTableField extends CustomField<List<GraphicFeed>> implements IGraphicFeedsField {

    private static final long serialVersionUID = -2936983441775994254L;

    private List<GraphicFeed> currentValue;
    private final GraphicFeedListing content;
    private int maxParameters = -1;

    public GraphicFeedTableField() {
        content = new GraphicFeedListing();
    }

    @Override
    protected List<GraphicFeed> generateModelValue() {
        return currentValue;
    }

    @Override
    protected void setPresentationValue(List<GraphicFeed> newValue) {
        currentValue = newValue;
        List<GraphicFeed> feeds = new ArrayList<>();
        if (newValue != null) {
            feeds.addAll(newValue);
        }
        content.setFeeds(feeds);
    }

    protected Component initContent() {
        return content;
    }

    @Override
    public boolean isConfigured() {
        int size = content.getFeeds().size();
        return size > 0 && size <= getMaxParameters();
    }

    public String getRequiredError() {
        String errorMsg = getTranslation("validators.graph_feeds_size");
        return " 0 < " + errorMsg + " <= " + maxParameters;
    }

    public void setGraphicWidget(GraphicWidget widget) {
        maxParameters = GraphicWidgetFactory.maxParameters(widget);
        setRequiredIndicatorVisible(maxParameters > 0);
        if (widget.getType().equals(GraphicWidgetType.EMBEDDED)) {
            setRequiredIndicatorVisible(isConfigured());
        }
        content.setMaxSize(maxParameters);
        content.setGraphicWidget(widget, widget.getFeeds(), new ChannelAcceptor());
    }

    public int getMaxParameters() {
        return maxParameters;
    }

    public void setMaxParameters(int maxParameters) {
        this.maxParameters = maxParameters;
    }
}
