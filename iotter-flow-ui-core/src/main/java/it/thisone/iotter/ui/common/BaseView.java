package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;

public abstract class BaseView extends VerticalLayout implements HasDynamicTitle {

	private static final long serialVersionUID = 5876788432112136543L;

	public abstract String getI18nKey();

	@Override
	public String getPageTitle() {
		return getTranslation("view." + getI18nKey());
	}

	public String getI18nLabel(String key) {
		return getTranslation(getI18nKey() + "."+ key);
	}


}