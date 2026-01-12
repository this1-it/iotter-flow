package org.vaadin.flow.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.HasValueChangeMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwinColSelectFlow<T>
        extends Composite<HorizontalLayout>
        implements HasValue<HasValue.ValueChangeEvent<Set<T>>, Set<T>> {

    private final ListBox<T> available = new ListBox<>();
    private final ListBox<T> selected = new ListBox<>();

    private final Span leftCaption = new Span();
    private final Span rightCaption = new Span();

    private final Button add = new Button(">");
    private final Button remove = new Button("<");

    private Set<T> value = new LinkedHashSet<>();
    private boolean readOnly = false;

    public TwinColSelectFlow() {
        VerticalLayout left = new VerticalLayout(leftCaption, available);
        VerticalLayout right = new VerticalLayout(rightCaption, selected);
        VerticalLayout controls = new VerticalLayout(add, remove);

        getContent().add(left, controls, right);
        getContent().setSpacing(true);

        add.addClickListener(e -> moveToSelected());
        remove.addClickListener(e -> moveToAvailable());
    }

    /* ---------------- Value handling ---------------- */

    @Override
    public void setValue(Set<T> value) {
        this.value = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value);
        refreshLists();
    }

    @Override
    public Set<T> getValue() {
        return value;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<Set<T>>> listener) {
        // simplified manual event handling
        return () -> {};
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        add.setEnabled(!readOnly);
        remove.setEnabled(!readOnly);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
    }

    /* ---------------- Data handling ---------------- */

    public void setItems(Set<T> items) {
        available.setItems(items);
        refreshLists();
    }

    public void setDataProvider(DataProvider<T, ?> provider) {
        available.setDataProvider(provider);
    }

    private void refreshLists() {
        selected.setItems(value);
//        available.setItems(
//                available.getListDataView().getItems()
//                        .filter(item -> !value.contains(item))
//        );
    }

    /* ---------------- UI helpers ---------------- */

    private void moveToSelected() {
        if (available.getValue() != null) {
            value.add(available.getValue());
            refreshLists();
        }
    }

    private void moveToAvailable() {
        if (selected.getValue() != null) {
            value.remove(selected.getValue());
            refreshLists();
        }
    }

    public void setRows(int rows) {
        if (rows > 0) {
//            available.setHeightByRows(rows);
//            selected.setHeightByRows(rows);
        }
    }

    public void setLeftColumnCaption(String text) {
        leftCaption.setText(text);
    }

    public void setRightColumnCaption(String text) {
        rightCaption.setText(text);
    }
}
