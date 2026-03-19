package it.thisone.iotter.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "FEED_BAND")
public class ChartBand extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4157568602604560765L;

	private MeasureRange range;
	
	@Column(name = "LABEL")
	private String label;

	@Column(name = "FILL_COLOR")
	private String fillColor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FEED_ID")
	private GraphicFeed feed;

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ChartBand == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final ChartBand otherObject = (ChartBand) obj;
		return new EqualsBuilder().append(getId(), otherObject.getId()).isEquals();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public GraphicFeed getFeed() {
		return feed;
	}

	public void setFeed(GraphicFeed feed) {
		this.feed = feed;
	}

	public MeasureRange getRange() {
		if (range == null) {
			range = new MeasureRange();
		}
		return range;
	}

	public void setRange(MeasureRange range) {
		this.range = range;
	}


	
}
