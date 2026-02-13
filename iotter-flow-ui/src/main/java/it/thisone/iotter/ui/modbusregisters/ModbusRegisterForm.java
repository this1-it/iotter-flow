package it.thisone.iotter.ui.modbusregisters;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;

import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.fields.BacNetMeasureUnitSelect;
import it.thisone.iotter.ui.common.fields.BooleanOptionGroup;
import it.thisone.iotter.ui.common.fields.FormatSelect;
import it.thisone.iotter.ui.common.fields.FunctionCodeSelect;
import it.thisone.iotter.ui.common.fields.PermissionSelect;
import it.thisone.iotter.ui.common.fields.PrioritySelect;
import it.thisone.iotter.ui.common.fields.QualifierSelect;
import it.thisone.iotter.ui.common.fields.SignedSelect;
import it.thisone.iotter.ui.common.fields.TypeReadSelect;
import it.thisone.iotter.ui.common.fields.TypeVarSelect;
import it.thisone.iotter.ui.main.UiConstants;

public class ModbusRegisterForm extends AbstractBaseEntityForm<ModbusRegister> {

    private static final long serialVersionUID = 1L;

    private final List<ModbusRegister> others;

    @PropertyId("address")
    private TextField address;

    @PropertyId("displayName")
    private TextField displayName;

    @PropertyId("active")
    private BooleanOptionGroup active;

    @PropertyId("measureUnit")
    private BacNetMeasureUnitSelect measureUnit;

    @PropertyId("scaleMultiplier")
    private TextField scaleMultiplier;

    @PropertyId("offset")
    private TextField offset;

    @PropertyId("decimalDigits")
    private TextField decimalDigits;

    @PropertyId("deltaLogging")
    private TextField deltaLogging;

    @PropertyId("min")
    private TextField min;

    @PropertyId("max")
    private TextField max;

    @PropertyId("typeVar")
    private TypeVarSelect typeVar;

    @PropertyId("typeRead")
    private TypeReadSelect typeRead;

    @PropertyId("format")
    private FormatSelect format;

    @PropertyId("signed")
    private SignedSelect signed;

    @PropertyId("permission")
    private PermissionSelect permission;

    @PropertyId("functionCode")
    private FunctionCodeSelect functionCode;

    @PropertyId("priority")
    private PrioritySelect priority;

    @PropertyId("bitmask")
    private TextField bitmask;

    @PropertyId("qualifier")
    private QualifierSelect qualifier;

    public ModbusRegisterForm(ModbusRegister entity, List<ModbusRegister> others) {
        super(entity, ModbusRegister.class, UiConstants.PROVISIONING, null,null,false);
        this.others = others;

        bindFields();

    }

    protected void initializeFields() {
        address = new TextField(getI18nLabel("address"));
        address.setSizeFull();
        address.setRequiredIndicatorVisible(true);

        displayName = new TextField(getI18nLabel("displayName"));
        displayName.setSizeFull();
        displayName.setRequiredIndicatorVisible(true);

        active = new BooleanOptionGroup();
        active.setLabel(getI18nLabel("active"));
        //active.setSizeFull();

        measureUnit = new BacNetMeasureUnitSelect();
        measureUnit.setLabel(getI18nLabel("measureUnit"));
        measureUnit.setSizeFull();

        scaleMultiplier = new TextField(getI18nLabel("scaleMultiplier"));
        scaleMultiplier.setSizeFull();

        offset = new TextField(getI18nLabel("offset"));
        offset.setSizeFull();

        decimalDigits = new TextField(getI18nLabel("decimalDigits"));
        decimalDigits.setSizeFull();

        deltaLogging = new TextField(getI18nLabel("deltaLogging"));
        deltaLogging.setSizeFull();

        min = new TextField(getI18nLabel("min"));
        min.setSizeFull();

        max = new TextField(getI18nLabel("max"));
        max.setSizeFull();

        typeVar = new TypeVarSelect();
        typeVar.setLabel(getI18nLabel("typeVar"));
        typeVar.setSizeFull();
        typeVar.setClearButtonVisible(false);
        typeVar.setAllowCustomValue(false);

        typeRead = new TypeReadSelect();
        typeRead.setLabel(getI18nLabel("typeRead"));
        typeRead.setSizeFull();
        typeRead.setClearButtonVisible(false);
        typeRead.setAllowCustomValue(false);

        format = new FormatSelect();
        format.setLabel(getI18nLabel("format"));
        format.setSizeFull();

        signed = new SignedSelect();
        signed.setLabel(getI18nLabel("signed"));
        signed.setSizeFull();

        permission = new PermissionSelect();
        permission.setLabel(getI18nLabel("permission"));
        permission.setSizeFull();

        functionCode = new FunctionCodeSelect();
        functionCode.setLabel(getI18nLabel("functionCode"));
        functionCode.setSizeFull();

        priority = new PrioritySelect();
        priority.setLabel(getI18nLabel("priority"));
        priority.setSizeFull();

        bitmask = new TextField(getI18nLabel("bitmask"));
        bitmask.setSizeFull();

        qualifier = new QualifierSelect();
        qualifier.setLabel(getI18nLabel("qualifier"));
        qualifier.setSizeFull();

        addField("address", address);
        addField("displayName", displayName);
        addField("active", active);
        addField("measureUnit", measureUnit);
        addField("scaleMultiplier", scaleMultiplier);
        addField("offset", offset);
        addField("decimalDigits", decimalDigits);
        addField("deltaLogging", deltaLogging);
        addField("min", min);
        addField("max", max);
        addField("typeVar", typeVar);
        addField("typeRead", typeRead);
        addField("format", format);
        addField("signed", signed);
        addField("permission", permission);
        addField("functionCode", functionCode);
        addField("priority", priority);
        addField("bitmask", bitmask);
        addField("qualifier", qualifier);
    }

    protected void bindFields() {
        Binder<ModbusRegister> binder = getBinder();

        binder.forField(address)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToIntegerConverter(getTranslation("invalid.address")))
                .bind(ModbusRegister::getAddress, ModbusRegister::setAddress);

        binder.forField(displayName)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .bind(ModbusRegister::getDisplayName, ModbusRegister::setDisplayName);

        binder.forField(active)
                .bind(ModbusRegister::getActive, ModbusRegister::setActive);

//        binder.forField(measureUnit)
//                .bind(ModbusRegister::getMeasureUnit, ModbusRegister::setMeasureUnit);

        binder.forField(scaleMultiplier)
                .withConverter(new StringToDoubleConverter(getTranslation("invalid_value")))
                .bind(ModbusRegister::getScaleMultiplier, ModbusRegister::setScaleMultiplier);

        binder.forField(offset)
                .withConverter(new StringToDoubleConverter(getTranslation("invalid_value")))
                .bind(ModbusRegister::getOffset, ModbusRegister::setOffset);

        binder.forField(decimalDigits)
                .withConverter(new StringToIntegerConverter(getTranslation("invalid_value")))
                .bind(ModbusRegister::getDecimalDigits, ModbusRegister::setDecimalDigits);

        binder.forField(deltaLogging)
                .withConverter(new StringToDoubleConverter(getTranslation("invalid_value")))
                .bind(ModbusRegister::getDeltaLogging, ModbusRegister::setDeltaLogging);

        binder.forField(min)
                .withConverter(new StringToDoubleConverter(getTranslation("invalid_value")))
                .bind(ModbusRegister::getMin, ModbusRegister::setMin);

        binder.forField(max)
                .withConverter(new StringToDoubleConverter(getTranslation("invalid_value")))
                .bind(ModbusRegister::getMax, ModbusRegister::setMax);

        binder.forField(typeVar)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .bind(ModbusRegister::getTypeVar, ModbusRegister::setTypeVar);

        binder.forField(typeRead)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .bind(ModbusRegister::getTypeRead, ModbusRegister::setTypeRead);

        binder.forField(format)
                .bind(ModbusRegister::getFormat, ModbusRegister::setFormat);

        binder.forField(signed)
                .bind(ModbusRegister::getSigned, ModbusRegister::setSigned);

        binder.forField(permission)
                .bind(ModbusRegister::getPermission, ModbusRegister::setPermission);

        binder.forField(functionCode)
                .bind(ModbusRegister::getFunctionCode, ModbusRegister::setFunctionCode);

        binder.forField(priority)
                .bind(ModbusRegister::getPriority, ModbusRegister::setPriority);

        binder.forField(bitmask)
                .bind(ModbusRegister::getBitmask, ModbusRegister::setBitmask);

        binder.forField(qualifier)
                .bind(ModbusRegister::getQualifier, ModbusRegister::setQualifier);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.getStyle().set("padding", "var(--lumo-space-m)");
        formLayout.add(
                address, displayName, active, measureUnit,
                scaleMultiplier, offset, decimalDigits, deltaLogging,
                min, max, typeVar, typeRead, format, signed,
                permission, functionCode, priority, bitmask, qualifier);

        mainLayout.add(buildPanel(formLayout));
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
        try {
            Integer addressValue = Integer.parseInt(address.getValue());
            TypeRead typeValue = typeRead.getValue();
            Optional<ModbusRegister> match = others.stream()
                    .filter(o -> o.getAddress().equals(addressValue) && o.getTypeRead().equals(typeValue))
                    .findFirst();
            if (match.isPresent()) {
                throw new IllegalArgumentException(match.get().getDisplayName());
            }
        } catch (Exception e) {
            throw new EditorConstraintException(getI18nLabel("invalid.address") + ": " + e.getMessage());
        }
    }


}
