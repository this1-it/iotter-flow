package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.ui.common.UIUtils;

public class AccountStatusSelect extends ComboBox<AccountStatus> {

	private static final long serialVersionUID = -2993122599439071404L;

	public AccountStatusSelect() {
		super();
		List<AccountStatus> items = new ArrayList<>(
				Arrays.asList(AccountStatus.NEED_ACTIVATION, AccountStatus.ACTIVE, AccountStatus.SUSPENDED));
		setItems(items);
		setItemLabelGenerator(type -> UIUtils.localize(type.getI18nKey()));
	}

		@SuppressWarnings("unchecked")
	public void removeItem(AccountStatus item) {
		// We assume the data provider is a ListDataProvider, as set by setItems()
		ListDataProvider<AccountStatus> dataProvider = (ListDataProvider<AccountStatus>) getDataProvider();
		dataProvider.getItems().remove(item);
		dataProvider.refreshAll();
	}

}
