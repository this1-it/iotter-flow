package it.thisone.iotter.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "FEED_THRESHOLD")
public class ChartThreshold extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4157568602604560765L;
	
	@Column(name = "VALUE")
	private Float value;

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
		if (obj instanceof ChartThreshold == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final ChartThreshold otherObject = (ChartThreshold) obj;
		return new EqualsBuilder().append(getId(), otherObject.getId()) //
				.append(getValue(), otherObject.getValue()).isEquals();
    }
	
	
	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
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
	
}
