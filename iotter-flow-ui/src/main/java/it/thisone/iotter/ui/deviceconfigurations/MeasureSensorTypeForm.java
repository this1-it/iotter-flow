package it.thisone.iotter.ui.deviceconfigurations;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.converter.StringToIntegerConverter;

import it.thisone.iotter.persistence.model.MeasureSensorType;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.validators.UniqueMeasureSensorTypeValidator;

public class MeasureSensorTypeForm extends AbstractBaseEntityForm<MeasureSensorType> {

    private static final long serialVersionUID = 1L;

    @PropertyId("name")
    private TextField name;

    @PropertyId("code")
    private TextField code;

    private boolean fieldsInitialized;

    public MeasureSensorTypeForm(MeasureSensorType entity) {
        super(entity, MeasureSensorType.class, "measuresensortype", null, null, false);
        ensureFieldsInitialized(entity);
        getBinder().readBean(entity);
    }

    @Override
    protected void initializeFields() {
        ensureFieldsInitialized(getEntity());
    }

    @Override
    protected void bindFields() {
        // Fields are bound in bindFields(MeasureSensorType) to keep entity-dependent validators.
    }

    private void ensureFieldsInitialized(MeasureSensorType entity) {
        if (fieldsInitialized) {
            return;
        }
        name = new TextField(getI18nLabel("name"));
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);

        code = new TextField(getI18nLabel("code"));
        code.setWidthFull();
        code.setRequiredIndicatorVisible(true);
        if (!isCreateBean()) {
            code.setReadOnly(true);
        }

        bindFields(entity);
        fieldsInitialized = true;
    }

    private void bindFields(MeasureSensorType entity) {
        getBinder().forField(name)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .bind(MeasureSensorType::getName, MeasureSensorType::setName);

        getBinder().forField(code)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToIntegerConverter(getTranslation("validators.fieldgroup_errors")))
                .withValidator(new UniqueMeasureSensorTypeValidator(entity.getCode()))
                .bind(MeasureSensorType::getCode, MeasureSensorType::setCode);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        ensureFieldsInitialized(getEntity());
        FormLayout layout = new FormLayout();
        layout.add(name, code);

        VerticalLayout mainLayout = buildMainLayout();
        mainLayout.add(buildPanel(layout));
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }

    public String getWindowStyle() {
        return UIUtils.S_WINDOW_STYLE;
    }

    public float[] getWindowDimension() {
        return UIUtils.S_DIMENSION;
    }
}
