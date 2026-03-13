package it.thisone.iotter.ui.graphicwidgets;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractWidgetVisualizer;

public class UnsupportedWidgetVisualizer extends AbstractWidgetVisualizer {

    private static final long serialVersionUID = 1L;

    private final Div root;
    private final Span message;

    public UnsupportedWidgetVisualizer(GraphicWidget widget) {
        super(widget);
        root = new Div();
        root.addClassName("unsupported-widget-visualizer");
        message = new Span("Widget visualizer not migrated: " + (widget.getType() != null ? widget.getType().name() : ""));
        root.add(message);
        setRootComposition(root);
    }

    @Override
    protected Component buildVisualization() {
        return root;
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void draw() {
    }

}
