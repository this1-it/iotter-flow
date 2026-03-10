package it.thisone.iotter.ui.deviceconfigurations;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;

import it.thisone.iotter.persistence.model.MeasureUnitType;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.validators.UniqueMeasureUnitTypeValidator;

public class MeasureUnitTypeForm extends AbstractBaseEntityForm<MeasureUnitType> {

	private static final long serialVersionUID = 1L;

	private TextField name;
	private TextField code;
	private boolean fieldsInitialized;

	public MeasureUnitTypeForm(MeasureUnitType entity) {
		super(entity, MeasureUnitType.class, "measureunittype", null, null, false);
		ensureFieldsInitialized(entity);
		getBinder().readBean(entity);
	}

	@Override
	protected void initializeFields() {
		ensureFieldsInitialized(getEntity());
	}

	@Override
	protected void bindFields() {
		// Fields are bound in bindFields(MeasureUnitType) to keep entity-dependent validators.
	}

	@Override
	public VerticalLayout getFieldsLayout() {
		ensureFieldsInitialized(getEntity());
		return super.getFieldsLayout();
	}

	private void ensureFieldsInitialized(MeasureUnitType entity) {
		if (fieldsInitialized) {
			return;
		}
		name = new TextField();
		name.setWidthFull();
		name.setRequiredIndicatorVisible(true);
		name.setLabel(getI18nLabel("name"));

		code = new TextField();
		code.setWidthFull();
		code.setRequiredIndicatorVisible(true);
		code.setLabel(getI18nLabel("code"));

		if (!isCreateBean()) {
			code.setReadOnly(true);
		}

		addField("name", name);
		addField("code", code);

		bindFields(entity);
		fieldsInitialized = true;
	}

	private void bindFields(MeasureUnitType entity) {
		Binder<MeasureUnitType> binder = getBinder();
		String requiredMessage = getTranslation("validators.fieldgroup_errors");

		binder.forField(name)
				.asRequired(requiredMessage)
				.bind(MeasureUnitType::getName, MeasureUnitType::setName);

		binder.forField(code)
				.asRequired(requiredMessage)
				.withConverter(new StringToIntegerConverter(requiredMessage))
				.withValidator(new UniqueMeasureUnitTypeValidator(entity.getCode()))
				.bind(MeasureUnitType::getCode, MeasureUnitType::setCode);
	}

	@Override
	protected void afterCommit() {
	}

	@Override
	protected void beforeCommit() throws EditorConstraintException {
		String codeValue = code.getValue();
		if (codeValue != null && !codeValue.isEmpty()) {
			try {
				Integer.parseInt(codeValue);
			} catch (NumberFormatException e) {
				throw new EditorConstraintException("Code must be a valid integer");
			}
		}
	}

	public String getWindowStyle() {
		return UIUtils.S_WINDOW_STYLE;
	}

	public float[] getWindowDimension() {
		return UIUtils.S_DIMENSION;
	}
}
