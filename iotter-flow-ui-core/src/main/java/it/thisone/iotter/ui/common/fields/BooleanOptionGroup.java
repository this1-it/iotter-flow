package it.thisone.iotter.ui.common.fields;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import it.thisone.iotter.ui.common.UIUtils;

public class BooleanOptionGroup extends RadioButtonGroup<Boolean> {

    private static final long serialVersionUID = 1L;

    public BooleanOptionGroup() {
        super();

        // Ordered labels: false first, then true
        Map<Boolean, String> labels = new LinkedHashMap<>();
        labels.put(false, UIUtils.localize("enum.boolean.false"));
        labels.put(true, UIUtils.localize("enum.boolean.true"));

        setItems(labels.keySet());

        // Vaadin 14-compatible label rendering
        setRenderer(new ComponentRenderer<>(value ->
                new Span(labels.get(value))
        ));
    }
}
