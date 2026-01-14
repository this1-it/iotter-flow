package it.thisone.iotter.ui.common.fields;



import com.github.juchar.colorpicker.ColorPickerFieldRaw;
import com.vaadin.flow.component.AbstractCompositeField;

public class ColorField
        extends AbstractCompositeField<ColorPickerFieldRaw, ColorField, String> {

    private static final long serialVersionUID = 1L;

    private final ColorPickerFieldRaw picker;

    public ColorField() {
        super(null); // default value null, server-side

        picker = getContent();

        // Default configuration (can be overridden via delegating methods)
        picker.setPinnedPalettes(true);
        picker.setHexEnabled(false);
        picker.setPalette(
                "#ff0000",
                "#00ff00",
                "#0000ff",
                "--lumo-contrast"
        );
        picker.setChangeFormatButtonVisible(true);
        picker.setCssCustomPropertiesEnabled(true);
        picker.setWidth("400px");

        // Sync internal value → composite field value
        picker.addValueChangeListener(e ->
                setModelValue(e.getValue(), true)
        );
    }

    public ColorField(String label) {
        this();
        picker.setLabel(label);
    }

    // ---------- Value lifecycle ----------

    @Override
    protected void setPresentationValue(String value) {
        if (value == null) {
            picker.clear();
        } else {
            picker.setValue(value);
        }
    }

    // ---------- Delegated API ----------

    public void setPinnedPalettes(boolean pinned) {
        picker.setPinnedPalettes(pinned);
    }

    public void setHexEnabled(boolean enabled) {
        picker.setHexEnabled(enabled);
    }

    public void setPalette(String... colors) {
        picker.setPalette(colors);
    }

    public void setChangeFormatButtonVisible(boolean visible) {
        picker.setChangeFormatButtonVisible(visible);
    }

    public void setCssCustomPropertiesEnabled(boolean enabled) {
        picker.setCssCustomPropertiesEnabled(enabled);
    }

    public void setWidth(String width) {
        picker.setWidth(width);
    }
}

