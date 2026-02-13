package it.thisone.iotter.ui.visualizers.controlpanel;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.ui.visualizers.controlpanel.IconSetResolver.IconSet;
import it.thisone.iotter.ui.visualizers.controlpanel.IconSetResolver.IconSetEnum;

public class IconSetField extends CustomField<String> {

    private static final long serialVersionUID = -4610489795190467557L;

    private String currentValue;
    private Button preview;
    private VerticalLayout content;
    private Dialog picker;
    private IconSetResolver resolver;

    public IconSetField() {
        resolver = new IconSetResolver();
    }


    protected Component initContent() {
        if (content == null) {
            if (picker == null) {
                picker = new Dialog();
                picker.setCloseOnEsc(true);
                picker.setCloseOnOutsideClick(true);
                picker.add(buildPopup());
            }
            preview = createButton(resolver.resolveIconSetName(IconSetEnum.ICON_SET_01.name()));
            preview.addClickListener(event -> picker.open());

            content = new VerticalLayout(preview);
            content.setPadding(false);
            content.setSpacing(false);
        }
        return content;
    }

    @Override
    protected String generateModelValue() {
        return currentValue;
    }

    @Override
    protected void setPresentationValue(String value) {
        this.currentValue = value;
        IconSet set = resolver.resolveIconSetName(value);
        showIcons(set, preview);
    }

    private Component buildPopup() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("aernetpro-section");
        layout.setSpacing(false);
        layout.setPadding(true);

        for (IconSetEnum literal : IconSetEnum.values()) {
            final IconSet set = resolver.resolveIconSetName(literal.toString());
            Button option = createButton(set);
            option.addClickListener(event -> {
                setModelValue(set.getType().getDisplayName(), true);
                picker.close();
            });
            layout.add(option);
        }

        return layout;
    }

    private Button createButton(IconSet set) {
        Button button = new Button();
        button.addClassName("tiny");
        showIcons(set, button);
        return button;
    }

    private void showIcons(IconSet set, Button button) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.add(new Span(set.getType().toString()));
        for (int i = 0; i < set.getIcons().length; i++) {
            int value = set.getValues()[i];
            Div iconWrapper = new Div();
            iconWrapper.add(set.createIconValue());
            iconWrapper.addClassName("v-icon");
            row.add(iconWrapper);
            row.add(new Span(String.valueOf(value)));
        }
        // button.removeAll();
        // button.add(row);
    }
}
