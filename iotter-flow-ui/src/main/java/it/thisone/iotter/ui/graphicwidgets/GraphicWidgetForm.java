package it.thisone.iotter.ui.graphicwidgets;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.ifc.IGraphicWidgetEditor;

public class GraphicWidgetForm extends AbstractBaseEntityForm<GraphicWidget> implements IGraphicWidgetEditor {

    private static final long serialVersionUID = 1L;

    private TextField label;
    private TextField provider;
    private TextField url;
    private TextArea description;

    public GraphicWidgetForm(GraphicWidget entity) {
        super(entity, GraphicWidget.class, "graphwidget.editor", null, null, false);
        bindFields();
        getBinder().readBean(entity);
    }

    @Override
    protected void initializeFields() {
        label = new TextField(getI18nLabel("label"));
        label.setWidthFull();

        provider = new TextField(getI18nLabel("provider"));
        provider.setWidthFull();

        url = new TextField(getI18nLabel("url"));
        url.setWidthFull();

        description = new TextArea(getI18nLabel("description"));
        description.setWidthFull();
    }

    @Override
    protected void bindFields() {
        getBinder().forField(label).bind(GraphicWidget::getLabel, GraphicWidget::setLabel);
        getBinder().forField(provider).bind(GraphicWidget::getProvider, GraphicWidget::setProvider);
        getBinder().forField(url).bind(GraphicWidget::getUrl, GraphicWidget::setUrl);
        getBinder().forField(description).bind(GraphicWidget::getDescription, GraphicWidget::setDescription);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        initializeFields();
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.add(label, provider, url, description);
        mainLayout.add(form);
        return mainLayout;
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }

    @Override
    protected void afterCommit() {
    }

    @Override
    public int getMaxParameters() {
        return GraphicWidgetFactory.maxParameters(getEntity());
    }
}
