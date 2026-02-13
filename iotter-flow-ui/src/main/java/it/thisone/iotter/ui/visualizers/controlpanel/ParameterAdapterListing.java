package it.thisone.iotter.ui.visualizers.controlpanel;

import static it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseForm.CONTROLPANELBASE_EDITOR;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseConstants;
import it.thisone.iotter.ui.model.ChannelAdapter;
import it.thisone.iotter.ui.model.ChannelAdapterDataProvider;
import it.thisone.iotter.ui.visualizers.TandemTraceChartAdapter;

public class ParameterAdapterListing extends VerticalLayout implements ControlPanelBaseConstants {

    private static final String LABEL = "label";
    private static final String CHECKED = "checked";
    private static final String LAST_MEASURE = "lastMeasureValueUnit";

    private final Grid<ChannelAdapter> grid;
    private final Object target;
    private final boolean anonymous;
    private final ChannelAdapterDataProvider dataProvider;

    private static final long serialVersionUID = 2001077544797472399L;

    public String getI18nLabel(String key) {
        return getTranslation(getI18nKey() + "." + key);
    }

    public String getI18nKey() {
        return CONTROLPANELBASE_EDITOR;
    }

    public ParameterAdapterListing(ChannelAdapterDataProvider container, Object component) {
        super();
        this.target = component;
        this.dataProvider = container;
        // NOTE: user/session based enablement still depends on legacy UIUtils wiring.
        this.anonymous = false;

        setPadding(false);
        setSpacing(false);
        setSizeFull();

        grid = new Grid<>();
        grid.addClassName("parameter-grid");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

        Column<ChannelAdapter> labelColumn = grid
                .addComponentColumn(adapter -> {
                    Span span = new Span(adapter.getLabel());
                    if (adapter.isChecked() && adapter.getFillColor() != null) {
                        span.getStyle().set("color", adapter.getFillColor());
                    }
                    return span;
                })
                .setKey(LABEL)
                .setHeader(getI18nLabel("parameter"))
                .setWidth("235px")
                .setFlexGrow(0);

        grid.addColumn(ChannelAdapter::getLastMeasureValueUnit)
                .setKey(LAST_MEASURE)
                .setHeader(getI18nLabel("lastMeasure"));

        Column<ChannelAdapter> checkedColumn = grid
                .addComponentColumn(adapter -> {
                    Checkbox checkBox = new Checkbox(adapter.isChecked());
                    checkBox.setEnabled(!anonymous);
                    checkBox.addValueChangeListener(event -> {
                        adapter.setChecked(Boolean.TRUE.equals(event.getValue()));
                        dataProvider.refreshItem(adapter);
                    });
                    return checkBox;
                })
                .setKey(CHECKED)
                .setHeader("")
                .setWidth("70px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);

        grid.setEnabled(!anonymous);

        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder(getI18nLabel("parameter"));
        nameFilter.setClearButtonVisible(true);
        nameFilter.setWidthFull();
        nameFilter.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value == null || value.trim().isEmpty()) {
                dataProvider.clearFilters();
            } else {
                String filter = value.trim().toLowerCase();
                dataProvider.setFilter(item -> {
                    String label = item.getLabel();
                    return label != null && label.toLowerCase().contains(filter);
                });
            }
        });

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(labelColumn).setComponent(nameFilter);
        filterRow.getCell(checkedColumn).setComponent(applyRefresh());

        add(grid);
        expand(grid);
    }

    protected Button applyRefresh() {
        Button button = new Button(new Icon(VaadinIcon.REFRESH));
        button.addClassName("tiny");
        button.addClassName("borderless");
        button.setEnabled(!anonymous);

        button.addClickListener(event -> {
            List<String> checked = dataProvider.getItems().stream()
                    .filter(ChannelAdapter::isChecked)
                    .map(ChannelAdapter::getKey)
                    .collect(Collectors.toList());
            if (target instanceof TandemTraceChartAdapter) {
                ((TandemTraceChartAdapter) target).applyChecked(checked);
            }
        });
        return button;
    }

    public Grid<ChannelAdapter> getGrid() {
        return grid;
    }
}
