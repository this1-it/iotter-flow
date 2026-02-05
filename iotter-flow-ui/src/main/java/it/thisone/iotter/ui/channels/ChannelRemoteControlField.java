package it.thisone.iotter.ui.channels;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelRemoteControl;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.ui.common.charts.ChannelUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;

public class ChannelRemoteControlField extends CustomField<ChannelRemoteControl> {

	private static final long serialVersionUID = -7857658199046016587L;

	private ChannelRemoteControl currentValue;
	private final String topic;
	private final Double min;
	private final Double max;
	private final String key;
	private final String serial;
	private final Device device;

	private HorizontalLayout layout;
	private ComboBox<Double> combo;
	private TextField textField;

	public ChannelRemoteControlField(Channel channel) {
		key = channel.getKey();
		device = channel.getDevice();
		serial = channel.getDevice().getSerial();
		min = channel.getRemote().getMin().doubleValue();
		max = channel.getRemote().getMax().doubleValue();
		topic = channel.getRemote().getTopic();

		ChannelRemoteControl item = new ChannelRemoteControl();
		item.setMax(max.floatValue());
		item.setMin(min.floatValue());
		item.setTopic(topic);

		FeedKey feedKey = new FeedKey(serial, key);
		MeasureRaw lastMeasure = ChartUtils.lastMeasure(feedKey);
		if (lastMeasure != null && lastMeasure.getValue() != null) {
			try {
				item.setValue((Double) ChartUtils.calculateMeasure(lastMeasure.getValue(), channel.getDefaultMeasure()));
			} catch (Exception ignored) {
			}
		}
		currentValue = item;
		combo = ChannelUtils.enumComboBox(channel);
	}

	public void setValidationError(String message) {
		setInvalid(message != null && !message.isEmpty());
		setErrorMessage(message);
	}

	
	protected Component initContent() {
		if (layout != null) {
			return layout;
		}
		layout = new HorizontalLayout();
		layout.setWidthFull();
		layout.setSpacing(true);
		layout.setPadding(true);

		if (combo != null) {
			Double value = getValue() != null ? getValue().getValue() : null;
			if (value != null) {
				combo.setValue(value);
			}
			combo.addValueChangeListener(event -> updateValue(event.getValue()));
			combo.setWidthFull();
			layout.add(combo);
			return layout;
		}

		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(getLocale());
		decimalFormat.setGroupingUsed(false);
		decimalFormat.setMaximumFractionDigits(4);
		decimalFormat.setRoundingMode(RoundingMode.FLOOR);

		String placeholder = String.format("%s:%s, %s:%s",
				getTranslation("channel.remote.min"), decimalFormat.format(min),
				getTranslation("channel.remote.max"), decimalFormat.format(max));

		textField = new TextField();
		textField.setPlaceholder(placeholder);
		textField.setWidthFull();
		if (getValue() != null && getValue().getValue() != null) {
			textField.setValue(decimalFormat.format(getValue().getValue()));
		}

		Button button = new Button(VaadinIcon.CHECK.create());
		button.addClickListener(event -> {
			try {
				Double doubleValue = Double.parseDouble(textField.getValue());
				if (doubleValue < min || doubleValue > max) {
					setValidationError(getTranslation("channel.remote.not_in_range"));
					return;
				}
				updateValue(doubleValue);
				setValidationError(null);
			} catch (NumberFormatException e) {
				setValidationError(getTranslation("validators.conversion_error"));
			}
		});

		layout.add(textField, button);
		layout.setFlexGrow(1f, textField);
		return layout;
	}

	private void updateValue(Double value) {
		ChannelRemoteControl item = new ChannelRemoteControl();
		item.setMax(max.floatValue());
		item.setMin(min.floatValue());
		item.setTopic(topic);
		item.setValue(value);
		setValue(item);
	}

	@Override
	protected ChannelRemoteControl generateModelValue() {
		return currentValue;
	}

	@Override
	protected void setPresentationValue(ChannelRemoteControl value) {
		currentValue = value;
	}


	public String getKey() {
		return key;
	}

	public String getSerial() {
		return serial;
	}

	public Device getDevice() {
		return device;
	}
}
