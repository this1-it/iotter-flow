package it.thisone.iotter.ui.maps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.vaadin.addons.componentfactory.leaflet.layer.ui.marker.Marker;
import org.vaadin.addons.componentfactory.leaflet.types.DivIcon;
import org.vaadin.addons.componentfactory.leaflet.types.Point;

import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;
import it.thisone.iotter.util.MapUtils;

public class GraphFeedsImageOverlayMap extends ImageOverlayMap {

	private GraphicWidget entity;
	private static final long serialVersionUID = 1L;
	private SimpleDateFormat sdf;
	
	private ChannelAdapterDataProvider dataProvider;


	public GraphFeedsImageOverlayMap(GraphicWidget entity, float mapWidth, float mapHeight, boolean editable) {
		super(entity.getImage(),entity.getIMarkers(), editable);
		this.entity = entity;
		TimeZone tz = entity.getGroupWidget().getTimeZone();
		if (tz == null) {
			tz = TimeZone.getDefault();
		}
		sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss ZZZ");
		sdf.setTimeZone(tz);
		dataProvider = new ChannelAdapterDataProvider();
		dataProvider.addFeeds(entity.getFeeds());
		initContent(mapWidth, mapHeight);
	}
	
	private GraphicFeed getFeed(String markerId) {
		for (GraphicFeed feed : entity.getFeeds()) {
			if (feed.getMarkerId().equals(markerId))
				return feed;
		}
		return null;
	}


	@Override
	protected Marker createMarker(Point point, String markerId) {
		GraphicFeed feed = getFeed(markerId);
		if (feed == null) {
			return null;
		}
		ChannelAdapter adapter = dataProvider.getAdapter(feed.getKey());
		
		Marker marker = new Marker(new org.vaadin.addons.componentfactory.leaflet.types.LatLng(point.getX(), point.getY()));
		DivIcon icon = new DivIcon(MapUtils.divIcon(feed, adapter.getLabel(), "..."));
		icon.setIconSize(new Point(5, 5));
		marker.setIcon(icon);
		marker.bindPopup(adapter.getDisplayName());
		return marker;
	}

	@Override
	protected void setImage(ImageData image) {
		entity.setImage(image);
	}
	
	public void refresh() {
		dataProvider.refresh();
		for (ChannelAdapter adapter : dataProvider.getItems()) {
			String markerId = adapter.getKey();
			String unit = adapter.getMeasureUnit();
			String number = adapter.getLastMeasureValue();
			Date date = adapter.getLastMeasureDate();
			String displayName = adapter.getDisplayName();
			String label = adapter.getLabel();
			String timeStamp = "";
			if (date != null) {
				timeStamp = sdf.format(date);
			}
			GraphicFeed feed = getFeed(markerId);
			Marker leafLetMarker = getMarker(markerId);
			if (leafLetMarker != null) {
				String popup = String.format("%s<br/>%s", displayName, timeStamp);
				String value = String.format("%s %s", number, unit);
				DivIcon icon = new DivIcon(MapUtils.divIcon(feed, label, value));
				icon.setIconSize(new Point(5, 5));
				leafLetMarker.setIcon(icon);
				leafLetMarker.bindPopup(popup);
			}
		}
	}
	
	

}
