package it.thisone.iotter.persistence.canonical;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;

/**
The canonical metamodel consists of dedicated classes, typically generated, one per persistent class, that contain
static declarations of the metamodel objects associated with that persistent class. This allows you to access the same
information exposed through the metamodel API, but in a form that applies directly to your persistent classes.
 *
 */
@StaticMetamodel(Network.class)
public class Network_ {
	public static volatile SingularAttribute<Network, Long> id;
	public static volatile CollectionAttribute<Network, NetworkGroup> groups;
}