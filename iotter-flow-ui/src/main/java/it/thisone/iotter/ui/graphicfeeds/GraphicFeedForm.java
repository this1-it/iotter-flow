package it.thisone.iotter.ui.graphicfeeds;

import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChartPlotOptions;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.CustomMarkerSymbolEnum;
import it.thisone.iotter.ui.common.fields.ChannelAcceptor;
import it.thisone.iotter.ui.common.fields.ChannelSelect;
import it.thisone.iotter.ui.common.fields.ChartPlotOptionsField;

public class GraphicFeedForm extends AbstractBaseEntityForm<GraphicFeed> {

    private static final long serialVersionUID = 1L;

    private TextField label;
    private ChannelSelect channel;
    private ChartPlotOptionsField options;
    private ChartThresholdsField thresholds;
    private boolean fieldsInitialized;

    public GraphicFeedForm(GraphicFeed entity) {
        this(entity, false);
    }

    public GraphicFeedForm(GraphicFeed entity, boolean readOnly) {
        super(entity, GraphicFeed.class, "graphfeed.editor", null, null, readOnly);
        ensureFieldsInitialized(entity);
        bindFields();
        getBinder().readBean(entity);
        applySelectionState(entity);
    }

    @Override
    protected void initializeFields() {
        ensureFieldsInitialized(getEntity());
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        ensureFieldsInitialized(getEntity());
        GraphicWidgetType type = getWidgetType(getEntity());

        TabSheet multicomponent = new TabSheet();
        multicomponent.setSizeFull();

        VerticalLayout general = new VerticalLayout(channel, label);
        general.setPadding(true);
        general.setSpacing(true);

        VerticalLayout grid = new VerticalLayout(general, options);
        grid.setPadding(true);
        grid.setSpacing(true);

        VerticalLayout mainLayout = buildMainLayout();
        mainLayout.add(multicomponent);
        mainLayout.setFlexGrow(1f, multicomponent);

        multicomponent.addTab(getI18nLabel("general_tab"), buildPanel(grid));

        label.setVisible(false);
        options.setVisible(true);

        switch (type) {
        case EMBEDDED:
        case LAST_MEASURE_TABLE:
        case LAST_MEASURE:
            label.setVisible(true);
            break;
        case TABLE:
            options.setVisible(false);
            break;
        case CUSTOM:
        case MULTI_TRACE:
            thresholds.setFeed(getEntity());
            multicomponent.addTab(getI18nLabel("thresholds_tab"), thresholds);
            break;
        case WIND_ROSE:
            if (ChannelAcceptor.isSpeed(getEntity().getChannel())) {
                thresholds.setFeed(getEntity());
                multicomponent.addTab(getI18nLabel("categories_tab"), thresholds);
            }
            break;
        default:
            break;
        }

        return mainLayout;
    }

    private void ensureFieldsInitialized(GraphicFeed entity) {
        if (fieldsInitialized) {
            return;
        }

        label = new TextField(getI18nLabel("label"));
        label.setWidthFull();

        channel = new ChannelSelect();
        channel.setWidthFull();

        options = new ChartPlotOptionsField();
        options.setGraphWidgetType(getWidgetType(entity));
        options.buildContent();
        options.setWidthFull();

        thresholds = new ChartThresholdsField();
        thresholds.setWidthFull();

        addField("channel", channel);
        addField("label", label);
        addField("options", options);
        addField("thresholds", thresholds);

        channel.addValueChangeListener(event -> {
            Channel selected = event.getValue();
            if (selected != null) {
                label.setValue(selected.toString());
                options.setChannel(selected);
            }
        });

        fieldsInitialized = true;
    }

    @Override
    protected void bindFields() {
        Binder<GraphicFeed> binder = getBinder();
        binder.forField(channel).bind(GraphicFeed::getChannel, GraphicFeed::setChannel);
        binder.forField(label).bind(GraphicFeed::getLabel, GraphicFeed::setLabel);
        binder.forField(options).bind(GraphicFeed::getOptions, GraphicFeed::setOptions);
        binder.forField(thresholds).bind(GraphicFeed::getThresholds, GraphicFeed::setThresholds);
    }

    private GraphicWidgetType getWidgetType(GraphicFeed entity) {
        if (entity != null && entity.getWidget() != null && entity.getWidget().getType() != null) {
            return entity.getWidget().getType();
        }
        return GraphicWidgetType.MULTI_TRACE;
    }

    private void applySelectionState(GraphicFeed entity) {
        if (entity == null) {
            return;
        }
        options.setChannel(entity.getChannel());
        MeasureUnit measure = entity.getMeasure();
        if (measure != null) {
            channel.setMeasure(measure);
        }
    }

    @Override
    protected void afterCommit() {
        MeasureUnit measure = channel.getMeasure();
        getEntity().setMeasure(measure);

        ChartPlotOptions optionsValue = options.getValue();
        if (optionsValue != null) {
            if (getWidgetType(getEntity()) == GraphicWidgetType.WIND_ROSE) {
                optionsValue.setMarkerSymbol(CustomMarkerSymbolEnum.ARROW.name());
            }
            getEntity().setOptions(optionsValue);
        }
        getEntity().setThresholds(thresholds.getValue());
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }

    @Override
    public String getWindowStyle() {
        return "graphfeed-editor";
    }

    @Override
    public float[] getWindowDimension() {
        return UIUtils.L_DIMENSION;
    }
}
