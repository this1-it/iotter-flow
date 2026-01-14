package it.thisone.iotter.persistence.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.PrivateOwned;
import org.eclipse.persistence.annotations.TimeOfDay;

import it.thisone.iotter.persistence.ifc.IMarker;

@Cacheable(false)
//@Cache(type = CacheType.SOFT, // Cache everything until the JVM decides memory is low.
//		expiryTimeOfDay = @TimeOfDay(hour = 3) // 3:00 AM
//)
@Entity
@Table(name = "FEED")
public class GraphicFeed extends BaseEntity implements IMarker {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GraphicFeed() {
		super();
	}

	@Column(name = "LABEL")
	private String label;

	private MeasureUnit measure;

	private ChartPlotOptions options;

	@PrivateOwned
	@OneToMany(orphanRemoval = true, mappedBy = "feed", fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	@OrderColumn(name = "POSITION")
	private List<ChartBand> bands;

	@PrivateOwned
	@OneToMany(orphanRemoval = true, mappedBy = "feed", fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	@OrderColumn(name = "POSITION")
	private List<ChartThreshold> thresholds;

	@OneToOne
	@JoinColumn(name = "CHANNEL_ID", referencedColumnName = "id")
	private Channel channel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GRAPH_WIDGET_ID")
	private GraphicWidget widget;

	@Column(name = "X")
	private float x;

	@Column(name = "Y")
	private float y;

	@Column(name = "META_DATA")
	private String metaData;

	@Column(name = "OID")
	private String oid;

	@Column(name = "SECTION")
	private String section;

	/**
	 * Id of the terminal managed resource.
	 */
	@Column(name = "RESOURCEID")
	private String resourceID;

	// Feature #1886
	@Column(name = "CHECKED")
	private boolean checked;
	
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public GraphicWidget getWidget() {
		return widget;
	}

	public void setWidget(GraphicWidget graph) {
		this.widget = graph;
	}

	public MeasureUnit getMeasure() {
		return measure;
	}

	public void setMeasure(MeasureUnit measure) {
		this.measure = measure;
	}

	public ChartPlotOptions getOptions() {
		if (options == null) {
			options = new ChartPlotOptions();
		}
		return options;
	}

	public void setOptions(ChartPlotOptions options) {
		this.options = options;
	}

	public Device getDevice() {
		if (getChannel() != null) {
			return getChannel().getDevice();
		}
		return null;
	}

	public String getKey() {
		if (getChannel() != null) {
			return getChannel().getKey();
		}
		return getMetaData();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getKey()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof GraphicFeed == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final GraphicFeed otherObject = (GraphicFeed) obj;
		return new EqualsBuilder().append(getKey(), otherObject.getKey()).isEquals();
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	
	public List<ChartBand> getBands() {
		if (bands == null)
			bands = new ArrayList<ChartBand>();
		return bands;
	}

	public void setBands(List<ChartBand> bands) {
		this.bands = bands;
	}

	public List<ChartThreshold> getThresholds() {
		if (thresholds == null)
			thresholds = new ArrayList<ChartThreshold>();
		return thresholds;
	}

	public void setThresholds(List<ChartThreshold> thresholds) {
		this.thresholds = thresholds;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public String getMarkerId() {
		return getKey();
	}

	public String getMetaData() {
		if (getChannel() != null) {
			if (getChannel().getMetaData() == null && metaData != null) {
				getChannel().setMetaData(metaData);
			}	
			return getChannel().getMetaData();
		}
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getResourceID() {
		return resourceID;
	}

	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	@Override
	public String toString() {
		return "GraphicFeed [metaData=" + getMetaIdentifier() + ", key=" + getKey() + ", section=" + section + ", resourceID="
				+ resourceID + "]";
	}
	
	@Transient
	public String getMetaIdentifier() {
		if (this.metaData != null) {
			int i = StringUtils.ordinalIndexOf(this.metaData,"|", 3 );
			if (i > 0) {
				return this.metaData.substring(0, i);
			}
		}
		return null;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}


}
