package it.thisone.iotter.ui.visualizers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.IExportable;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.common.MarkupsUtils;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;
import org.vaadin.flow.components.HtmlSpan;

public class LastMeasureAdapter extends AbstractWidgetVisualizer implements IExportable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1239240505211683641L;
	private SimpleDateFormat sdf;
	private HtmlSpan measureLabel;
	private static Logger logger = LoggerFactory
			.getLogger(LastMeasureAdapter.class);

	private ChannelAdapterDataProvider container;

	
	public LastMeasureAdapter(GraphicWidget widget) {
		super(widget);
		TimeZone tz = widget.getGroupWidget().getTimeZone();
		if (tz == null) {
			tz = UIUtils.getBrowserTimeZone();
		}
		container = new ChannelAdapterDataProvider();
		container.addFeeds(widget.getFeeds());

		
		sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss ZZZ");
		sdf.setTimeZone(tz);
		measureLabel = new HtmlSpan();
		
		// measureLabel.setImmediate(true);
		setRootComposition(buildVisualization());
	}

	@Override
	protected Component buildVisualization() {
		Component component = new Span("Missing Parameters");
		if (((GraphicWidget) getWidget()).getFeeds().isEmpty()) {
			return component;
		}

		Object parent = ((GraphicWidget) getWidget()).getParent();
		if (parent != null) {
			measureLabel.setWidthFull();
		} else {
			String width = null;
			if (width != null && !width.isEmpty()) {
				measureLabel.setWidth(width);
			}
		}
		if (((GraphicWidget) getWidget()).getType().equals(
				GraphicWidgetType.LAST_MEASURE_TABLE)) {
			component = buildMeasures();
		} else {
			component = buildMeasure();
		}

		refresh();
		
		VerticalLayout outer = new VerticalLayout();
		outer.setSizeFull();
		outer.addClassName("graph-widget-outer");
		outer.add(component);
		return outer;

		
		//return component;
	}

	private Component buildMeasures() {
		measureLabel.addClassName("station-label");
		return measureLabel;
	}

	private Component buildMeasure() {
		GraphicFeed feed = ((GraphicWidget) getWidget()).getFeeds().get(0);

		String color = feed.getOptions().getFillColor();

		measureLabel = new HtmlSpan("");
		measureLabel.setId("measure" + feed.getId());
		measureLabel.getElement().getStyle().set("color", color);
		measureLabel.getElement().getStyle().set("overflow", "visible");
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(false);
		layout.setSizeFull();
		String label = ChannelUtils.displayName(feed.getChannel());
		if (feed.getLabel() != null && !feed.getLabel().isEmpty()) {
			label = feed.getLabel();
		}
		Span feedLabel = new Span(label);
		feedLabel.addClassName("station-label");
		feedLabel.setId("label" + feed.getId());
		feedLabel.getElement().getStyle().set("color", color);
		layout.add(feedLabel);

		layout.add(measureLabel);
		layout.setFlexGrow(1f, measureLabel);

		return layout;
	}

	@Override
	public boolean refresh() {
		if (((GraphicWidget) getWidget()).getFeeds().isEmpty()) return false;
		container.refresh();
		Date lastDate = container.getLastDate();
		String description = "";
		if (((GraphicWidget) getWidget()).getType().equals(
				GraphicWidgetType.LAST_MEASURE_TABLE)) {
			String html = MarkupsUtils.toHtml(container);
			measureLabel.setInnerHtml(html);
			description = ((GraphicWidget) getWidget()).getLabel();
		} else {
			GraphicFeed feed = ((GraphicWidget) getWidget()).getFeeds().get(0);
			
			ChannelAdapter item = container.getAdapter(feed.getKey());
			if (item != null) {
				String html = String.format("%s %s", item.getLastMeasureValue(), item.getMeasureUnit());
				measureLabel.setText(html);
			}
 
			
//			String unit = ChartUtils.getUnitOfMeasure(feed);
//			String label = feed.getLabel();
//			if (label == null)
//				label = "";
//			description = String.format("%s %s", label, unit);
//			MeasureRaw measure = ChartUtils.lastMeasure(feed.getKey());
//			MeasureUnit measureUnit = feed.getMeasure();
//			DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
//					.getInstance(UIUtils.getLocale());
//			decimalFormat.applyPattern(measureUnit.normalizedFormat());
//
//			String value = "...";
//			try {
//				if (measure != null) {
//					lastDate = measure.getDate();
//					Double number = (Double) ChartUtils.calculateMeasure(
//							measure.getValue(), measureUnit);
//					value = decimalFormat.format(number);
//				}
//			} catch (MeasureException e) {
//			}
//
//			String html = String.format("%s %s", value, unit);
//			measureLabel.setValue(html);
		}

		if (lastDate != null) {
			String tooltip = String.format("<b>%s</b> %s",
					sdf.format(lastDate), description);
			measureLabel.getElement().setProperty("title", tooltip);
		}
		
		logger.debug("refreshed label adapter {}", description);

		return true;
	}


	
	@Override
	public void draw() {
		// Flow synchronizes server-side state changes without explicit markAsDirty calls.
	}

	@Override
	public ExportConfig createExportConfig() {
		ExportConfig config = new ExportConfig();
		config.setName(getWidget().getLabel());
		// List<GraphicFeed> feeds = ((GraphicWidget) getWidget()).getFeeds();
		// config.setFeeds(ChartUtils.createExportFeeds(feeds));
		// config.setInterpolation(Interpolation.RAW);
		return config;
	}

}
