package it.thisone.iotter.persistence.canonical;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;

@StaticMetamodel(Device.class)
public class Device_ {
	public static volatile SingularAttribute<Device, Long> id;
	public static volatile CollectionAttribute<Device, Channel> channels;
}