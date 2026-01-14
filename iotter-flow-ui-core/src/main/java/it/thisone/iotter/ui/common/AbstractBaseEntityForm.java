package it.thisone.iotter.ui.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.flow.component.Alignment;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.notification.Notification;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;

import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.model.BaseEntity;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.ui.eventbus.PendingChangesEvent;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.Utils;


public abstract class AbstractBaseEntityForm<T extends BaseEntity> extends AbstractForm<T> {

    private static final long serialVersionUID = -6063387822417475910L;
    private static final float ACTION_BUTTON_WIDTH = 120f;
	private String name;
    //private BeanBinderFacade<T> binderFacade;
    private Network network;
    private FormLayout formLayout;
	private Map<String, Component> fields = new HashMap<>(); 
	private List<String> properties = new ArrayList<>();
	private com.vaadin.flow.component.button.Button cancelButton;

    /**
     * A generic entity form using Vaadin 8 Binder and AbstractForm
     * 
     * @param facade contains entity to be edited and binder
     * @param network used for editing relations, it may be null
     */
    public AbstractBaseEntityForm(T entity, Class<T> entityType, String name, Network network) {
        super(entityType);
        this.name = name;
        this.network = network;
        this.setBinder(new Binder<>(entityType));
        setEntity(entity);
        
        if (getEntity().getOwner() == null) {
        	getEntity().setOwner(UIUtils.getUserDetails().getTenant());
        }
        
        
        // Configure buttons styling
        getSaveButton().addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
        getResetButton().addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
        getDeleteButton().addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
        getCancelButton().addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
        getSaveButton().setWidth(ACTION_BUTTON_WIDTH, Unit.PIXELS);
        getCancelButton().setWidth(ACTION_BUTTON_WIDTH, Unit.PIXELS);
        
        // Initially disable save button if form is invalid
        if (!getBinder().isValid()) {
            getSaveButton().setEnabled(false);
        }
        
        // Add validation listener to enable/disable save button
        getBinder().addStatusChangeListener(event -> {
            getSaveButton().setEnabled(event.getBinder().isValid());
        });
    }

    @Override
    protected Component createContent() {
    	HasComponents fieldsLayout = getFieldsLayout();

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);

        buttonLayout.addComponent(getSaveButton());
        buttonLayout.addComponent(getCancelButton());
        buttonLayout.addComponent(getResetButton());
        buttonLayout.addComponent(getDeleteButton());

        HorizontalLayout footer = new HorizontalLayout();
        //footer.addClassName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
        footer.setWidth(100, Unit.PERCENTAGE);
        footer.setHeight(UIUtils.TOOLBAR_HEIGHT, Unit.PIXELS);
        footer.setPadding(false);
        footer.setSpacing(false);
        footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        footer.addComponent(buttonLayout);
        footer.setExpandRatio(buttonLayout, 1f);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        layout.addComponent(fieldsLayout);
        layout.setExpandRatio(fieldsLayout, 1f);
        layout.addComponent(footer);
        return layout;
    }

    /**
     * Add fields to form. Default implementation adds fields to a form layout
     */
    public HasComponents getFieldsLayout() {
        VerticalLayout mainLayout = buildMainLayout();
        mainLayout.addComponent(buildPanel(getOrCreateFormLayout()));
        return mainLayout;
    }

    protected VerticalLayout buildMainLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(new MarginInfo(true, false, false, false));
        mainLayout.setSizeFull();
        return mainLayout;
    }
    
    private FormLayout getOrCreateFormLayout() {
        if (formLayout == null) {
            formLayout = new FormLayout();
            formLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
            formLayout.setSpacing(true);
            formLayout.setPadding(true);
            for (String property : properties) {
                Component field = getField(property);
                if (field != null) {
                    formLayout.addComponent(field);
                }
            }
        }
        return formLayout;
    }

    public Component buildForm(List<String> properties) {
        Component[] fields = new Component[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            fields[i] = getField(properties.get(i));
        }
        return buildForm(fields);
    }

    protected Component getField(String string) {
		return fields.get(string);
	}
    
    protected void addField(String name, Component field) {
		fields.put(name,field);
		properties.add(name);
        if (formLayout != null) {
            formLayout.addComponent(field);
        }
	}

	/**
     * Create a layout/panel to display editable fields
     * 
     * @param fields
     * @return
     */
    public Component buildForm(Component[] fields) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null) {
                getOrCreateFormLayout().addComponent(fields[i]);
            }
        }
        return buildPanel(getOrCreateFormLayout());
    }

    public Component buildPanel(Component component) {
        PanelFlow panel = new PanelFlow();
        panel.setContent(component);
        panel.setSizeFull();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        layout.setSizeFull();
        layout.addComponent(panel);
        layout.setPadding(true);
        return layout;
    }

    private void closeParentWindow() {
        Component parent = this;
        while (parent != null) {
            if (parent instanceof Dialog) {
                ((Dialog) parent).close();
                return;
            }
            parent = parent.getParent();
        }
    }

    private com.vaadin.flow.component.button.Button getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new com.vaadin.flow.component.button.Button(UIUtils.localize("basic.editor.cancel"));
            cancelButton.addClickListener(event -> closeParentWindow());
        }
        return cancelButton;
    }

    protected void save(T entity) {
        try {
            beforeCommit();
            getBinder().writeBean(entity);
            afterCommit();
            UIUtils.getUIEventBus().post(new PendingChangesEvent());
            
            // Call the saved handler if set
            if (getSavedHandler() != null) {
                getSavedHandler().onSave(entity);
            }
        } catch (ValidationException e) {
            StringWriter errorStackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(errorStackTrace));
            String message = null;
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            if (message == null) {
                message = " check application log for details ";
                UIUtils.trace(TracingAction.ERROR_UI, UIUtils.getUserDetails().getName(), 
                        UIUtils.getUserDetails().getTenant(), null, null, Utils.stackTrace(e));
            }

            PopupNotification.show(
                    UIUtils.localize("validators.fieldgroup_errors") + " \n " + message,
                    Notification.Type.ERROR_MESSAGE);
        } catch (EditorConstraintException e) {
            PopupNotification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    protected void reset(T entity) {
        getBinder().readBean(entity);
        
        // Call the reset handler if set
        if (getResetHandler() != null) {
            getResetHandler().onReset(entity);
        }
    }

    protected void delete(T entity) {
        try {
            beforeCommit();
            
            // Call the delete handler if set
            if (getDeleteHandler() != null) {
                getDeleteHandler().onDelete(entity);
            }
        } catch (EditorConstraintException e) {
            PopupNotification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public boolean isCreateBean() {
        return getEntity().isNew();
    }



    public void refreshItem(T item) {
        setEntity(item);

    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append(name, this.getEntity()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.getEntity().getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractBaseEntityForm == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        @SuppressWarnings("unchecked")
        final AbstractBaseEntityForm<T> otherObject = (AbstractBaseEntityForm<T>) obj;
        return new EqualsBuilder().append(this.getEntity().getId(),
                otherObject.getEntity().getId()).isEquals();
    }

    @Deprecated
    public abstract String getWindowStyle();
    
    public abstract float[] getWindowDimension();
    
    /**
     * Change bean after commit copy extra field to bean if needed
     */
    protected abstract void afterCommit();

    protected abstract void beforeCommit() throws EditorConstraintException;

    public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey()  + "." + key);
    }

	private String getI18nKey() {
		return name;
	}

}
