package it.thisone.iotter.ui.groupwidgets;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.exceptions.ApplicationRuntimeException;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.BaseEditor;
import it.thisone.iotter.ui.common.EditorSelectedEvent;
import it.thisone.iotter.ui.common.EditorSelectedListener;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.designer.PlaceHolderRemovedEvent;
import it.thisone.iotter.ui.designer.PlaceHolderRemovedListener;
import it.thisone.iotter.ui.designer.PlaceHolderSavedEvent;
import it.thisone.iotter.ui.designer.PlaceHolderSavedListener;
import it.thisone.iotter.ui.eventbus.PendingChangesEvent;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.graphicwidgets.GraphicWidgetPlaceHolder;
import it.thisone.iotter.ui.gridstack.GridstackBoard;
import it.thisone.iotter.ui.gridstack.GridstackLayoutUtils;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.PopupNotification;

public class GroupWidgetDesigner extends BaseEditor<GroupWidget> {

    private static final long serialVersionUID = -9049540364011024970L;
    private static final Logger logger = LoggerFactory.getLogger(GroupWidgetDesigner.class);

    private final GroupWidgetService groupWidgetService;
    private final NetworkService networkService;
    private final NetworkGroupService networkGroupService;
    private final UserDetailsAdapter currentUser;
    private final UIEventBus uiEventBus;

    private GroupWidget entity;
    private GridstackBoard gridstackBoard;
    private final List<GraphicWidget> addedWidgets;
    private final List<String> removedWidgets;
    private VerticalLayout mainLayout;
    private Button saveButton;

    public GroupWidgetDesigner(GroupWidget sourceEntity,
            GroupWidgetService groupWidgetService,
            NetworkService networkService,
            NetworkGroupService networkGroupService,
            UserDetailsAdapter currentUser) {
        super("groupwidget.designer", sourceEntity != null && sourceEntity.getId() != null ? sourceEntity.getId() : "");

        this.groupWidgetService = groupWidgetService;
        this.networkService = networkService;
        this.networkGroupService = networkGroupService;
        this.currentUser = currentUser;
        this.uiEventBus = resolveUiEventBus();
        this.addedWidgets = new ArrayList<>();
        this.removedWidgets = new ArrayList<>();

        setEventPoster(event -> {
            if (uiEventBus != null) {
                uiEventBus.post(event);
            }
        });

        this.entity = resolveEntity(sourceEntity);
        if (this.entity == null) {
            String msg = "GroupWidget not found";
            logger.error(msg);
            throw new ApplicationRuntimeException(msg);
        }

        // Force viewport-sized designer content so dialog overlay can expand
        // without shrink-wrapping to inner content width.
        getElement().getStyle().set("width", "100vw");
        getElement().getStyle().set("height", "100vh");

        gridstackBoard = new GridstackBoard();
        gridstackBoard.setWidthFull();
        gridstackBoard.setEditable(true);
        gridstackBoard.addLayoutChangedListener(event -> {
            entity.setLayout(event.getLayout());
            if (uiEventBus != null) {
                uiEventBus.post(new PendingChangesEvent());
            }
        });

        buildLayout();
        migrateLayoutIfNeeded();
        loadWidgetsToGrid();
    }

    private GroupWidget resolveEntity(GroupWidget sourceEntity) {
        if (sourceEntity == null) {
            return null;
        }
        if (sourceEntity.getId() == null) {
            return sourceEntity;
        }
        GroupWidget managed = groupWidgetService.findOne(sourceEntity.getId());
        return managed != null ? managed : sourceEntity;
    }

    private UIEventBus resolveUiEventBus() {
        UI ui = UI.getCurrent();
        if (ui == null || ui.getSession() == null) {
            return null;
        }
        return ui.getSession().getAttribute(UIEventBus.class);
    }

    private void buildLayout() {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setWidth("100vw");
        mainLayout.setHeight("100vh");
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setHeight(TOOLBAR_HEIGHT + "px");
        toolbar.setPadding(true);
        toolbar.setSpacing(true);
        toolbar.addClassName(TOOLBAR_STYLE);

        HorizontalLayout buttonbar = new HorizontalLayout();
        buttonbar.setSpacing(true);
        buttonbar.addClassName(BUTTONS_STYLE);
        buttonbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        Button modifyButton = createModifyButton();
        buttonbar.add(modifyButton);

        saveButton = createSaveButton();
        buttonbar.add(saveButton);

        Button cancelButton = createCancelButton();
        buttonbar.add(cancelButton);

        Button addButton = createAddButton();
        buttonbar.add(addButton);

        toolbar.add(buttonbar);
        toolbar.setVerticalComponentAlignment(Alignment.CENTER, buttonbar);

        mainLayout.add(toolbar);
        mainLayout.add(gridstackBoard);
        mainLayout.setFlexGrow(1f, gridstackBoard);

        setRootComposition(mainLayout);
    }

    private void migrateLayoutIfNeeded() {
        if (entity.getLayout() == null && !entity.getWidgets().isEmpty()) {
            String migratedJson = GridstackLayoutUtils.convertLegacyToGridJson(entity.getWidgets());
            entity.setLayout(migratedJson);
            logger.info("Migrated legacy layout to Gridstack JSON for GroupWidget {}", entity.getId());
        }
    }

    private void loadWidgetsToGrid() {
        // for (GraphicWidget widget : entity.getWidgets()) {
        //     if (widget.getParent() == null) {
        //         List<GraphicWidget> children = widget.findChildren(entity.getWidgets());
        //         GraphicWidgetPlaceHolder placeHolder = new GraphicWidgetPlaceHolder(widget, children);
        //         placeHolderListeners(placeHolder);
        //         int[] defaultSize = GridstackLayoutUtils.getDefaultGridSize(widget.getType());
        //         gridstackBoard.addWidget(widget.getId(), placeHolder, 0, 0, defaultSize[0], defaultSize[1]);
        //     }
        // }
        if (entity.getLayout() != null) {
            gridstackBoard.setLayout(entity.getLayout());
        }
    }

    private Button createAddButton() {
        Button button = new Button(VaadinIcon.PLUS.create());
        button.getElement().setProperty("title", getI18nLabel("add_graph"));
        button.setId("add_graph");
        button.addClickListener(event -> openChoice());
        return button;
    }

    private Button createModifyButton() {
        Button button = new Button(VaadinIcon.EDIT.create());
        button.getElement().setProperty("title", getI18nLabel("modify_action"));
        button.addClickListener(event -> openEditor(entity, getI18nLabel("modify_dialog")));
        return button;
    }

    public void openEditor(GroupWidget groupWidget, String label) {
        GroupWidgetForm content = new GroupWidgetForm(groupWidget, null, currentUser, networkService, networkGroupService, false);
        Dialog dialog = createDialog(label, content);
        content.setSavedHandler(saved -> dialog.close());
        dialog.open();
    }

    @SuppressWarnings("serial")
    private void placeHolderListeners(GraphicWidgetPlaceHolder placeHolder) {
        placeHolder.addListener(new PlaceHolderRemovedListener() {
            @Override
            public void placeHolderRemoved(PlaceHolderRemovedEvent event) {
                if (event.getSource() instanceof GraphicWidgetPlaceHolder) {
                    removePlaceHolder((GraphicWidgetPlaceHolder) event.getSource());
                }
            }
        });

        placeHolder.addListener(new PlaceHolderSavedListener() {
            @Override
            public void placeHolderSaved(PlaceHolderSavedEvent event) {
                if (event.getSource() instanceof GraphicWidgetPlaceHolder) {
                    managePlaceHolder((GraphicWidgetPlaceHolder) event.getSource());
                }
            }
        });
    }

    private void removePlaceHolder(GraphicWidgetPlaceHolder placeHolder) {
        String id = placeHolder.getIdentifier();
        boolean found = false;
        for (GraphicWidget widget : addedWidgets) {
            if (widget.getId().equals(id)) {
                found = true;
                List<GraphicWidget> children = widget.findChildren(addedWidgets);
                addedWidgets.remove(widget);
                addedWidgets.removeAll(children);
                break;
            }
        }
        if (!found) {
            for (GraphicWidget widget : entity.getWidgets()) {
                if (widget.getId().equals(id)) {
                    List<GraphicWidget> children = widget.findChildren(entity.getWidgets());
                    for (GraphicWidget child : children) {
                        removedWidgets.add(child.getId().toString());
                    }
                    break;
                }
            }
            removedWidgets.add(id);
        }
        gridstackBoard.removeWidget(id);
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected void onSave() {
        entity.removeGraphWidgets(removedWidgets);
        entity.addGraphWidgets(addedWidgets);

        for (GraphicWidget widget : entity.getWidgets()) {
            for (GraphicFeed feed : widget.getFeeds()) {
                if (feed.isNew()) {
                    feed.setId(null);
                }
            }
        }

        try {
            groupWidgetService.update(entity);
        } catch (BackendServiceException e) {
            PopupNotification.show(AN_EDIT_CONFLICT_OCCURRED, PopupNotification.Type.ERROR);
        }
    }

    public VerticalLayout getMainLayout() {
        return mainLayout;
    }

    public void switchLayout(VerticalLayout compositionRoot) {
        setRootComposition(compositionRoot);
    }

    public List<GraphicWidget> getAddedWidgets() {
        return addedWidgets;
    }

    public List<String> getRemovedWidgets() {
        return removedWidgets;
    }

    public void managePlaceHolder(GraphicWidgetPlaceHolder placeHolder) {
        GraphicWidget widget = placeHolder.getWidget();
        boolean alreadyInGrid = false;
        for (GraphicWidget existing : entity.getWidgets()) {
            if (existing.getId().equals(widget.getId())) {
                alreadyInGrid = true;
                break;
            }
        }
        for (GraphicWidget added : addedWidgets) {
            if (added.getId().equals(widget.getId())) {
                alreadyInGrid = true;
                break;
            }
        }
        if (!alreadyInGrid) {
            addedWidgets.add(widget);
            addedWidgets.addAll(placeHolder.getWidgetChildren());
            int[] defaultSize = GridstackLayoutUtils.getDefaultGridSize(widget.getType());
            gridstackBoard.addWidget(widget.getId(), placeHolder, 0, 0, defaultSize[0], defaultSize[1]);
        }
    }

    public void createPlaceHolder(GraphicWidgetType type, String provider) {
        GraphicWidget widget = new GraphicWidget();
        widget.setId(EncryptUtils.getUniqueId());
        widget.setType(type);
        widget.setProvider(provider);
        widget.getOptions().setFillColor(ChartUtils.quiteRandomHexColor());
        widget.setGroupWidget(entity);

        List<GraphicWidget> children = new ArrayList<>();
        GraphicWidgetPlaceHolder placeHolder = new GraphicWidgetPlaceHolder(widget, children);
        placeHolderListeners(placeHolder);
        placeHolder.openEditor(placeHolder.getWidget(), true);
    }

    @SuppressWarnings("unchecked")
    public void openChoice() {
        GroupWidgetChoice content = new GroupWidgetChoice(new GroupWidgetUiFactory());
        Dialog dialog = createDialog(getI18nLabel("add_graph"), content);
        content.addListener(new EditorSelectedListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void editorSelected(EditorSelectedEvent event) {
                dialog.close();
                if (event.getSelected() != null) {
                    GraphicWidget item = (GraphicWidget) event.getSelected().iterator().next();
                    createPlaceHolder(item.getType(), item.getProvider());
                }
            }
        });
        dialog.open();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (uiEventBus != null) {
            uiEventBus.register(this);
        }
        if (entity.getWidgets().isEmpty()) {
            openChoice();
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
        if (!isPendingChanges()) {
            setPendingChanges(true);
            if (saveButton != null) {
                saveButton.addClassName("pending-changes");
            }
        }
    }
}
