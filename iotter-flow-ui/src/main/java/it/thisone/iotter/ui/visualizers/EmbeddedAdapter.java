package it.thisone.iotter.ui.visualizers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;

import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.IExportable;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;
import it.thisone.iotter.ui.maps.GraphFeedsImageOverlayMap;

public class EmbeddedAdapter extends AbstractWidgetVisualizer implements IExportable {

	private static final long serialVersionUID = -7774350046808287086L;
	private GraphFeedsImageOverlayMap map;

	public EmbeddedAdapter(GraphicWidget widget) {
		super(widget);
		Component visualization = buildVisualization();
		setRootComposition(visualization);
	}
	

	@Override
	protected Component buildVisualization() {
		
		if (((GraphicWidget) getWidget()).getFeeds().isEmpty()) {
			ImageData embedded = ((GraphicWidget) getWidget()).getImage();
			return embeddedImage(embedded);
		}
		
		map = new GraphFeedsImageOverlayMap( //
				(GraphicWidget) getWidget(), //
				-1, -1, false);
		map.refresh();

		return map;
	}

	public static Image embeddedImage(ImageData embedded) {
		Image image = new Image();
		image.addClassName("embedded-image");
		try {
			image.setSrc(createStreamResource(embedded));
			image.setId(embedded.getFilename());
		} catch (Exception ex) {
			image.setSrc("img/empty-image.png");
		}
		return image;

	}

	@Override
	public boolean refresh() {
		if (map == null) {
			return false;
		}
		map.refresh();
		return true;
	}



	private static StreamResource createStreamResource(final ImageData imageData) {
		return new StreamResource(imageData.getFilename(), () -> createStream(imageData));
	}

	private static InputStream createStream(final ImageData imageData) {
		return new ByteArrayInputStream(imageData.getData());
	}

	@Override
	public void draw() {
		// Flow synchronizes server-side state changes without explicit markAsDirty calls.
	}
	
	@Override
	public ExportConfig createExportConfig() {
		ExportConfig config = new ExportConfig();
		config.setName(getWidget().getLabel());
		//List<GraphicFeed> feeds = ((GraphicWidget) getWidget()).getFeeds();
		// config.setFeeds(ChartUtils.createExportFeeds(feeds));
		// config.setInterpolation(Interpolation.RAW);
		return config;
	}

}
