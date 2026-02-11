package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;



import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import org.eclipse.persistence.annotations.PrivateOwned;
import org.eclipse.persistence.annotations.TimeOfDay;

@Cacheable(false)
//@Cache(type = CacheType.SOFT, // Cache everything until the JVM decides memory is low.
//expiryTimeOfDay=@TimeOfDay(hour=3) // 3:00 AM
//)

@Entity
@Indexes({
		@Index(name = "GROUP_WIDGET_OWNER_INDEX", columnNames = { "OWNER" }),
		@Index(name = "GROUP_WIDGET_DEVICE_INDEX", columnNames = { "DEVICE",
				"EXTERNAL_ID" }) })
@Table(name = "GROUP_WIDGET")
public class GroupWidget extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GroupWidget() {
		super();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof GroupWidget == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final GroupWidget otherObject = (GroupWidget) obj;
		// TODO
		return new EqualsBuilder().append(this.getId(), otherObject.getId())
				.isEquals();
	}

	@Transient
	public Network getNetwork() {
		if (getGroup() != null) {
			return getGroup().getNetwork();
		}
		return null;
	}

	@Column(name = "NAME")
	private String name;

	@Column(name = "DEVICE")
	private String device;

	// Feature #213 (In Progress): FTP D32 importer must put an extra-key to
	// identifies visualization name
	@Column(name = "EXTERNAL_ID")
	private String externalId;
	
	@Column(name = "CREATOR")
	private String creator;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ID")
	private GroupWidget parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "NETWORK_GROUP_ID")
	private NetworkGroup group;

	@PrivateOwned
	@OneToMany(mappedBy = "groupWidget", fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	@OrderColumn(name = "POSITION")
	private List<GraphicWidget> widgets;

	private GroupWidgetOptions options;

	@javax.persistence.Lob
	@Column(name = "LAYOUT")
	private String layout;
	
	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public GroupWidget getParent() {
		return parent;
	}

	public void setParent(GroupWidget parent) {
		this.parent = parent;
	}


	public NetworkGroup getGroup() {
		return group;
	}

	public void setGroup(NetworkGroup group) {
		this.group = group;
	}


	public List<GraphicWidget> getWidgets() {
		if (widgets == null) {
			widgets = new ArrayList<GraphicWidget>();
		}
		return widgets;
	}

	public void setWidgets(List<GraphicWidget> widgets) {
		for (GraphicWidget widget : widgets) {
			widget.setGroupWidget(this);
			widget.setOwner(getOwner());
		}
		this.widgets = widgets;
	}

	public void addGraphWidgets(List<GraphicWidget> widgets) {
		for (GraphicWidget widget : widgets) {
			addGraphWidget(widget);
		}
	}

	public void addGraphWidget(GraphicWidget widget) {
		if (!getWidgets().contains(widget)) {
			widget.setId(null);
			widget.setGroupWidget(this);
			widget.setOwner(getOwner());
			getWidgets().add(widget);
		}
	}

	public void removeGraphWidgets(List<String> removed) {
		ArrayList<GraphicWidget> widgets = new ArrayList<GraphicWidget>();
		for (GraphicWidget widget : getWidgets()) {
			if (!removed.contains(widget.getId())) {
				widgets.add(widget);
			}
		}
		setWidgets(widgets);
	}

	public TimeZone getTimeZone() {
		TimeZone tz = null;
		if (getNetwork() != null) {
			String id = getNetwork().getTimeZone();
			if (id != null) {
				tz = TimeZone.getTimeZone(id);
			}
		}
		return tz;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	public boolean isExclusive() {
		return (device != null && externalId != null);
	}
	
	public boolean isAutomaticDefault() {
		if (device == null || externalId == null) {
			return false;
		}
		return device.equals(externalId);
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}
	

	public Collection<Device> getAssociatedDevices() {
		Set<Device> devices = new HashSet<Device>();
		for (GraphicWidget widget : getWidgets()) {
			for (GraphicFeed feed : widget.getFeeds()) {
				if (feed.getChannel() != null) {
					devices.add(feed.getChannel().getDevice());
				}
			}
		}
		return Collections.unmodifiableCollection(devices);
	}

	public boolean isAuthor(String user) {
		if (getOwner() != null && user.equals(getOwner())){
			return true;
		}
		if (getCreator() != null && user.equals(getCreator())){
			return true;
		}
		return false;
	}

	public GroupWidgetOptions getOptions() {
		if (options == null) {
			options = new GroupWidgetOptions();
		}
		return options;
	}

	public void setOptions(GroupWidgetOptions options) {
		this.options = options;
	}
	
}
