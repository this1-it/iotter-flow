package it.thisone.iotter.ui.groupwidgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.ui.common.EditorSelectedEvent;
import it.thisone.iotter.ui.common.WidgetTypeInstance;
import it.thisone.iotter.ui.ifc.IGroupWidgetUiFactory;

public class GroupWidgetChoice extends Composite<VerticalLayout> {

    private static final long serialVersionUID = 1L;

    private final IGroupWidgetUiFactory config;

    public GroupWidgetChoice(IGroupWidgetUiFactory config) {
        this.config = config;
        buildLayout();
    }

    private void buildLayout() {
        getContent().setWidthFull();
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        Collection<WidgetTypeInstance> container = config.getWidgetTypes();
        for (WidgetTypeInstance item : container) {
            getContent().add(createContentWrapper(item.getType(), item.getProvider(), item));
        }
    }

    private Button createContentWrapper(GraphicWidgetType type, String provider, WidgetTypeInstance item) {
        String label = getTranslation(type.getI18nKey());
        if (provider != null) {
            label = getTranslation("provider." + provider.toLowerCase());
        }

        Button button = new Button(label);
        if (item.getIcon() != null) {
            button.setIcon(item.getIcon());
        }
        button.addClassName(type.name().toLowerCase() + "-icon");
        button.setWidthFull();

        GraphicWidget widget = new GraphicWidget();
        widget.setType(type);
        widget.setProvider(provider);

        button.addClickListener(event -> notifySelected(widget));
        return button;
    }

    private void notifySelected(GraphicWidget widget) {
        fireEvent(new EditorSelectedEvent<>(this, widget));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addEditorSelectedListener(ComponentEventListener<EditorSelectedEvent<?>> listener) {
        return addListener(EditorSelectedEvent.class, (ComponentEventListener) listener);
    }

    public String getWindowStyle() {
        return "groupwidget-choice";
    }

    public float[] getWindowDimension() {
        return new float[] { 0.8f, 0.8f };
    }
}
