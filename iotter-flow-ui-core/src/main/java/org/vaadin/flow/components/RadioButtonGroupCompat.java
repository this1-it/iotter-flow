package org.vaadin.flow.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.function.SerializableFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Mimics the Vaadin 8 RadioButtonGroup. Supports {@link #setItemDescriptionGenerator(ItemLabelGenerator)}
 * and {@link #setHtmlContentAllowed(boolean)}.
 * @param <T> the item type.
 */
public class RadioButtonGroupCompat<T> extends RadioButtonGroup<T> {
    
    private ItemLabelGenerator<T> itemLabelGenerator = Object::toString;
    
    private ItemLabelGenerator<T> tooltipGenerator = null;
    private boolean htmlContentAllowed = false;

    public RadioButtonGroupCompat() {
        this(Collections.emptyList());
    }

    /**
     * Mimicks a Vaadin 8 constructor.
     *
     * @param caption ignored - In Vaadin 14, RadioButtonGroup doesn't support a caption.
     * @param items   the items to show.
     */
    @Deprecated
    public RadioButtonGroupCompat( String caption,  Collection<T> items) {
        this(items);
    }

    public RadioButtonGroupCompat(Collection<T> items) {
        // In Vaadin 14, RadioButtonGroup doesn't support a caption.
        setItems(items);
        // In Vaadin 8, the RadioButtonGroup is vertical by default
        setVertical(true);
    }


    public void setItemLabelGenerator(ItemLabelGenerator<T> generator) {
        this.itemLabelGenerator = Objects.requireNonNull(generator);
        updateRenderer();
    }

    private void updateRenderer() {
        if (tooltipGenerator == null && !htmlContentAllowed) {
            // https://github.com/vaadin/flow-components/issues/1681
            setRenderer(new TextRenderer<>(itemLabelGenerator));
        } else {
            setRenderer(new ComponentRenderer<>((SerializableFunction<T, Component>) t -> {
                final String label = itemLabelGenerator.apply(t);
                final Span span = htmlContentAllowed ? new HtmlSpan(label) : new Span(label);
                if (tooltipGenerator != null) {
                    span.getElement().setAttribute("title", tooltipGenerator.apply(t));
                }
                return span;
            }));
        }
    }

    public void setItemDescriptionGenerator(ItemLabelGenerator<T> generator) {
        tooltipGenerator = generator;
        updateRenderer();
    }

    public void setHtmlContentAllowed(boolean htmlContentAllowed) {
        this.htmlContentAllowed = htmlContentAllowed;
        updateRenderer();
    }

    public boolean isHtmlContentAllowed() {
        return htmlContentAllowed;
    }

    
    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }


    public ItemLabelGenerator<T> getItemDescriptionGenerator() {
        return tooltipGenerator;
    }

    public void setVertical(boolean vertical) {
        if (vertical) {
            addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        } else {
            removeThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        }
    }

    public boolean isVertical() {
        return getThemeNames().contains(RadioGroupVariant.LUMO_VERTICAL.getVariantName());
    }
}
