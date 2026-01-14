package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.PrivateOwned;
import org.eclipse.persistence.annotations.TimeOfDay;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.ifc.IMarker;
import it.thisone.iotter.persistence.ifc.IWidget;

@Cacheable(true)
@Cache(type = CacheType.SOFT, // Cache everything until the JVM decides memory is low.
expiryTimeOfDay=@TimeOfDay(hour=3) // 3:00 AM
//expiry = 3600000 // 60 minutes
)
@Entity
@Table(name = "GRAPH_WIDGET")
public class GraphicWidget extends BaseEntity implements IWidget {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GraphicWidget() {
		super();
	}

	@Column(name = "X")
	private float x;

	@Column(name = "Y")
	private float y;

	@Column(name = "WIDTH")
	private float width;

	@Column(name = "HEIGHT")
	private float height;

	@Column(name = "LABEL")
	private String label;

	@Column(name = "CONTAINER")
	private String container;

	@Column(name = "PARENT")
	private String parent;

	@Column(name = "DEVICE")
	private String device;

	@Lob
	@Column(name = "DESCRIPTION")
	@Basic(optional = false, fetch = FetchType.LAZY)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE")
	private GraphicWidgetType type;

	@Column(name = "PROVIDER")
	private String provider;

	private GraphicWidgetOptions options;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GROUP_WIDGET_ID")
	private GroupWidget groupWidget;

	@PrivateOwned
	@OneToMany(orphanRemoval = true, mappedBy = "widget", fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	@OrderColumn(name = "POSITION")
	private List<GraphicFeed> feeds;

	@PrivateOwned
	@OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private ImageData image;

	@Column(name = "URL")
	private String url;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getId());
		sb.append(" ");
		sb.append(getLabel());
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getId()).append(getLabel()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof GraphicWidget == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final GraphicWidget otherObject = (GraphicWidget) obj;
		return new EqualsBuilder().append(getId(), otherObject.getId()).append(getType(), otherObject.getType())
				.append(getLabel(), otherObject.getLabel()).isEquals();
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public GraphicWidgetType getType() {
		return type;
	}

	public void setType(GraphicWidgetType type) {
		this.type = type;
	}

	public GroupWidget getGroupWidget() {
		return groupWidget;
	}

	public void setGroupWidget(GroupWidget groupWidget) {
		this.groupWidget = groupWidget;
	}

	public boolean addFeed(GraphicFeed feed) {
		if (feed != null && !getFeeds().contains(feed)) {
			feed.setWidget(this);
			feed.setOwner(getOwner());
			getFeeds().add(feed);
			return true;
		}
		return false;
	}

	public List<GraphicFeed> getFeeds() {
		if (feeds == null) {
			feeds = new ArrayList<GraphicFeed>();
		}
		return feeds;
	}

	public void setFeeds(List<GraphicFeed> feeds) {
		for (GraphicFeed feed : feeds) {
			feed.setWidget(this);
			feed.setOwner(getOwner());
		}
		this.feeds = feeds;
	}

	public GraphicWidgetOptions getOptions() {
		if (options == null) {
			options = new GraphicWidgetOptions();
		}
		return options;
	}

	public void setOptions(GraphicWidgetOptions options) {
		this.options = options;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public List<GraphicWidget> findChildren(List<GraphicWidget> items) {
		List<GraphicWidget> children = new ArrayList<GraphicWidget>();
		if (getContainer() == null) {
			return children;
		}
		for (GraphicWidget item : items) {
			if (getContainer().equals(item.getParent())) {
				children.add(item);
			}
		}
		return children;
	}

	@Override
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	@Override
	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	@Override
	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public ImageData getImage() {
		return image;
	}

	public void setImage(ImageData image) {
		this.image = image;
	}

	public List<IMarker> getIMarkers() {
		List<IMarker> markers = new ArrayList<>();
		for (GraphicFeed feed : getFeeds()) {
			markers.add(feed);
		}
		return markers;
	}

	public boolean hasExtremes() {
		for (GraphicFeed feed : getFeeds()) {
			MeasureRange extremes = feed.getOptions().getExtremes();
			if (extremes != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Bug #328: Mancata disassociazione strumento
	 */
	public void removeOrphanFeeds() {
		try {
			List<GraphicFeed> orphans = new ArrayList<>();
			for (GraphicFeed feed : getFeeds()) {
				Device device = feed.getDevice();
				if (!getGroupWidget().getNetwork().equals(device.getNetwork())) {
					orphans.add(feed);
				}
			}
			getFeeds().removeAll(orphans);
		} catch (Exception e) {

		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

}
