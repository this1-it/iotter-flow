package it.thisone.iotter.ui.about;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.example.application.views.AccordionView;
import com.example.application.views.ButtonView;
import com.example.application.views.DatePickerView;
import com.example.application.views.DateTimePickerView;
import com.example.application.views.GridView;
import com.example.application.views.HomeView;
import com.example.application.views.TreeGridView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.flow.components.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Version;

import it.thisone.iotter.ui.MainLayout;

@Route(value = "About", layout = MainLayout.class)
@PageTitle("About")
public class AboutView extends VerticalLayout {

    public static final String VIEW_NAME = "About";

    public AboutView() {
        Span versionSpan = new Span(getTranslation("about.view.version",
                Version.getFullVersion()));


        TabSheet multicomponent = new TabSheet();
		multicomponent.setSizeFull();

		multicomponent.addTab("Theme demo", new HomeView());
        multicomponent.addTab("Accordion", new AccordionView());
        multicomponent.addTab("Button", new ButtonView());
        multicomponent.addTab("Grid", new GridView());
        multicomponent.addTab("TreeGrid", new TreeGridView());
        multicomponent.addTab("DatePickerV", new DatePickerView());
        multicomponent.addTab("DateTimePicker", new DateTimePickerView());
        multicomponent.addTab("Icons", buildIcons());



        add(VaadinIcon.INFO_CIRCLE.create(), versionSpan, multicomponent);
        setSizeFull();
    }

    private Component buildIcons() {
                List<VaadinIcon> icons = Arrays.stream(VaadinIcon.values())
                .sorted(Comparator.comparing(VaadinIcon::name))
                .collect(Collectors.toList());

        Grid<VaadinIcon> grid = new Grid<>();
        grid.setItems(icons);
        grid.addColumn(new ComponentRenderer<>(icon -> icon.create())).setHeader("Icon").setFlexGrow(0).setWidth("80px");
        grid.addColumn(VaadinIcon::name).setHeader("Name").setSortable(true);
        grid.setSizeFull();
        return grid;
    }
}
