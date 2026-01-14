package it.thisone.iotter.ui.ifc;

import java.util.Collection;

import com.vaadin.flow.data.binder.ValidationException;

import it.thisone.iotter.persistence.model.Channel;

public interface IChannelListingField {

	Class<? extends Collection<Channel>> getType();

	void setValue(Collection<Channel> collection);

	Collection<Channel> getValue();

	void validate() throws ValidationException;

	void commit() throws ValidationException;

}