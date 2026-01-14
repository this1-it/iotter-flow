package it.thisone.iotter.ui.common.fields;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.datepicker.DatePicker;

import it.thisone.iotter.ui.common.UIUtils;

public class LegacyDateField
        extends AbstractCompositeField<DatePicker, LegacyDateField, Date> {

    private static final long serialVersionUID = 1L;

    private final DatePicker internalField;
    private final ZoneId zoneId;

    public LegacyDateField() {
        super(null); 

        this.zoneId = UIUtils.getBrowserTimeZone().toZoneId();
        this.internalField = getContent();

        internalField.addValueChangeListener(e -> updateModelValue());
    }

    public LegacyDateField(String label) {
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
                value.toInstant()
                     .atZone(zoneId)
                     .toLocalDate()
            );
        }
    }

    private void updateModelValue() {
        LocalDate localDate = internalField.getValue();
        Date date = (localDate == null)
                ? null
                : Date.from(localDate.atStartOfDay(zoneId).toInstant());

        setModelValue(date, true);
    }

    // ---------- Delegated API (Flow equivalents) ----------

    public void setMin(Date min) {
        internalField.setMin(toLocalDate(min));
    }

    public Date getMin() {
        return toDate(internalField.getMin());
    }

    public void setMax(Date max) {
        internalField.setMax(toLocalDate(max));
    }

    public Date getMax() {
        return toDate(internalField.getMax());
    }

    public void setLocale(java.util.Locale locale) {
        internalField.setLocale(locale);
    }

    // ---------- Conversion helpers ----------

    private LocalDate toLocalDate(Date date) {
        return date == null
                ? null
                : date.toInstant().atZone(zoneId).toLocalDate();
    }

    private Date toDate(LocalDate localDate) {
        return localDate == null
                ? null
                : Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }
}
