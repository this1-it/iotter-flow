package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;

import it.thisone.iotter.persistence.model.BaseEntity;
import it.thisone.iotter.security.Permissions;

public abstract class AbstractBaseEntityListing<T extends BaseEntity> extends BaseComponent {

	private static final long serialVersionUID = 2521293335405518354L;

	protected static final int DEFAULT_LIMIT = 100;

	public static final String COUNTER_STYLE = "counter-label";
	public static final String ALWAYS_ENABLED_BUTTON = "always_enabled_button";

	private final VerticalLayout mainLayout;
	private VerticalLayout editorLayout;
	private final HorizontalLayout buttonsLayout;
	private Component selectable;
	private AbstractDataProvider<T, ?> dataProvider;
	private long totalSize = -1;
	private Permissions permissions;

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
		if (this.permissions != null && this.permissions.isViewAllMode()) {
			this.permissions.setViewMode(true);
		}
	}

	public AbstractBaseEntityListing(Class<T> itemType, String name, String id, boolean readOnly) {
		super(name, id);
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(false);
		
		editorLayout = new VerticalLayout();
		editorLayout.setSizeFull();
		//setSizeFull();


		buttonsLayout = new HorizontalLayout();
		//buttonsLayout.setStyleName(UIUtils.BUTTONS_STYLE);
		buttonsLayout.setSpacing(true);
		//buttonsLayout.setDefaultComponentAlignment(Alignment.MIDDLE_RIGHT);
		Span counter = new Span();
		//counter.setStyleName(COUNTER_STYLE);
		counter.setId(COUNTER_STYLE);
		buttonsLayout.add(counter);
		setRootComposition(mainLayout);
	}

	protected abstract AbstractBaseEntityForm<T> getEditor(T item, boolean readOnly);

	protected abstract void openDetails(T item);
	
	protected abstract void openRemove(T item);

	
	public VerticalLayout getMainLayout() {
		return mainLayout;
	}

	public VerticalLayout getEditorLayout() {
		return editorLayout;
	}

	public void setEditorLayout(VerticalLayout editorLayout) {
		this.editorLayout = editorLayout;
	}

	public HorizontalLayout getButtonsLayout() {
		return buttonsLayout;
	}

	public Component getSelectable() {
		return selectable;
	}

	@SuppressWarnings("unchecked")
	public void setSelectable(final Component component) {
		if (component instanceof Grid) {
			Grid<T> grid = (Grid<T>) component;
			grid.addSelectionListener(event -> {
				T item = getCurrentValue();
				enableButtons(item);
			});
		} else {
			throw new UnsupportedOperationException("unsupported component");
		}
		selectable = component;
	}

	@SuppressWarnings("unchecked")
	public T getSelectedItem() {
		if (selectable instanceof Grid) {
			Grid<T> grid = (Grid<T>) selectable;
			return grid.asSingleSelect().getValue();
		}
		throw new UnsupportedOperationException("unsupported selectable component");
	}

	public T getCurrentValue() {
		return getSelectedItem();
	}

	public AbstractDataProvider<T, ?> getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(AbstractDataProvider<T, ?> dataProvider) {
		this.dataProvider = dataProvider;
	}

	public <F> void setBackendDataProvider(AbstractDataProvider<T, F> dataProvider) {
		setDataProvider(dataProvider);
	}

	@SuppressWarnings("unchecked")
	public <F> void setFilter(F filter) {
		if (dataProvider instanceof ConfigurableFilterDataProvider) {
			ConfigurableFilterDataProvider<T, F, F> backendProvider =
					(ConfigurableFilterDataProvider<T, F, F>) dataProvider;
			backendProvider.setFilter(filter);
		}
	}

//	public void refreshTotalSize() {
//		if (dataProvider != null) {
//			setTotalSize(dataProvider.size(new Query()));
//		}
//	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}



	public void enableButtons(T item) {
		boolean enabled = item != null;
		int size = getSize();
//		Iterator<Component> iterator = buttonsLayout.iterator();
//		while (iterator.hasNext()) {
//			Component c = iterator.next();
//			if (c instanceof Button) {
//				Button button = (Button) c;
//				if (button.getId() != null && button.getId().contains(ALWAYS_ENABLED_BUTTON)) {
//					if (maxSize > 0) {
//						button.setEnabled(size < maxSize);
//					}
//				} else {
//					button.setEnabled(enabled);
//				}
//			} else if (c instanceof Span) {
//				Span label = (Span) c;
//				if (COUNTER_STYLE.equals(label.getId())) {
//					//label.setValue(Integer.toString(size));
//					label.setText(Long.toString(size));
//				}
//			}
//		}
	}

	private int getSize() {
		if (totalSize >= 0) {
			return (int) totalSize;
		}
		if (dataProvider == null) {
			return 0;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractDataProvider rawProvider = (AbstractDataProvider) dataProvider;
		return rawProvider.size(new Query());
	}
}
