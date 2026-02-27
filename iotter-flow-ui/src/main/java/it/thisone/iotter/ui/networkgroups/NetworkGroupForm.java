package it.thisone.iotter.ui.networkgroups;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.validator.StringLengthValidator;

import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.validators.UniqueNetworkGroupNameValidator;

public class NetworkGroupForm extends AbstractBaseEntityForm<NetworkGroup> {

    private static final long serialVersionUID = 1L;

    @PropertyId("name")
    private TextField name;

    @PropertyId("description")
    private TextArea description;

    private final Network network;
    private boolean fieldsInitialized;
    private boolean bindingsInitialized;

    public NetworkGroupForm(NetworkGroup entity, Network network) {
        this(entity, network, false);
    }

    public NetworkGroupForm(NetworkGroup entity, Network network, boolean readOnly) {
        super(entity, NetworkGroup.class, "networkgroup.editor", network, null, readOnly);
        this.network = network;
        ensureFieldsInitialized();
        ensureBindingsInitialized();
        getBinder().readBean(entity);
    }

    private void ensureFieldsInitialized() {
        if (!fieldsInitialized) {
            initializeFields();
            fieldsInitialized = true;
        }
    }

    private void ensureBindingsInitialized() {
        if (!bindingsInitialized) {
            bindFields();
            bindingsInitialized = true;
        }
    }

    @Override
    protected void initializeFields() {
        name = new TextField(getI18nLabel("name"));
        name.setSizeFull();
        name.setRequiredIndicatorVisible(true);
        name.setReadOnly(isReadOnly());

        description = new TextArea(getI18nLabel("description"));
        description.setSizeFull();
        description.setReadOnly(isReadOnly());
        //description.setRows(4);
    }

    @Override
    protected void bindFields() {
        ensureFieldsInitialized();
        NetworkGroup entity = getEntity();
        Binder<NetworkGroup> binder = getBinder();
        binder.forField(name)
                .asRequired(UIUtils.localize("validators.fieldgroup_errors"))
                .withValidator(new StringLengthValidator(UIUtils.localize("validators.fieldgroup_errors"), 1, 255))
                .withValidator(new UniqueNetworkGroupNameValidator(entity.getName(), network))
                .bind(NetworkGroup::getName, NetworkGroup::setName);

        binder.forField(description)
                .bind(NetworkGroup::getDescription, NetworkGroup::setDescription);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        ensureFieldsInitialized();
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.add(name, description);
        mainLayout.add(buildPanel(formLayout));
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }


}
