package it.thisone.iotter.persistence.canonical;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import it.thisone.iotter.persistence.model.Channel;

@StaticMetamodel(Channel.class)
public class Channel_ {
	public static volatile SingularAttribute<Channel, Long> id;
}