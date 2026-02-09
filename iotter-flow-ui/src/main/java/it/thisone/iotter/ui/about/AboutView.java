package it.thisone.iotter.ui.about;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        List<VaadinIcon> icons = Arrays.stream(VaadinIcon.values())
                .sorted(Comparator.comparing(VaadinIcon::name))
                .collect(Collectors.toList());

        Grid<VaadinIcon> grid = new Grid<>();
        grid.setItems(icons);
        grid.addColumn(new ComponentRenderer<>(icon -> icon.create())).setHeader("Icon").setFlexGrow(0).setWidth("80px");
        grid.addColumn(VaadinIcon::name).setHeader("Name").setSortable(true);
        grid.setSizeFull();

        add(VaadinIcon.INFO_CIRCLE.create(), versionSpan, grid);
        setSizeFull();
    }
}
