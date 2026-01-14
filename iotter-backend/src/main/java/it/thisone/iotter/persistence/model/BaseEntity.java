package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * This class is an abstract superclass for all Entity classes in the
 * application. This class defines variables which are common for all entity
 * classes.
 * 
 * 
 */

/**
 * 
 * 
 @Column(name = "bla")
 * @Column(name = "...")
 * @Type(type = "ch.vd.registre.base.hibernate.type.RegDateUserType")
 * @ManyToOne
 * @JoinColumn(name = "..._FK") public Party getParty() { // begin-user-code
 *                  return party; // end-user-code }
 * @OneToMany(mappedBy = "...") public List<Contact> getContacts() { //
 *                     begin-user-code return contacts; // end-user-code }
 * @Entity
 * @Table(name = "PP_...")
 * @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 * @DiscriminatorColumn(name = "DTYPE", discriminatorType =
 *                           DiscriminatorType.STRING)
 * 
 * 
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = -7289994339186082141L;


	@Id
	@GeneratedValue(generator = "system-uuid")
	private String id;
	
	@Column(name = "VERSION", nullable = false)
	@Version
	private Long consistencyVersion;

	@Column(name = "OWNER")
	private String owner;

	/**
	 * Get the primary key for this entity.
	 * 
	 * @return Primary key
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the primary key for this entity. Usually, this method should never be
	 * called.
	 * 
	 * @param id
	 *            New primary key
	 */
	public void setId(String id) {
//		if(id ==null) {
//			System.out.println("BaseEntity null id");
//		}
		this.id = id;
	}

	/**
	 * Get the concurrency version number for this entity. The concurrency
	 * version is a number which is used for optimistic locking in the database.
	 * 
	 * @return Current consistency version
	 */
	public long getConsistencyVersion() {
		return consistencyVersion;
	}

	/**
	 * Set the concurrency version number for this entity. Usually, this method
	 * should never be called.
	 * 
	 * @param consistencyVersion
	 *            New consistency version
	 */
	protected void setConsistencyVersion(long consistencyVersion) {
		this.consistencyVersion = consistencyVersion;
	}

	/**
	 * Get owner 
	 * @return
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Set owner 
	 * @param owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Transient
	public boolean isNew() {
	    if(this.id == null) return true;
	    if(this.id.startsWith("-")) return true;
	    return false;
	}
}
