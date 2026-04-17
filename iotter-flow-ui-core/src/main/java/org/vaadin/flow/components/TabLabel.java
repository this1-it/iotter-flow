package org.vaadin.flow.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * A responsive tab header component that shows an icon, a text label, or both.
 * <p>
 * Responsive behavior (via CSS media queries):
 * <ul>
 *   <li>Desktop (&gt;600px): text shown; icon hidden when text is also present</li>
 *   <li>Mobile (&le;600px): icon shown; text hidden when icon is also present</li>
 * </ul>
 * At least one of icon or text must be provided. Text is truncated with ellipsis
 * when it exceeds the max-width defined in {@code tab-label.css}.
 * <p>
 * Use the static factory methods to construct instances:
 * <pre>
 *   TabLabel.of(VaadinIcon.HOME, "Home")   // icon + text
 *   TabLabel.ofText("Settings")            // text only
 *   TabLabel.ofIcon(VaadinIcon.COG)        // icon only
 * </pre>
 */
public class TabLabel extends Composite<HorizontalLayout> {

    private TabLabel(VaadinIcon vaadinIcon, String text) {
        if (vaadinIcon == null && (text == null || text.isBlank())) {
            throw new IllegalArgumentException("TabLabel requires at least one of icon or text");
        }

        HorizontalLayout root = getContent();
        root.addClassName("tab-label");
        root.setPadding(false);
        root.setSpacing(false);
        root.setAlignItems(FlexComponent.Alignment.CENTER);

        if (vaadinIcon != null) {
            Icon icon = vaadinIcon.create();
            icon.addClassName("tab-label-icon");
            icon.getStyle().set("width", "12px").set("height", "12px");
            root.add(icon);
            root.addClassName("tab-label--has-icon");
        } else {
            root.addClassName("tab-label--text-only");
        }

        if (text != null && !text.isBlank()) {
            Span textSpan = new Span(text);
            textSpan.addClassName("tab-label-text");
            root.add(textSpan);
            root.addClassName("tab-label--has-text");
        } else {
            root.addClassName("tab-label--icon-only");
        }
    }

    /**
     * Creates a TabLabel with both an icon and a text label.
     */
    public static TabLabel of(VaadinIcon icon, String text) {
        return new TabLabel(icon, text);
    }

    /**
     * Creates a text-only TabLabel. The text is always shown regardless of screen size.
     */
    public static TabLabel ofText(String text) {
        return new TabLabel(null, text);
    }

    /**
     * Creates an icon-only TabLabel. The icon is always shown regardless of screen size.
     */
    public static TabLabel ofIcon(VaadinIcon icon) {
        return new TabLabel(icon, null);
    }
}
