package it.thisone.iotter.ui.graphicwidgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vaadin.flow.components.TabSheet;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.channels.ChannelGrid;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.designer.PlaceHolderChangedEvent;
import it.thisone.iotter.ui.designer.PlaceHolderRemovedEvent;
import it.thisone.iotter.ui.eventbus.PendingChangesEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;

public class GraphicWidgetPlaceHolder extends AbstractWidgetPlaceHolder {

    private static final long serialVersionUID = 668757940014666398L;

    private final List<GraphicWidget> children;
    private final VerticalLayout mainLayout = new VerticalLayout();
    private final Span embeddedInfo = new Span();

    private UIEventBus uiEventBus;

    public GraphicWidgetPlaceHolder(GraphicWidget widget, List<GraphicWidget> children) {
        super(widget);
        widget.removeOrphanFeeds();
        this.children = children != null ? children : new ArrayList<>();
        buildLayout();
    }

    private void buildLayout() {
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.setDefaultHorizontalComponentAlignment(Alignment.START);

        Button controlsButton = new Button(VaadinIcon.COG.create());
        controlsButton.getElement().setProperty("title", getI18nLabel("configure"));
        com.vaadin.flow.component.contextmenu.ContextMenu menu = new com.vaadin.flow.component.contextmenu.ContextMenu(controlsButton);
        menu.setOpenOnClick(true);
        menu.addItem(getI18nLabel("configure"), e -> openEditor(entity, false));
        menu.addItem(getI18nLabel("info"), e -> openInfo());
        menu.addItem(getI18nLabel("remove"), e -> openRemove());
        for (GraphicWidget child : children) {
            String childName = child.getType() != null ? child.getType().name() : "CHILD";
            MenuItem childMenu = menu.addItem(getI18nLabel("configure") + " " + childName, e -> openEditor(child, false));
            childMenu.getElement().setProperty("data-child-id", child.getId());
        }

        placeholderLabel = new Span(getWidgetName(entity));
        HorizontalLayout header = new HorizontalLayout(controlsButton, placeholderLabel);
        header.setSpacing(true);
        header.setAlignItems(Alignment.CENTER);
        mainLayout.add(header);

        if (GraphicWidgetType.EMBEDDED.equals(widgetType())) {
            embeddedInfo.setText(getI18nLabel("no_info"));
            mainLayout.add(embeddedInfo);
        }

        mainLayout.addClassName(getStyleNameFromType());
        setId(entity.toString());
        setRootComposition(mainLayout);
    }

    private GraphicWidgetType widgetType() {
        return getWidget() != null ? getWidget().getType() : null;
    }

    public String getStyleNameFromType() {
        GraphicWidgetType type = widgetType();
        return type != null ? type.name().toLowerCase() + "-placeholder" : "widget-placeholder";
    }

    public void setChildren(List<GraphicWidget> updatedChildren) {
        children.clear();
        if (updatedChildren != null) {
            children.addAll(updatedChildren);
        }
    }

    public void openInfo() {
        com.vaadin.flow.component.Component content = new Span(getI18nLabel("no_info"));
        Collection<Channel> channels = new ArrayList<>();
        if (!entity.getFeeds().isEmpty()) {
            for (GraphicFeed feed : entity.getFeeds()) {
                if (feed.getChannel() != null) {
                    channels.add(feed.getChannel());
                }
            }
            for (GraphicWidget child : children) {
                for (GraphicFeed feed : child.getFeeds()) {
                    if (feed.getChannel() != null) {
                        channels.add(feed.getChannel());
                    }
                }
            }
            ChannelGrid channelTable = new ChannelGrid(channels);
            TabSheet tabs = new TabSheet();
            tabs.addTab(getI18nLabel("device_params"), channelTable);
            content = tabs;
        }
        Dialog dialog = createDialog(entity.getLabel(), content);
        dialog.open();
    }

    public void openRemove() {
        Callback callback = result -> {
            if (result) {
                if (uiEventBus != null) {
                    uiEventBus.post(new PendingChangesEvent());
                }
                fireEvent(new PlaceHolderRemovedEvent(GraphicWidgetPlaceHolder.this));
            }
        };
        String caption = getTranslation("basic.editor.are_you_sure");
        String message = getI18nLabel("remove_action");
        ConfirmationDialog dialog = new ConfirmationDialog(caption, message, callback);
        dialog.open();
    }

    public void openPositionEditor(final com.vaadin.flow.component.Component source) {
        fireEvent(new PlaceHolderChangedEvent(source));
    }

    public List<GraphicWidget> getWidgetChildren() {
        return children;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        uiEventBus = resolveUiEventBus();
        if (uiEventBus != null) {
            uiEventBus.register(this);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (uiEventBus != null) {
            uiEventBus.unregister(this);
        }
        super.onDetach(detachEvent);
    }

    @Subscribe
    public void pendingChanges(PendingChangesEvent event) {
        if (GraphicWidgetType.EMBEDDED.equals(widgetType())) {
            embeddedInfo.setText(getI18nLabel("pending_changes"));
        }
    }
}
