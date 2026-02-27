package it.thisone.iotter.ui.maps;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ImageData;
import it.thisone.iotter.ui.common.BaseEditor;
import it.thisone.iotter.ui.common.EditorSavedEvent;

public class GraphWidgetEmbeddedMapEditor extends BaseEditor<GraphicWidget> {

    private static final long serialVersionUID = 1L;

    private HorizontalLayout footer;
    private VerticalLayout imageLayout;

    public GraphWidgetEmbeddedMapEditor(ImageData image, List<GraphicFeed> feeds) {
        super("groupwidgets.custommap");

        GraphicWidget entity = new GraphicWidget();
        entity.setGroupWidget(new GroupWidget());
        entity.setFeeds(feeds);
        entity.setImage(image);
        setItem(entity);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        imageLayout = new VerticalLayout();
        imageLayout.setSizeFull();

        footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setPadding(true);
        footer.setSpacing(true);
        footer.setVisible(true);

        mainLayout.add(imageLayout, footer);
        mainLayout.setFlexGrow(1f, imageLayout);

        setRootComposition(mainLayout);
        showMap(entity);
    }

    private void showMap(GraphicWidget entity) {
        if (entity == null) {
            imageLayout.setVisible(false);
            footer.setVisible(false);
            return;
        }

        GraphFeedsImageOverlayMap content = new GraphFeedsImageOverlayMap(entity, -1, -1, true);

        footer.removeAll();
        imageLayout.removeAll();
        imageLayout.add(content);

        HorizontalLayout editorLayout = new HorizontalLayout();
        editorLayout.setSpacing(true);

        Button save = createSaveButton();
        Button remove = createRemoveButton();
        Button cancel = createCancelButton();
        Button upload = content.createImageButton();

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        remove.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        editorLayout.add(save, remove, cancel, upload);
        footer.add(editorLayout);

        footer.setVisible(true);
        imageLayout.setVisible(true);
    }

    @Override
    protected void onSave() {
    }

    protected Button createRemoveButton() {
        Button button = new Button();
        button.setIcon(VaadinIcon.TRASH.create());
        button.getElement().setProperty("title", getI18nLabel("remove_button"));
        button.addClickListener(event -> {
            getItem().setImage(null);
            fireEvent(new EditorSavedEvent<>(GraphWidgetEmbeddedMapEditor.this, getItem()));
        });
        return button;
    }

    public float[] getWindowDimension() {
        return XL_DIMENSION;
    }

    public String getWindowStyle() {
        return null;
    }
}
