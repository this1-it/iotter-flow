package it.thisone.iotter.ui.graphicwidgets;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;

import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.designer.IPlaceHolder;
import it.thisone.iotter.ui.designer.PlaceHolderChangedEvent;
import it.thisone.iotter.ui.designer.PlaceHolderChangedListener;
import it.thisone.iotter.ui.designer.PlaceHolderRemovedEvent;
import it.thisone.iotter.ui.designer.PlaceHolderRemovedListener;
import it.thisone.iotter.ui.designer.PlaceHolderSavedEvent;
import it.thisone.iotter.ui.designer.PlaceHolderSavedListener;
import it.thisone.iotter.ui.eventbus.GraphWidgetParamsEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.ifc.IGraphicWidgetEditor;
import it.thisone.iotter.util.PopupNotification;

public abstract class AbstractWidgetPlaceHolder extends BaseComponent implements IPlaceHolder {

    private static final long serialVersionUID = 1L;
    protected GraphicWidget entity;
    protected Span placeholderLabel;

    public AbstractWidgetPlaceHolder(GraphicWidget entity) {
        super("graphwidget.editor", String.valueOf(entity.getId()));
        this.entity = entity;
    }

    public void openEditor(final GraphicWidget widget, boolean create) {
        AbstractBaseEntityForm<GraphicWidget> content = GraphicWidgetFactory.createWidgetEditor(widget);
        if (content == null) {
            PopupNotification.show("Unable to create widget editor", PopupNotification.Type.ERROR);
            return;
        }

        String caption = create ? getI18nLabel("dialog_create") : getI18nLabel("dialog");
        Dialog dialog = createDialog(caption, content);
        content.setSavedHandler(saved -> {
            if (saved != null && getWidget().getId().equals(widget.getId())) {
                getPlaceholderLabel().setText(getWidgetName(widget));
            }
            if (saved != null) {
                fireEvent(new PlaceHolderSavedEvent(AbstractWidgetPlaceHolder.this));
            }
            dialog.close();
        });
        dialog.open();

        if (content instanceof IGraphicWidgetEditor && create && ((IGraphicWidgetEditor) content).getMaxParameters() > 0) {
            UIEventBus eventBus = resolveUiEventBus();
            if (eventBus != null) {
                eventBus.post(new GraphWidgetParamsEvent(widget.getId()));
            }
        }
    }

    protected UIEventBus resolveUiEventBus() {
        return getUI()
                .map(ui -> ui.getSession() != null ? ui.getSession().getAttribute(UIEventBus.class) : null)
                .orElse(null);
    }

    public GraphicWidget getWidget() {
        return entity;
    }

    public Span getPlaceholderLabel() {
        return placeholderLabel;
    }

    public static String getWidgetName(GraphicWidget widget) {
        String type = widget.getType() != null ? widget.getType().name() : "WIDGET";
        String provider = widget.getProvider() != null ? widget.getProvider() : "";
        String label = widget.getLabel() != null ? widget.getLabel() : widget.getId();
        return String.format("%s %s: %s", type, provider, label).trim();
    }

    @Override
    public void addListener(PlaceHolderSavedListener listener) {
        addListener(PlaceHolderSavedEvent.class, listener);
    }

    @Override
    public void addListener(PlaceHolderRemovedListener listener) {
        addListener(PlaceHolderRemovedEvent.class, listener);
    }

    @Override
    public void addListener(PlaceHolderChangedListener listener) {
        addListener(PlaceHolderChangedEvent.class, listener);
    }

    @Override
    public String getIdentifier() {
        return entity.getId();
    }

    @Override
    public int getX() {
        return (int) entity.getX();
    }

    @Override
    public int getY() {
        return (int) entity.getY();
    }

    @Override
    public int getPixelWidth() {
        return (int) entity.getWidth();
    }

    @Override
    public int getPixelHeight() {
        return (int) entity.getHeight();
    }

    @Override
    public void setX(int value) {
        entity.setX(value);
    }

    @Override
    public void setY(int value) {
        entity.setY(value);
    }

    @Override
    public void setPixelWidth(int value) {
        entity.setWidth(value);
    }

    @Override
    public void setPixelHeight(int value) {
        entity.setHeight(value);
    }
}
