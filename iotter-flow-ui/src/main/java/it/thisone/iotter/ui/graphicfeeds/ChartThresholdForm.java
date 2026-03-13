package it.thisone.iotter.ui.graphicfeeds;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.converter.StringToFloatConverter;

import it.thisone.iotter.persistence.model.ChartThreshold;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;

public class ChartThresholdForm extends AbstractBaseEntityForm<ChartThreshold> {

    private static final long serialVersionUID = 1L;

    @PropertyId("value")
    private TextField value;

    @PropertyId("label")
    private TextField label;

    public ChartThresholdForm(ChartThreshold entity) {
        this(entity, false);
    }

    public ChartThresholdForm(ChartThreshold entity, boolean readOnly) {
        super(entity, ChartThreshold.class, "graphthreshold.editor", null, null, readOnly);
        bindFields();
        getBinder().readBean(entity);
    }

    @Override
    protected void initializeFields() {
        value = new TextField(getI18nLabel("value"));
        value.setWidthFull();
        value.setRequiredIndicatorVisible(true);

        label = new TextField(getI18nLabel("label"));
        label.setWidthFull();
    }

    public TextField getValueField() {
        return value;
    }

    public TextField getLabelField() {
        return label;
    }

    @Override
    protected void bindFields() {
        Binder<ChartThreshold> binder = getBinder();
        binder.forField(value)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToFloatConverter(getTranslation("invalid_value")))
                .bind(threshold -> threshold.getValue(), (threshold, newValue) -> threshold.setValue(newValue));

        binder.forField(label)
                .bind(ChartThreshold::getLabel, ChartThreshold::setLabel);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        initializeFields();

        HorizontalLayout fields = new HorizontalLayout(value, label);
        fields.setPadding(true);
        fields.setSpacing(true);
        fields.setWidth(100, Unit.PERCENTAGE);
        fields.setFlexGrow(1f, value, label);

        VerticalLayout mainLayout = buildMainLayout();
        mainLayout.add(buildPanel(fields));
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }

    public String getWindowStyle() {
        return "graphthreshold-editor";
    }

    public float[] getWindowDimension() {
        return UIUtils.M_DIMENSION;
    }
}
