package it.thisone.iotter.ui.channels;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.converter.StringToFloatConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.FormFieldOrder;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChannelUtils;

// Feature #382 Feed Alarms Thresholds
public class ChannelAlarmForm extends AbstractBaseEntityForm<Channel> {

    private static final long serialVersionUID = 5961351166441753740L;

    @FormFieldOrder(1)
    @PropertyId("alarm.armed")
    private Checkbox armed;

    @FormFieldOrder(2)
    @PropertyId("alarm.notify")
    private Checkbox notify;

    @FormFieldOrder(3)
    private ComboBox<Boolean> digitalThreshold;

    @FormFieldOrder(4)
    @PropertyId("alarm.lowLow")
    private TextField lowLow;

    @FormFieldOrder(5)
    @PropertyId("alarm.low")
    private TextField low;

    @FormFieldOrder(6)
    @PropertyId("alarm.high")
    private TextField high;

    @FormFieldOrder(7)
    @PropertyId("alarm.highHigh")
    private TextField highHigh;

    @FormFieldOrder(8)
    @PropertyId("alarm.delayMinutes")
    private TextField delayMinutes;

    @FormFieldOrder(9)
    @PropertyId("alarm.repeatMinutes")
    private TextField repeatMinutes;

    public ChannelAlarmForm(Channel entity, boolean readOnly) {
        super(entity, Channel.class, "channel.alarm", null, null, readOnly);
        bindFields();
        configureDigitalThresholdChoice(entity);
    }

    @Override
    protected void initializeFields() {
        armed = new Checkbox(getI18nLabel("armed"));
        armed.setSizeFull();
        armed.setReadOnly(isReadOnly());

        notify = new Checkbox(getI18nLabel("notify"));
        notify.setSizeFull();
        notify.setReadOnly(isReadOnly());

        digitalThreshold = new ComboBox<>(getI18nLabel("digital"));
        digitalThreshold.setSizeFull();
        digitalThreshold.setAllowCustomValue(false);
        digitalThreshold.setReadOnly(isReadOnly());
        

        lowLow = createThresholdField("lowLow");
        lowLow.setReadOnly(isReadOnly());
        
        low = createThresholdField("low");
        low.setReadOnly(isReadOnly());
        
        high = createThresholdField("high");
        high.setReadOnly(isReadOnly());
        
        highHigh = createThresholdField("highHigh");
        highHigh.setReadOnly(isReadOnly());
        
        delayMinutes = createNumberField("delayMinutes");
        delayMinutes.setReadOnly(isReadOnly());
        
        repeatMinutes = createNumberField("repeatMinutes");
        repeatMinutes.setReadOnly(isReadOnly());
        
    }

    private TextField createThresholdField(String key) {
        TextField field = new TextField(getI18nLabel(key));
        field.setSizeFull();
        field.setRequiredIndicatorVisible(true);
        return field;
    }

    private TextField createNumberField(String key) {
        TextField field = new TextField(getI18nLabel(key));
        field.setSizeFull();
        return field;
    }

    @Override
    protected void bindFields() {
        getBinder().forField(armed)
                .bind(channel -> channel.getAlarm().isArmed(),
                        (channel, value) -> channel.getAlarm().setArmed(Boolean.TRUE.equals(value)));

        getBinder().forField(notify)
                .bind(channel -> channel.getAlarm().isNotify(),
                        (channel, value) -> channel.getAlarm().setNotify(Boolean.TRUE.equals(value)));

        getBinder().forField(lowLow)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToFloatConverter(getI18nLabel("invalid.thresholds")))
                .bind(channel -> channel.getAlarm().getLowLow(), (channel, value) -> channel.getAlarm().setLowLow(value));

        getBinder().forField(low)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToFloatConverter(getI18nLabel("invalid.thresholds")))
                .bind(channel -> channel.getAlarm().getLow(), (channel, value) -> channel.getAlarm().setLow(value));

        getBinder().forField(high)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToFloatConverter(getI18nLabel("invalid.thresholds")))
                .bind(channel -> channel.getAlarm().getHigh(), (channel, value) -> channel.getAlarm().setHigh(value));

        getBinder().forField(highHigh)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withConverter(new StringToFloatConverter(getI18nLabel("invalid.thresholds")))
                .bind(channel -> channel.getAlarm().getHighHigh(), (channel, value) -> channel.getAlarm().setHighHigh(value));

        getBinder().forField(delayMinutes)
                .withConverter(new StringToIntegerConverter(getI18nLabel("invalid.thresholds")))
                .withValidator(value -> value == null || (value >= 10 && value <= 30), "min 10 ...  max 30")
                .bind(channel -> channel.getAlarm().getDelayMinutes(),
                        (channel, value) -> channel.getAlarm().setDelayMinutes(value));

        getBinder().forField(repeatMinutes)
                .withConverter(new StringToIntegerConverter(getI18nLabel("invalid.thresholds")))
                .withValidator(value -> value == null || (value >= 10 && value <= 30), "min 10 ...  max 30")
                .bind(channel -> channel.getAlarm().getRepeatMinutes(),
                        (channel, value) -> channel.getAlarm().setRepeatMinutes(value));
    }

    private void configureDigitalThresholdChoice(Channel channel) {
        digitalThreshold.setItems(true, false);
        digitalThreshold.setItemLabelGenerator(
                value -> Boolean.TRUE.equals(value) ? getI18nLabel("digital.up") : getI18nLabel("digital.down"));
        digitalThreshold.addValueChangeListener(event -> {
            Boolean value = event.getValue();
            if (value != null) {
                applyDigitalThresholdPreset(value);
            }
        });

        if (ChannelUtils.isTypeAlarm(channel) || ChannelUtils.isTypeDigital(channel)) {
            if (channel.getAlarm().isEmpty()) {
                digitalThreshold.setValue(true);
            } else {
                digitalThreshold.setValue(channel.getAlarm().getLowLow() != null && channel.getAlarm().getLowLow() < 0);
            }
            hideAnalogThresholds();
        } else {
            digitalThreshold.setVisible(false);
        }
    }

    private void hideAnalogThresholds() {
        lowLow.setVisible(false);
        low.setVisible(false);
        high.setVisible(false);
        highHigh.setVisible(false);
    }

    private void applyDigitalThresholdPreset(boolean upDirection) {
        if (upDirection) {
            lowLow.setValue("-1");
            low.setValue("-1");
            high.setValue("0");
            highHigh.setValue("1");
        } else {
            lowLow.setValue("0");
            low.setValue("1");
            high.setValue("2");
            highHigh.setValue("2");
        }
    }

//    @Override
//    public VerticalLayout getFieldsLayout() {
//        FormLayout layout = new FormLayout();
//        layout.add(armed, notify, digitalThreshold, lowLow, low, high, highHigh, delayMinutes, repeatMinutes);
//        VerticalLayout mainLayout = buildMainLayout();
//        mainLayout.add(buildPanel(layout));
//        return mainLayout;
//    }


    @Override
    protected void afterCommit() {
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
        validateAlarmThresholds();
    }



    private void validateAlarmThresholds() throws EditorConstraintException {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(UIUtils.getLocale());
        try {
            Float lowLowValue = decimalFormat.parse(lowLow.getValue()).floatValue();
            Float lowValue = decimalFormat.parse(low.getValue()).floatValue();
            Float highHighValue = decimalFormat.parse(highHigh.getValue()).floatValue();
            Float highValue = decimalFormat.parse(high.getValue()).floatValue();

            if (lowValue < lowLowValue) {
                throw new EditorConstraintException(
                        getI18nLabel("invalid.low") + String.format(": %f < %f", lowValue, lowLowValue));
            }
            if (highValue > highHighValue) {
                throw new EditorConstraintException(
                        getI18nLabel("invalid.high") + String.format(": %f > %f", highValue, highHighValue));
            }
            if (lowValue > highValue) {
                throw new EditorConstraintException(
                        getI18nLabel("invalid.low") + String.format(": %f > %f", lowValue, highValue));
            }
            if (lowLowValue >= highHighValue) {
                throw new EditorConstraintException(
                        getI18nLabel("invalid.lowLow") + String.format(": %f >= %f", lowLowValue, highHighValue));
            }
        } catch (ParseException e) {
            throw new EditorConstraintException(getI18nLabel("invalid.thresholds"));
        }
    }
}
