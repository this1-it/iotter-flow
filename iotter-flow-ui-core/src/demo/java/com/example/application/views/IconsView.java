package com.example.application.views;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.poi.ss.formula.functions.T;
import org.vaadin.flow.components.TabSheet;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.textfield.TextField;

public class IconsView extends VerticalLayout {

    public IconsView() {
        TabSheet multicomponent = new TabSheet();
        multicomponent.setSizeFull();
        multicomponent.addTab("Vaadin Icons", buildVaadinIcons());
        multicomponent.addTab("Font Awesome Regular", buildFontAweSomeIcons());
        multicomponent.addTab("Font Awesome Solid", buildFontAweSomeSolidIcons());

        add(multicomponent);
        setSizeFull();

    }

    private Component buildVaadinIcons() {
        List<VaadinIcon> icons = Arrays.stream(VaadinIcon.values())
                .sorted(Comparator.comparing(VaadinIcon::name))
                .collect(Collectors.toList());
        return buildIconGrid(icons);
    }

    private Component buildFontAweSomeIcons() {
        List<FontAwesome.Regular> icons = Arrays.stream(FontAwesome.Regular.values())
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
        return buildIconGrid(icons);
    }

    private Component buildFontAweSomeSolidIcons() {
        List<FontAwesome.Solid> icons = Arrays.stream(FontAwesome.Solid.values())
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
        return buildIconGrid(icons);
    }

    private <T extends Enum<T> & IconFactory> Component buildIconGrid(List<T> icons) {
        ListDataProvider<T> dataProvider = new ListDataProvider<>(icons);

        Grid<T> grid = new Grid<>();
        grid.setDataProvider(dataProvider);
        grid.addColumn(new ComponentRenderer<>(icon -> icon.create()))
                .setHeader("Icon")
                .setFlexGrow(0)
                .setWidth("80px");
        grid.addColumn(this::formatIconName)
                .setHeader("Name")
                .setSortable(true);
        grid.setSizeFull();

        TextField filter = new TextField();
        filter.setPlaceholder("Search icon");
        filter.setClearButtonVisible(true);
        filter.setWidthFull();
        filter.addValueChangeListener(event -> {
            String term = event.getValue() == null ? "" : event.getValue().trim().toLowerCase();
            dataProvider.setFilter(icon -> formatIconName(icon).contains(term));
        });

        VerticalLayout layout = new VerticalLayout(filter, grid);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setSizeFull();
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.setFlexGrow(1, grid);
        return layout;
    }

    private String formatIconName(Enum<?> icon) {
        return icon.name().toLowerCase().replace('_', '-');
    }
}
