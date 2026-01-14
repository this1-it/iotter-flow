package it.thisone.iotter.ui.common.fields;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;

import it.thisone.iotter.ui.common.UIUtils;

public class LegacyDateTimeField
        extends AbstractCompositeField<DateTimePicker, LegacyDateTimeField, Date> {

    private static final long serialVersionUID = 1L;

    private final DateTimePicker internalField;
    private final ZoneId zoneId;

    public LegacyDateTimeField() {
        super(null); // default value null, server-side

        this.zoneId = UIUtils.getBrowserTimeZone().toZoneId();
        this.internalField = getContent();

        internalField.addValueChangeListener(e -> updateModelValue());
    }

    public LegacyDateTimeField(String label) {
        this();
        internalField.setLabel(label);
    }

    // ---------- Value lifecycle ----------

    @Override
    protected void setPresentationValue(Date value) {
        if (value == null) {
            internalField.clear();
        } else {
            internalField.setValue(
                LocalDateTime.ofInstant(value.toInstant(), zoneId)
            );
        }
    }

    private void updateModelValue() {
        LocalDateTime ldt = internalField.getValue();
        Date date = (ldt == null)
                ? null
                : Date.from(ldt.atZone(zoneId).toInstant());

        setModelValue(date, true);
    }

    // ---------- Delegated API (adjusted for Flow) ----------

    public void setStep(java.time.Duration step) {
        internalField.setStep(step);
    }

    public void setMin(Date min) {
        internalField.setMin(toLocalDateTime(min));
    }

    public Date getMin() {
        return toDate(internalField.getMin());
    }

    public void setMax(Date max) {
        internalField.setMax(toLocalDateTime(max));
    }

    public Date getMax() {
        return toDate(internalField.getMax());
    }

    public void setLocale(java.util.Locale locale) {
        internalField.setLocale(locale);
    }

    // ---------- Conversion helpers ----------

    private LocalDateTime toLocalDateTime(Date date) {
        return date == null
                ? null
                : LocalDateTime.ofInstant(date.toInstant(), zoneId);
    }

    private Date toDate(LocalDateTime ldt) {
        return ldt == null
                ? null
                : Date.from(ldt.atZone(zoneId).toInstant());
    }
}
