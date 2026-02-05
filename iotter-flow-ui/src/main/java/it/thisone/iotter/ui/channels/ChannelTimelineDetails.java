package it.thisone.iotter.ui.channels;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.charts.MultiTraceChartAdapter;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.model.TimeInterval;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChannelTimelineDetails extends  Composite<VerticalLayout> {

	@Autowired
	private CassandraService cassandraService;
	
	private static final long serialVersionUID = 1L;

	public ChannelTimelineDetails(Channel item) {
		super();
		getContent().add(buildContent(item));
	}

	private com.vaadin.flow.component.Component buildContent(Channel channel) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setPadding(true);
		mainLayout.setSizeFull();

		GraphicWidget widget = new GraphicWidget();
		widget.setType(GraphicWidgetType.MULTI_TRACE);
		widget.setLabel(channel.getConfiguration().getDisplayName());
		widget.getOptions().setScale(ChartScaleType.LINEAR);
		widget.getOptions().setShowGrid(false);
		widget.getOptions().setExporting(false);
		widget.getOptions().setRealTime(false);
		widget.getOptions().setZoomable(false);

		GraphicFeed feed = new GraphicFeed();
		feed.getOptions().setFillColor(ChartUtils.hexColor(0));
		feed.setChannel(channel);
		feed.setMeasure(channel.getDefaultMeasure());
		widget.addFeed(feed);

		MultiTraceChartAdapter chartAdapter = new MultiTraceChartAdapter(widget);
//		chartAdapter.setHeightFull();
//		chartAdapter.setWidthFull();

		if (channel.getConfiguration().getActivationDate() != null) {
			Date first =cassandraService.getMeasures()
					.getFirstTick(channel.getDevice().getSerial(), null);
			if (first != null && first.before(channel.getConfiguration().getActivationDate())) {
				first = channel.getConfiguration().getActivationDate();
			}
			Date last = channel.getConfiguration().getDeactivationDate() != null
					? channel.getConfiguration().getDeactivationDate()
					: new Date();
			TimeInterval interval = new TimeInterval(channel.getConfiguration().getActivationDate(), last);
			chartAdapter.setTimeInterval(interval);
		}

		chartAdapter.register();
		mainLayout.add(chartAdapter);
		mainLayout.setFlexGrow(1f, chartAdapter);
		return mainLayout;
	}


}
