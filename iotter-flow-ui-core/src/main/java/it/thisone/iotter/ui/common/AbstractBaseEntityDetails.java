package it.thisone.iotter.ui.common;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;


import com.vaadin.flow.component.button.Button;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.notification.Notification;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import it.thisone.iotter.persistence.model.BaseEntity;
import it.thisone.iotter.ui.eventbus.PendingChangesEvent;
import it.thisone.iotter.util.PopupNotification;

@SuppressWarnings("serial")
public abstract class AbstractBaseEntityDetails<T extends BaseEntity> extends Composite<Div>  {

	private final String name;
	private final Class<T> beanType;
	private final T bean;
	private HorizontalLayout details;
	private final Button selectButton;
	private final Button removeButton;
	private final Button cancelButton;
	private FormLayout fieldsLayout;

	public AbstractBaseEntityDetails(String name, T bean, Class<T> beanType) {
		super();
		this.name = name;
		this.bean = bean;
		this.beanType = beanType;
		selectButton = new Button(UIUtils.localize("basic.editor.select"), this);
		removeButton = new Button(UIUtils.localize("basic.editor.remove"), this);
		cancelButton = new Button(UIUtils.localize("basic.editor.close"), this);


	}

	public AbstractBaseEntityDetails(T item, Class<T> itemType, String name, String[] fieldIds, boolean remove) {
		this(name, item, itemType);
		details = initDetails(fieldIds);
		removeButton.setVisible(remove);
		selectButton.setVisible(false);
		if (remove) {
			removeButton.setText(UIUtils.localize("basic.editor.yes"));
			cancelButton.setText(UIUtils.localize("basic.editor.no"));
		}
		buildLayout(details);
	}

	protected void buildLayout(Component content) {
		VerticalLayout verticalLayout = new VerticalLayout();
		
		verticalLayout.setSizeFull();
		verticalLayout.setSpacing(true);
		verticalLayout.add(content);


		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setPadding(true);
		buttonLayout.add(selectButton);
		buttonLayout.add(removeButton);
		buttonLayout.add(cancelButton);

		HorizontalLayout footer = new HorizontalLayout();
		footer.setWidth(100.0f, Unit.PERCENTAGE);
		//footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		footer.add(buttonLayout);

		verticalLayout.add(footer);

//		setCompositionRoot(verticalLayout);
//		setSizeFull();
	}

	private HorizontalLayout initDetails(String[] fieldIds) {
		fieldsLayout = new FormLayout();
//		fieldsLayout.setSpacing(true);
//		fieldsLayout.setPadding(true);

		Collection<String> properties = new ArrayList<String>();
		if (fieldIds != null) {
			for (String fieldId : fieldIds) {
				properties.add(fieldId);
			}
		}

		if (properties.isEmpty()) {
			for (PropertyDescriptor descriptor : getPropertyDescriptors()) {
				String name = descriptor.getName();
				if (!"class".equals(name)) {
					properties.add(name);
				}
			}
		}

		for (String propertyId : properties) {
			Object value = readProperty(propertyId);
			TextField field = new TextField(getI18nLabel(propertyId));
			field.setWidth(100, Unit.PERCENTAGE);
			field.setReadOnly(false);
			field.setValue(value == null ? "" : String.valueOf(value));
			field.setReadOnly(true);
			fieldsLayout.add(field);
		}

        PanelFlow panel = new PanelFlow();
		panel.setContent(fieldsLayout);
		panel.setSizeFull();

		HorizontalLayout layout = new HorizontalLayout();
		//layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		layout.setSizeFull();
		layout.add(panel);
		layout.setPadding(true);

		return layout;
	}



//	@Override
//	public void buttonClick(ClickEvent event) {
//		if (event.getButton() == removeButton) {
//			try {
//				onRemove();
//				UIUtils.getUIEventBus().post(new PendingChangesEvent());
//				fireEvent(new EntityRemovedEvent<T>(this, bean));
//			} catch (EditorConstraintException e) {
//				PopupNotification.show(e.getMessage(), PopupNotification.Type.ERROR);
//			}
//		} else if (event.getButton() == selectButton) {
//			fireEvent(new EntitySelectedEvent<T>(this, bean));
//		} else if (event.getButton() == cancelButton) {
//			if (removeButton.isVisible()) {
//				fireEvent(new EntityRemovedEvent<T>(this, null));
//			} else {
//				fireEvent(new EntitySelectedEvent<T>(this, null));
//			}
//		}
//	}
//
//	public void addListener(EntityRemovedListener listener) {
//		try {
//			Method method = EntityRemovedListener.class.getDeclaredMethod(EntityRemovedListener.ENTITY_REMOVED,
//					new Class[] { EntityRemovedEvent.class });
//			addListener(EntityRemovedEvent.class, listener, method);
//		} catch (final java.lang.NoSuchMethodException e) {
//			throw new java.lang.RuntimeException("Internal error, entity removed method not found");
//		}
//	}
//
//	public void addListener(EntitySelectedListener listener) {
//		try {
//			Method method = EntitySelectedListener.class.getDeclaredMethod(EntitySelectedListener.ENTITY_SELECTED,
//					new Class[] { EntitySelectedEvent.class });
//			addListener(EntitySelectedEvent.class, listener, method);
//		} catch (final java.lang.NoSuchMethodException e) {
//			throw new java.lang.RuntimeException("Internal error, entity selected method not found");
//		}
//	}

//	public void removeListener(EntityRemovedListener listener) {
//		removeListener(EntityRemovedEvent.class, listener);
//	}

	public String getI18nLabel(String key) {
		return UIUtils.localize(name + "." + key);
	}

	public T getBean() {
		return bean;
	}

	protected abstract void onRemove() throws EditorConstraintException;

	public Button getRemoveButton() {
		return removeButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public HorizontalLayout getDetails() {
		return details;
	}

	public FormLayout getFieldsLayout() {
		return fieldsLayout;
	}

	public void setDetails(Component content) {
        PanelFlow panel = new PanelFlow();
		panel.setContent(content);
		panel.setSizeFull();
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.add(panel);
		layout.setPadding(true);
		details = layout;
		buildLayout(details);
	}

	public Button getSelectButton() {
		return selectButton;
	}

	private PropertyDescriptor[] getPropertyDescriptors() {
		try {
			return Introspector.getBeanInfo(beanType).getPropertyDescriptors();
		} catch (Exception e) {
			return new PropertyDescriptor[0];
		}
	}

	private Object readProperty(String propertyId) {
		for (PropertyDescriptor descriptor : getPropertyDescriptors()) {
			if (descriptor.getName().equals(propertyId)) {
				try {
					Method readMethod = descriptor.getReadMethod();
					if (readMethod != null) {
						return readMethod.invoke(bean);
					}
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}
}
