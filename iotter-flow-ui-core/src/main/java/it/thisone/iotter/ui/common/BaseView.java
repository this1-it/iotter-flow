package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class BaseView extends VerticalLayout {

	private static final long serialVersionUID = 5876788432112136543L;

	public abstract String getI18nKey();

	public String getI18nLabel(String key) {
		return getTranslation(getI18nKey() + "."+ key, null);
	}


}