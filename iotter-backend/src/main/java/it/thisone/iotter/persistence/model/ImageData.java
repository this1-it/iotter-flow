package it.thisone.iotter.persistence.model;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;
import org.eclipse.persistence.annotations.TimeOfDay;

@Cache(type = CacheType.WEAK, expiryTimeOfDay=@TimeOfDay(hour=3))
@Cacheable(true)
@Entity
@Indexes({ @Index(name = "FILENAME_INDEX", columnNames = { "FILENAME" }) })
@Table(name = "IMAGEDATA")
public class ImageData extends BaseEntity {
	private static final long serialVersionUID = 7590792385834419260L;

	@Column(name = "WIDTH")
	private int width;
	
	@Column(name = "HEIGHT")
	private int height;

	@Column(name = "FILENAME")
	private String filename;
	
	@Column(name = "MIMETYPE")
	private String mimetype;
	
	@Lob
	@Column(name = "DATA", length=100000)    //This will generate MEDIUMBLOB
	@Basic(optional = false, fetch = FetchType.LAZY)
	private byte[] data;

	public ImageData() {
		data = new byte[]{};
	}

	public String getFilename() {
		return filename;
	}

	public String getMimetype() {
		return mimetype;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] byteArray) {
		data = byteArray;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		return sb.append(this.getFilename()).append(" ").append(this.getMimetype()).append(" ").append(this.getWidth() + "x" +getHeight()).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(this.getFilename()).append(this.getMimetype()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ImageData == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final ImageData otherObject = (ImageData) obj;
		return new EqualsBuilder().append(this.getFilename(), otherObject.getFilename()).isEquals();
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	
	
}