package it.thisone.iotter.ui.groupwidgets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.NetworkGroupType;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.fields.NetworkGroupSingleSelect;
import it.thisone.iotter.ui.common.fields.NetworkSelect;

public class GroupWidgetForm extends AbstractBaseEntityForm<GroupWidget> {

    public static final String GROUP_PROPERTY = "group";
    private static final long serialVersionUID = 1L;

    private final NetworkService networkService;
    private final NetworkGroupService networkGroupService;

    @PropertyId("name")
    private TextField name;

    @PropertyId("options.realTime")
    private Checkbox realTime;

    private NetworkSelect networkSelect;
    private NetworkGroupSingleSelect groupSelect;
    private final Network defaultNetwork;

    public GroupWidgetForm(GroupWidget entity, Network network, UserDetailsAdapter currentUser,
            NetworkService networkService, NetworkGroupService networkGroupService, boolean readOnly) {
        super(entity, GroupWidget.class, "groupwidget.editor", network, currentUser, readOnly);
        this.defaultNetwork = network;
        this.networkService = networkService;
        this.networkGroupService = networkGroupService;

        bindFields();
        getBinder().readBean(entity);
        configureNetworkAndGroup(entity);
    }

    @Override
    protected void initializeFields() {
        name = new TextField(getI18nLabel("name"));
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);
        name.setReadOnly(isReadOnly());

        realTime = new Checkbox(getI18nLabel("realTime"));
        realTime.setWidthFull();
        realTime.setReadOnly(isReadOnly());

        networkSelect = new NetworkSelect(loadNetworks());
        networkSelect.setWidthFull();
        networkSelect.setLabel(getI18nLabel("network"));
        networkSelect.setReadOnly(isReadOnly());

        groupSelect = new NetworkGroupSingleSelect(loadGroups());
        groupSelect.setWidthFull();

        groupSelect.setEnabled(!isReadOnly());
    }

    private List<Network> loadNetworks() {
        if (getCurrentUser() == null) {
            return Arrays.asList();
        }
        return networkService.findByOwner(getCurrentUser().getTenant());
    }

    private List<NetworkGroup> loadGroups() {
        if (getCurrentUser() == null) {
            return Arrays.asList();
        }
        return networkGroupService.findByOwner(getCurrentUser().getTenant());
    }

    @Override
    protected void bindFields() {
        getBinder().forField(name)
                .asRequired(getTranslation("validators.fieldgroup_errors"))
                .withValidator(new StringLengthValidator(getTranslation("validators.fieldgroup_errors"), 1, 255))
                .bind(GroupWidget::getName, GroupWidget::setName);

        getBinder().forField(realTime)
                .bind(widget -> widget.getOptions().isRealTime(),
                        (widget, value) -> widget.getOptions().setRealTime(Boolean.TRUE.equals(value)));
    }

    private void configureNetworkAndGroup(GroupWidget entity) {
        groupSelect.setVisible(Constants.USE_GROUPS && isCreateBean());
        networkSelect.setVisible(Constants.USE_GROUPS && isCreateBean());

        if (defaultNetwork != null) {
            networkSelect.setValue(defaultNetwork);
            networkSelect.setReadOnly(true);
            filterGroups(defaultNetwork);
        }

        networkSelect.addValueChangeListener(event -> filterGroups(event.getValue()));

        if (isCreateBean() && defaultNetwork != null) {
            preselectDefaultGroup(defaultNetwork);
        } else if (!isCreateBean() && entity.getGroup() != null) {
            groupSelect.select(entity.getGroup());
        }
    }

    private void filterGroups(Network network) {
        if (network == null) {
            groupSelect.deselectAll();
            groupSelect.setItems(Arrays.asList());
            return;
        }
        List<NetworkGroup> groups = networkGroupService.findByNetwork(network);
        groupSelect.setItems(groups);
        preselectDefaultGroup(network);
    }

    private void preselectDefaultGroup(Network network) {
        List<NetworkGroup> groups = networkGroupService.findByNetwork(network);
        Optional<NetworkGroup> defaultGroup = groups.stream().filter(NetworkGroup::isDefaultGroup).findFirst();
        defaultGroup.ifPresent(groupSelect::select);
    }

    @Override
    public VerticalLayout getFieldsLayout() {
        VerticalLayout mainLayout = buildMainLayout();
        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.add(name, realTime);
        if (Constants.USE_GROUPS && isCreateBean()) {
            form.add(networkSelect, groupSelect);
        }
        mainLayout.add(form);
        return mainLayout;
    }

    @Override
    protected void afterCommit() {
        if (isCreateBean() && groupSelect.getSelectedItems() != null && !groupSelect.getSelectedItems().isEmpty()) {
            NetworkGroup selectedGroup = groupSelect.getSelectedItems().iterator().next();
            getEntity().setGroup(selectedGroup);
        }
        if (isCreateBean() && getEntity().getCreator() == null && getCurrentUser() != null) {
            getEntity().setCreator(getCurrentUser().getUsername());
        }
        if (isCreateBean() && getEntity().getGroup() == null && groupSelect.getSelectedItems() != null) {
            groupSelect.getSelectedItems().stream().findFirst().ifPresent(group -> {
                getEntity().setGroup(group);
                group.setGroupType(NetworkGroupType.GROUP_WIDGET);
                group.setExclusive(true);
            });
        }
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
    }
}
