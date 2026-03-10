package it.thisone.iotter.ui.deviceconfigurations;

import java.util.Arrays;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.validator.StringLengthValidator;

import it.thisone.iotter.enums.Protocol;
import it.thisone.iotter.persistence.model.DeviceModel;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.validators.UniqueDeviceModelNameValidator;

public class DeviceModelForm extends AbstractBaseEntityForm<DeviceModel> {

    private static final long serialVersionUID = 1L;

    @PropertyId("name")
    private TextField name;

    @PropertyId("description")
    private TextArea description;

    @PropertyId("protocol")
    private ComboBox<Protocol> protocol;

    @PropertyId("userManual")
    private TextField userManual;

    @PropertyId("rtc")
    private Checkbox rtc;

    private boolean fieldsInitialized;

    public DeviceModelForm(DeviceModel entity) {
        super(entity, DeviceModel.class, "device_model.editor", null, null, false);
        ensureFieldsInitialized(entity);
        getBinder().readBean(entity);
    }

    private void ensureFieldsInitialized(DeviceModel entity) {
        if (fieldsInitialized) {
            return;
        }
        name = new TextField(getI18nLabel("name"));
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);

        description = new TextArea(getI18nLabel("description"));
        description.setWidthFull();
        description.setHeight("8em");

        protocol = new ComboBox<>(getI18nLabel("protocol"));
        protocol.setItems(Arrays.stream(Protocol.values())
                .filter(p -> !Protocol.FTP.equals(p))
                .toArray(Protocol[]::new));
        protocol.setAllowCustomValue(false);
        protocol.setClearButtonVisible(false);
        protocol.setWidthFull();
        protocol.setItemLabelGenerator(Protocol::name);

        userManual = new TextField(getI18nLabel("userManual"));
        userManual.setWidthFull();

        rtc = new Checkbox(getI18nLabel("rtc"));
        rtc.setWidthFull();

        if (entity.isNew()) {
            rtc.setValue(true);
        }
        if (!entity.isNew() && Protocol.NATIVE.equals(entity.getProtocol())) {
            protocol.setReadOnly(true);
        }

        bindFields(entity);
        fieldsInitialized = true;
    }

    @Override
    protected void initializeFields() {
        ensureFieldsInitialized(getEntity());
    }

    @Override
    protected void bindFields() {
        // Fields are bound in bindFields(DeviceModel) to preserve validator context.
    }

    private void bindFields(DeviceModel entity) {
        Binder<DeviceModel> binder = getBinder();

        binder.forField(name)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withValidator(new StringLengthValidator(getTranslation("validators.fieldgroup_errors"), 1, 255))
                .withValidator(new UniqueDeviceModelNameValidator(entity.getName()))
                .bind(DeviceModel::getName, DeviceModel::setName);

        binder.forField(description)
                .bind(DeviceModel::getDescription, DeviceModel::setDescription);

        binder.forField(protocol)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .bind(DeviceModel::getProtocol, DeviceModel::setProtocol);

        binder.forField(userManual)
                .bind(DeviceModel::getUserManual, DeviceModel::setUserManual);

//        binder.forField(rtc)
//                .bind(DeviceModel::getRtc, DeviceModel::setRtc);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        ensureFieldsInitialized(getEntity());
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.add(name, description, protocol, userManual, rtc);
        mainLayout.add(buildPanel(formLayout));
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }

    public String getWindowStyle() {
        return "device-model-editor";
    }

    public float[] getWindowDimension() {
        return UIUtils.M_DIMENSION;
    }
}
