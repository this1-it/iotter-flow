package it.thisone.iotter.ui.common.fields;
import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.util.PopupNotification;

public class ChannelSelect extends AbstractCompositeField<VerticalLayout, ChannelSelect, Channel> {

	private static final long serialVersionUID = -5206805427856901777L;
	private VerticalLayout layout;
	private ComboBox<MeasureUnit> measures;
	private Span channelLabel;
	private Channel value;
	private DeviceService deviceService;

	private static final String name = "channel.select";

	public ChannelSelect() {
		super(null);
		VerticalLayout layout = getContent();
		layout.setSpacing(true);
		channelLabel = new Span();

		measures = new ComboBox<MeasureUnit>();
		measures.setLabel(getI18nLabel("measure"));
		layout.add(measures);
	}

	public void setDeviceService(DeviceService deviceService) {
		this.deviceService = deviceService;
		measures.setItemLabelGenerator(measure -> deviceService.getUnitOfMeasureName(measure.getType()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		measures.setEnabled(enabled);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
	}

	public void setMeasure(MeasureUnit measure) {
		Channel channel = getValue();
		if (channel != null) {
			populateMeasures(channel, measure);
		}
	}

	public MeasureUnit getMeasure() {
		return (MeasureUnit) measures.getValue();
	}

	private void populateMeasures(Channel channel, MeasureUnit value) {
		if (!channel.getMeasures().isEmpty()) {
			measures.setItems(channel.getMeasures());
			measures.setEnabled(true);
		}
		measures.setValue(value);

		measures.addValueChangeListener(event -> {
			PopupNotification.show(getI18nLabel("measure_unit_warning"), PopupNotification.Type.WARNING);
		});
	}

	public String getI18nLabel(String key) {
		return getTranslation(getI18nKey() + "." + key);
	}

	public String getI18nKey() {
		return name;
	}

	@Override
	public Channel getValue() {
		return value;
	}

	@Override
	protected void setPresentationValue(Channel chnl) {
		if (chnl != null) {
			value = chnl;
			channelLabel.setText(String.format("%s [%d]", chnl.toString(), chnl.getConfiguration().getQualifier()));
		}
	}
}
