package it.thisone.iotter.ui.graphicfeeds;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.fields.ChannelAcceptor;
import it.thisone.iotter.ui.ifc.IGraphicFeedsField;

public class GraphicFeedWindRoseField extends CustomField<List<GraphicFeed>> implements IGraphicFeedsField {

    private static final long serialVersionUID = -2936983441775994254L;

    private List<GraphicFeed> currentValue;
    private final GraphicFeedListing speedListing;
    private final GraphicFeedListing directionListing;
    private final VerticalLayout content;
    private int maxParameters = 2;

    private final ChannelAcceptor acceptSpeed;
    private final ChannelAcceptor acceptDirection;

    public GraphicFeedWindRoseField() {
        speedListing = new GraphicFeedListing();
        speedListing.setId("speed");
        speedListing.setMaxSize(1);

        directionListing = new GraphicFeedListing();
        directionListing.setId("direction");
        directionListing.setMaxSize(1);

        content = new VerticalLayout(speedListing, directionListing);
        content.setSizeFull();
        content.setFlexGrow(1f, speedListing, directionListing);

        acceptSpeed = new ChannelAcceptor() {
            @Override
            public boolean accept(Channel channel) {
                return isSpeed(channel);
            }
        };
        acceptDirection = new ChannelAcceptor() {
            @Override
            public boolean accept(Channel channel) {
                return isDirection(channel);
            }
        };
    }

    @Override
    protected List<GraphicFeed> generateModelValue() {
        return currentValue;
    }

    @Override
    protected void setPresentationValue(List<GraphicFeed> feeds) {
        currentValue = feeds;
        List<GraphicFeed> source = feeds != null ? feeds : new ArrayList<>();
        List<GraphicFeed> speeds = new ArrayList<>();
        List<GraphicFeed> directions = new ArrayList<>();

        for (GraphicFeed feed : source) {
            if (acceptDirection.accept(feed.getChannel())) {
                directions.add(feed);
            }
            if (acceptSpeed.accept(feed.getChannel())) {
                speeds.add(feed);
            }
        }

        speedListing.setFeeds(speeds);
        directionListing.setFeeds(directions);
    }

    protected Component initContent() {
        return content;
    }

    @Override
    public boolean isConfigured() {
        List<String> errors = new ArrayList<>();
        if (speedListing.getFeeds().isEmpty()) {
            errors.add(getTranslation("validators.windrose.missing_speed"));
        }
        if (directionListing.getFeeds().isEmpty()) {
            errors.add(getTranslation("validators.windrose.missing_direction"));
        }
        if (errors.isEmpty()) {
            GraphicFeed speed = speedListing.getFeeds().get(0);
            GraphicFeed dir = directionListing.getFeeds().get(0);
            if (!speed.getDevice().equals(dir.getDevice())) {
                errors.add(getTranslation("validators.windrose.different_device"));
            }
        }
        return errors.isEmpty();
    }

    public void setGraphicWidget(GraphicWidget graph) {
        setRequiredIndicatorVisible(true);

        List<GraphicFeed> speeds = new ArrayList<>();
        List<GraphicFeed> directions = new ArrayList<>();

        for (GraphicFeed feed : graph.getFeeds()) {
            if (acceptDirection.accept(feed.getChannel())) {
                directions.add(feed);
            }
            if (acceptSpeed.accept(feed.getChannel())) {
                speeds.add(feed);
            }
        }
        speedListing.setMaxSize(1);
        speedListing.setGraphicWidget(graph, speeds, acceptSpeed);
        speedListing.setVisibleColumns("device", "channel", "measure", "thresholds");
        speedListing.setColumnHeader("thresholds", speedListing.getI18nLabel("categories"));
        speedListing.setTitle(speedListing.getI18nLabel("windrose.speed"));

        directionListing.setMaxSize(1);
        directionListing.setGraphicWidget(graph, directions, acceptDirection);
        directionListing.setTitle(directionListing.getI18nLabel("windrose.direction"));
    }

    public int getMaxParameters() {
        return maxParameters;
    }

    public void setMaxParameters(int maxParameters) {
        this.maxParameters = maxParameters;
    }
}
