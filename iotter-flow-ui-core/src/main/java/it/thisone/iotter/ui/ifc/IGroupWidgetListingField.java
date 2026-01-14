package it.thisone.iotter.ui.ifc;

import java.util.Collection;



import it.thisone.iotter.persistence.model.GroupWidget;

public interface IGroupWidgetListingField {

	Class<? extends Collection<GroupWidget>> getType();

	void setValue(Collection<GroupWidget> collection);

	Collection<GroupWidget> getValue();



}