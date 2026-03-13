package it.thisone.iotter.ui.graphicfeeds;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.ChartThreshold;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.AbstractBaseEntityListing;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.PopupNotification;

public class ChartThresholdMasterDetailsListing extends AbstractBaseEntityListing<ChartThreshold> {

    private static final long serialVersionUID = 6629896864484892844L;

    private GraphicFeed feed;
    private Grid<ChartThreshold> grid;
    private ListDataProvider<ChartThreshold> dataProvider;
    private Grid.Column<ChartThreshold> valueColumn;
    private Grid.Column<ChartThreshold> labelColumn;
    private boolean suppressSelection;

    public ChartThresholdMasterDetailsListing() {
        super(ChartThreshold.class, "graphthreshold.listing", "graphthreshold.listing", false);
        setPermissions(new Permissions(true));

        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.setWidthFull();
        setEditorLayout(editorLayout);

        grid = createGrid();
        setSelectable(grid);
        setDataProvider(dataProvider);

        grid.addSelectionListener(event -> {
            if (suppressSelection) {
                return;
            }
            ChartThreshold selected = event.getAllSelectedItems().stream().findFirst().orElse(null);
            openEditor(selected, "");
        });

        VerticalLayout split = new VerticalLayout(editorLayout, grid);
        split.setSizeFull();
        split.setSpacing(true);
        split.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        split.setFlexGrow(1f, grid);

        getMainLayout().add(split);
        getMainLayout().setFlexGrow(1f, split);
    }

    @Override
    protected AbstractBaseEntityForm<ChartThreshold> getEditor(ChartThreshold item, boolean readOnly) {
        return new ChartThresholdForm(item, readOnly);
    }

    @Override
    protected void openDetails(ChartThreshold item) {
        openEditor(item, getI18nLabel("view_dialog"));
    }

    @Override
    protected void openRemove(ChartThreshold item) {
        if (item == null) {
            return;
        }
        removeThreshold(item);
        openEditor(null, "");
    }

    private void openEditor(ChartThreshold item, String caption) {
        if (feed == null) {
            return;
        }

        boolean create = false;
        ChartThreshold target = item;
        if (target == null) {
            target = new ChartThreshold();
            target.setFillColor(ChartUtils.quiteRandomHexColor());
            create = true;
            suppressSelection = true;
            grid.deselectAll();
            suppressSelection = false;
        }
        target.setFeed(feed);

        ChartThresholdForm content = (ChartThresholdForm) getEditor(target, false);
        content.getValueField().setPlaceholder("i.e. 42");

        String createKey = "create_threshold";
        String modifyKey = "modify_threshold";
        if (feed.getWidget().getType().equals(GraphicWidgetType.WIND_ROSE)) {
            createKey = "create_category";
            modifyKey = "modify_category";
            content.getLabelField().setVisible(false);
        }

        String resolvedCaption = create ? getI18nLabel(createKey) : getI18nLabel(modifyKey);
        content.setId((caption == null || caption.isEmpty() ? resolvedCaption : caption).replace(' ', '-'));

        if (!create) {
            content.setDeleteHandler(entity -> {
                removeThreshold(entity);
                openEditor(null, "");
            });
        }

        content.setSavedHandler(entity -> {
            if (hasDuplicateValue(entity)) {
                PopupNotification.show(getI18nLabel("invalid_value"), PopupNotification.Type.WARNING);
                return;
            }
            if (entity.getId() == null) {
                entity.setId(EncryptUtils.getUniqueId());
            }
            entity.setFeed(feed);
            entity.setOwner(feed.getOwner());
            if (!dataProvider.getItems().contains(entity)) {
                dataProvider.getItems().add(entity);
            }
            repaint();
            dataProvider.refreshAll();
            openEditor(null, "");
        });

        getEditorLayout().removeAll();
        getEditorLayout().add(content);
    }

    private boolean hasDuplicateValue(ChartThreshold entity) {
        for (ChartThreshold existing : dataProvider.getItems()) {
            if (existing.getValue() == null || entity.getValue() == null) {
                continue;
            }
            boolean sameValue = existing.getValue().equals(entity.getValue());
            boolean sameEntity = existing.getId() != null && existing.getId().equals(entity.getId());
            if (sameValue && !sameEntity) {
                return true;
            }
        }
        return false;
    }

    private void removeThreshold(ChartThreshold entity) {
        dataProvider.getItems().remove(entity);
        repaint();
        dataProvider.refreshAll();
    }

    private void repaint() {
        List<ChartThreshold> items = new ArrayList<>(dataProvider.getItems());
        if (items.isEmpty()) {
            return;
        }

        Collections.sort(items, new GraphThresholdComparator());
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(items);

        if (feed != null && feed.getWidget().getType().equals(GraphicWidgetType.WIND_ROSE)) {
            NumberFormat nf = NumberFormat.getNumberInstance(UIUtils.getLocale());
            nf.setGroupingUsed(false);
            ListIterator<ChartThreshold> li = items.listIterator(items.size());
            String upper = "--";
            while (li.hasPrevious()) {
                ChartThreshold bean = li.previous();
                bean.setLabel(upper);
                upper = nf.format(bean.getValue());
            }
        }
    }

    public void setThresholds(List<ChartThreshold> thresholds) {
        dataProvider.getItems().clear();
        if (thresholds != null) {
            for (ChartThreshold threshold : thresholds) {
                if (threshold.getId() == null) {
                    threshold.setId(EncryptUtils.getUniqueId());
                }
                feed = threshold.getFeed();
                dataProvider.getItems().add(threshold);
            }
            repaint();
            dataProvider.refreshAll();
        }
    }

    public List<ChartThreshold> getThresholds() {
        List<ChartThreshold> thresholds = new ArrayList<>();
        for (ChartThreshold item : dataProvider.getItems()) {
            if (item.isNew()) {
                item.setId(null);
            }
            item.setFeed(feed);
            item.setOwner(feed.getOwner());
            thresholds.add(item);
        }
        Collections.sort(thresholds, new GraphThresholdComparator());
        return thresholds;
    }

    private Grid<ChartThreshold> createGrid() {
        dataProvider = new ListDataProvider<>(new ArrayList<>());
        Grid<ChartThreshold> table = new Grid<>();
        table.setDataProvider(dataProvider);
        table.setSelectionMode(Grid.SelectionMode.SINGLE);
        table.setWidthFull();
        table.addClassName("smallgrid");

        valueColumn = table.addColumn(ChartThreshold::getValue)
                .setKey("value")
                .setHeader(getI18nLabel("value"))
                .setFlexGrow(1);
        labelColumn = table.addColumn(ChartThreshold::getLabel)
                .setKey("label")
                .setHeader(getI18nLabel("label"))
                .setFlexGrow(1);
        table.addComponentColumn(this::buildColorCell)
                .setKey("fillColor")
                .setHeader(getI18nLabel("fillColor"))
                .setFlexGrow(1);

        return table;
    }

    private Component buildColorCell(ChartThreshold threshold) {
        Button button = new Button();
        button.setWidthFull();
        button.getStyle().set("background-color", threshold.getFillColor());
        button.getStyle().set("min-height", "2rem");
        button.addThemeName("tertiary-inline");
        button.addClickListener(event -> grid.select(threshold));

        Div wrapper = new Div(button);
        wrapper.setWidthFull();
        return wrapper;
    }

    public void setFeed(GraphicFeed feed) {
        this.feed = feed;
        switch (feed.getWidget().getType()) {
        case WIND_ROSE:
            valueColumn.setHeader(getI18nLabel("lower_value"));
            labelColumn.setHeader(getI18nLabel("upper_value"));
            break;
        default:
            valueColumn.setHeader(getI18nLabel("value"));
            labelColumn.setHeader(getI18nLabel("label"));
            break;
        }
        setThresholds(feed.getThresholds());
        setId(feed.getKey());
        openEditor(null, "");
    }

    public GraphicFeed getFeed() {
        return feed;
    }

    class GraphThresholdComparator implements Comparator<ChartThreshold> {
        @Override
        public int compare(ChartThreshold a, ChartThreshold b) {
            return a.getValue() < b.getValue() ? -1 : a.getValue().equals(b.getValue()) ? 0 : 1;
        }
    }
}
