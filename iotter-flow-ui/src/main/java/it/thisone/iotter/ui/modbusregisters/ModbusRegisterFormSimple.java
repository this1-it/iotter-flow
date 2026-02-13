package it.thisone.iotter.ui.modbusregisters;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.converter.StringToIntegerConverter;

import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.fields.TypeReadSelect;
import it.thisone.iotter.ui.main.UiConstants;

public class ModbusRegisterFormSimple extends AbstractBaseEntityForm<ModbusRegister> {

    private static final long serialVersionUID = 1L;

    private final List<ModbusRegister> others;

    @PropertyId("address")
    private TextField address;

    @PropertyId("typeRead")
    private TypeReadSelect typeRead;

    public ModbusRegisterFormSimple(ModbusRegister entity, List<ModbusRegister> others) {
        super(entity, ModbusRegister.class, UiConstants.PROVISIONING, null,null,false);
        this.others = others;

        bindFields();

    }

    protected void initializeFields() {
        address = new TextField(getI18nLabel("address"));
        address.setSizeFull();
        address.setRequiredIndicatorVisible(true);

        typeRead = new TypeReadSelect();
        typeRead.setLabel(getI18nLabel("typeRead"));
        typeRead.setSizeFull();
        typeRead.setClearButtonVisible(false);
        typeRead.setAllowCustomValue(false);
        typeRead.setRequiredIndicatorVisible(true);

        addField("address", address);
        addField("typeRead", typeRead);
    }

    protected void bindFields() {
        Binder<ModbusRegister> binder = getBinder();

        binder.forField(address)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToIntegerConverter(getTranslation("invalid.address")))
                .bind(ModbusRegister::getAddress, ModbusRegister::setAddress);

        binder.forField(typeRead)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .bind(ModbusRegister::getTypeRead, ModbusRegister::setTypeRead);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.getStyle().set("padding", "var(--lumo-space-m)");
        formLayout.add(address, typeRead);

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
