package it.thisone.iotter.ui.common;

import java.io.Serializable;

public interface ItemSelectedListener extends Serializable {
	public static final String ITEM_SELECTED = "itemSelected";
	public void itemSelected(ItemSelectedEvent event);
}
