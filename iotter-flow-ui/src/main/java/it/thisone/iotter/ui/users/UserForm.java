package it.thisone.iotter.ui.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.NetworkGroupType;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.RoleService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.AbstractBaseEntityForm;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.ui.common.fields.AccountStatusSelect;
import it.thisone.iotter.ui.common.fields.CountrySelect;
import it.thisone.iotter.ui.common.fields.NetworkGroupSelect;
import it.thisone.iotter.ui.common.fields.NetworkSelect;
import it.thisone.iotter.ui.common.fields.RoleSelect;
import it.thisone.iotter.ui.eventbus.UIEventBus;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetAdapterListing;
import it.thisone.iotter.ui.validators.AntiReDoSEmailValidator;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserForm extends AbstractBaseEntityForm<User> {

    private static final long serialVersionUID = 1L;


    private final RoleService roleService;
    private final NetworkService networkService;
    private final NetworkGroupService networkGroupService;
    private final GroupWidgetService groupWidgetService;

    @PropertyId("username")
    private TextField username;

    @PropertyId("accountStatus")
    private AccountStatusSelect accountStatus;

    private PasswordField originalPassword;
    private PasswordField verifiedPassword;

    @PropertyId("email")
    private TextField email;

    @PropertyId("firstName")
    private TextField firstName;

    @PropertyId("lastName")
    private TextField lastName;

    @PropertyId("company")
    private TextField company;

    @PropertyId("phone")
    private TextField phone;

    @PropertyId("street")
    private TextField street;

    @PropertyId("city")
    private TextField city;

    @PropertyId("zip")
    private TextField zip;

    @PropertyId("country")
    private CountrySelect country;

    @PropertyId("role")
    private RoleSelect role;

    private NetworkSelect networkSelect;
    private NetworkGroupSelect groups;
    private NetworkGroupSelect exclusiveGroups;

    private GroupWidgetAdapterListing visualizations;

    @Autowired
    public UserForm(User entity, Network network, UserDetailsAdapter currentUser, RoleService roleService,
            NetworkService networkService, NetworkGroupService networkGroupService,
            GroupWidgetService groupWidgetService, UIEventBus eventBus) {
        super(entity, User.class, "user.editor", network, currentUser);
        this.roleService = roleService;
        this.networkService = networkService;
        this.networkGroupService = networkGroupService;
        this.groupWidgetService = groupWidgetService;

        // Wire up event bus for PendingChangesEvent
        setEventPoster(eventBus::post);

        if (isCreateBean()) {
            getEntity().setAccountStatus(AccountStatus.ACTIVE);
        }

        populateFields();

        bindFields();
    }



    /**
     * Called during constructor via getFieldsLayout().
     */
    private void initializeFields() {
        username = new TextField();
        username.setWidthFull();
        username.setRequiredIndicatorVisible(true);
        username.setLabel(getI18nLabel("username"));
        if (!isCreateBean()) {
            username.setReadOnly(true);
        }
 
        accountStatus = new AccountStatusSelect();
        accountStatus.setWidthFull();
        accountStatus.setRequiredIndicatorVisible(true);
        accountStatus.setLabel(getI18nLabel("accountStatus"));

        originalPassword = new PasswordField();
        originalPassword.setWidthFull();
        originalPassword.setRequiredIndicatorVisible(isCreateBean());
        originalPassword.setLabel(getI18nLabel("originalPassword"));

        verifiedPassword = new PasswordField();
        verifiedPassword.setWidthFull();
        verifiedPassword.setRequiredIndicatorVisible(isCreateBean());
        verifiedPassword.setLabel(getI18nLabel("verifiedPassword"));

        email = new TextField();
        email.setWidthFull();
        email.setLabel(getI18nLabel("email"));

        firstName = new TextField();
        firstName.setWidthFull();
        firstName.setRequiredIndicatorVisible(true);
        firstName.setLabel(getI18nLabel("firstName"));

        lastName = new TextField();
        lastName.setWidthFull();
        lastName.setRequiredIndicatorVisible(true);
        lastName.setLabel(getI18nLabel("lastName"));

        company = new TextField();
        company.setWidthFull();
        company.setLabel(getI18nLabel("company"));

        phone = new TextField();
        phone.setWidthFull();
        phone.setLabel(getI18nLabel("phone"));

        street = new TextField();
        street.setWidthFull();
        street.setLabel(getI18nLabel("street"));

        city = new TextField();
        city.setWidthFull();
        city.setLabel(getI18nLabel("city"));

        zip = new TextField();
        zip.setWidthFull();
        zip.setLabel(getI18nLabel("zip"));

        country = new CountrySelect();
        country.setWidthFull();
        country.setLabel(getI18nLabel("country"));

        // Create empty selects - will be populated in initializeServiceDependentFields()
        role = new RoleSelect(new ArrayList<>());
        role.setWidthFull();
        role.setRequiredIndicatorVisible(true);
        role.setLabel(getI18nLabel("role"));
        if (!isCreateBean()) {
            role.setReadOnly(true);
        }

        networkSelect = new NetworkSelect(new ArrayList<>());
        networkSelect.setWidthFull();
        networkSelect.setLabel(getI18nLabel("network"));

        groups = new NetworkGroupSelect(new ArrayList<>(), true);
        groups.setWidthFull();

        exclusiveGroups = new NetworkGroupSelect(new ArrayList<>(), true);
        exclusiveGroups.setWidthFull();

        groups.setVisible(Constants.USE_GROUPS);
        exclusiveGroups.setVisible(Constants.USE_GROUPS);



    }

    /**
     * Initialized fields that require services (roles, networks, groups).
     * 
     */
    private void populateFields() {

 
        // Populate role select based on current user's permissions
        List<Role> roles = new ArrayList<>();
        if (getCurrentUser().hasRole(Constants.ROLE_SUPERVISOR)) {
            //roles = roleService.findAll();

            roles.add(roleService.findByName(Constants.ROLE_ADMINISTRATOR));
            roles.add(roleService.findByName(Constants.ROLE_SUPERUSER));
            roles.add(roleService.findByName(Constants.ROLE_USER));
            roles.add(roleService.findByName(Constants.ROLE_PRODUCTION));
            roles.add(roleService.findByName(Constants.ROLE_FINANCE));


        } else {
            roles.add(roleService.findByName(Constants.ROLE_ADMINISTRATOR));
            roles.add(roleService.findByName(Constants.ROLE_SUPERUSER));
            roles.add(roleService.findByName(Constants.ROLE_USER));
        }
        role.setItems(roles);

        // Populate network select
        networkSelect.setItems(loadNetworks());

        // Load and configure groups
        List<NetworkGroup> availableGroups = loadNetworkGroups();
        groups.setItems(availableGroups);
        exclusiveGroups.setItems(availableGroups);

        // Configure network selection and groups
        configureNetworkSelection();
        preselectGroups(getEntity().getGroups());
        //configureExclusiveGroups(details().hasRole(Constants.ROLE_SUPERUSER));

        
        List<GroupWidget> items = groupWidgetService.findByOwner(getEntity().getOwner());
        Set<NetworkGroup> selectedGroups = getEntity().getGroups();
        if (isCreateBean()) {
            selectedGroups = items.stream().map(GroupWidget::getGroup).filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        if (visualizations!= null) {
            visualizations.addItems(items, selectedGroups);
        }
        
    }

    private void bindFields() {
        String requiredMessage = getTranslation("validators.fieldgroup_errors");
        getBinder().forField(username)
                .asRequired(requiredMessage)
                .bind(User::getUsername, User::setUsername);

        getBinder().forField(accountStatus)
                .asRequired(requiredMessage)
                .bind(User::getAccountStatus, User::setAccountStatus);

        getBinder().forField(email)
                .withValidator(value -> {
                    if (value == null || value.trim().isEmpty()) {
                        return true;
                    }
                    return !new AntiReDoSEmailValidator(getI18nLabel("valid_email"))
                            .apply(value, new ValueContext(email))
                            .isError();
                }, getI18nLabel("valid_email"))
                .bind(User::getEmail, User::setEmail);

        getBinder().forField(firstName)
                .asRequired(requiredMessage)
                .bind(User::getFirstName, User::setFirstName);

        getBinder().forField(lastName)
                .asRequired(requiredMessage)
                .bind(User::getLastName, User::setLastName);

        getBinder().forField(originalPassword)
                .withValidator(value -> !isCreateBean() || (value != null && !value.trim().isEmpty()),
                        requiredMessage)
                .withValidator(value -> {
                    if (!isCreateBean()) {
                        return true;
                    }
                    String verify = verifiedPassword.getValue();
                    return verify != null && !verify.trim().isEmpty();
                }, requiredMessage)
                .bind(user -> "", (user, value) -> {
                    if (value != null && !value.trim().isEmpty()) {
                        user.setPassword(value);
                    }
                });

        getBinder().forField(verifiedPassword)
                .withValidator(value -> {
                    String original = originalPassword.getValue();
                    boolean originalEmpty = original == null || original.trim().isEmpty();
                    boolean valueEmpty = value == null || value.trim().isEmpty();
                    if (originalEmpty && valueEmpty) {
                        return !isCreateBean();
                    }
                    if (valueEmpty) {
                        return false;
                    }
                    return value.equals(original);
                }, requiredMessage)
                .bind(user -> "", (user, value) -> {});

        getBinder().forField(company)
                .bind(User::getCompany, User::setCompany);

        getBinder().forField(phone)
                .bind(User::getPhone, User::setPhone);

        getBinder().forField(street)
                .bind(User::getStreet, User::setStreet);

        getBinder().forField(city)
                .bind(User::getCity, User::setCity);

        getBinder().forField(zip)
                .bind(User::getZip, User::setZip);

        getBinder().forField(country)
                .bind(User::getCountry, User::setCountry);

        getBinder().forField(role)
                .asRequired(requiredMessage)
                .bind(User::getRole, User::setRole);

        originalPassword.addValueChangeListener(event -> getBinder().validate());
        verifiedPassword.addValueChangeListener(event -> getBinder().validate());
        email.addValueChangeListener(event -> getBinder().validate());

        getBinder().validate();
    }


    @Override
    public VerticalLayout getFieldsLayout() {
        // Ensure basic fields are initialized before building the layout
        // This is necessary because getFieldsLayout() is called during super() constructor
        if (username == null) {
            initializeFields();
        }

    boolean supervisor = getCurrentUser().hasRole(Constants.ROLE_SUPERVISOR);
        boolean hasVisualization = !supervisor;

        if (isCreateBean()) {
            accountStatus.setVisible(false);

            if (supervisor) {
                networkSelect.setVisible(false);
            }
        } else {
            hasVisualization = getEntity().hasRole(Constants.ROLE_SUPERUSER)
                    || getEntity().hasRole(Constants.ROLE_USER);

            exclusiveGroups.setVisible(false);

            boolean hasNetwork = getEntity().hasRole(Constants.ROLE_SUPERUSER) || getEntity().hasRole(Constants.ROLE_USER);

            if (!hasNetwork) {
                networkSelect.setVisible(false);
                groups.setVisible(false);
                exclusiveGroups.setVisible(false);
            }

            accountStatus.setReadOnly(getEntity().hasRole(Constants.ROLE_SUPERVISOR));

            if (getEntity().getAccountStatus() != null
                    && !AccountStatus.NEED_ACTIVATION.equals(getEntity().getAccountStatus())) {
                accountStatus.removeItem(AccountStatus.NEED_ACTIVATION);
            }
        }

        VerticalLayout mainLayout = buildMainLayout();
        com.vaadin.flow.component.Component login = buildPanel(buildLoginForm());
        com.vaadin.flow.component.Component user = buildPanel(buildUserForm());
        com.vaadin.flow.component.Component auth = buildPanel(buildAuthForm());


 
        TabSheet tabSheet = new TabSheet();
        tabSheet.addTab(getI18nLabel("login_info"),login);
        tabSheet.addTab(getI18nLabel("user_info"),user);
        tabSheet.addTab(getI18nLabel("auth_info"),auth);
        if (hasVisualization) {
            visualizations = new GroupWidgetAdapterListing();
            tabSheet.addTab(getI18nLabel("visualizations_tab"),visualizations);
        }
        
        mainLayout.add(tabSheet);

 
        
        return mainLayout;
    }

    private FormLayout buildLoginForm() {
        FormLayout layout = createFormLayout();
        layout.add(username);
        layout.add(accountStatus);
        layout.add(originalPassword);
        layout.add(verifiedPassword);
        return layout;
    }

    private FormLayout buildUserForm() {
        FormLayout layout = createFormLayout();
        layout.add(email);
        layout.add(firstName);
        layout.add(lastName);
        layout.add(company);
        layout.add(phone);
        layout.add(street);
        layout.add(city);
        layout.add(zip);
        layout.add(country);
        return layout;
    }

    private FormLayout buildAuthForm() {
        FormLayout layout = createFormLayout();
        layout.add(role);
        layout.add(networkSelect);
        layout.add(groups);
        layout.add(exclusiveGroups);
        return layout;
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        return formLayout;
    }



    private void configureNetworkSelection() {
        Network selectedNetwork = networkSelect.getValue();
        if (selectedNetwork == null) {
            if (getNetwork() != null) {
                networkSelect.setValue(getNetwork());
                selectedNetwork = getNetwork();
            } else if (getEntity().getNetwork() != null) {
                networkSelect.setValue(getEntity().getNetwork());
                selectedNetwork = getEntity().getNetwork();
            }
        }
        groups.setNetworkSelection(networkSelect, getNetwork(), selectedNetwork);
    }

    private void configureExclusiveGroups(boolean allGroups) {
        List<NetworkGroup> source = new ArrayList<>();
        if (allGroups) {
            Network targetNetwork = resolveNetwork();
            if (targetNetwork != null) {
                source.addAll(networkGroupService.findByNetwork(targetNetwork));
            } else {
                source.addAll(loadNetworkGroups());
            }
        } else {
            source.addAll(getEntity().getGroups());
        }
        exclusiveGroups.setExclusiveGroups(source);
    }

    private Network resolveNetwork() {
        if (networkSelect.getValue() != null) {
            return networkSelect.getValue();
        }
        if (getNetwork() != null) {
            return getNetwork();
        }
        return getEntity().getNetwork();
    }

    private List<Network> loadNetworks() {
        List<Network> networks = new ArrayList<>();
        networks.addAll(networkService.findByOwner(getEntity().getOwner()));
        Network defaultNetwork = getNetwork();
        if (defaultNetwork != null && !containsNetwork(networks, defaultNetwork)) {
            networks.add(defaultNetwork);
        }
        Network entityNetwork = getEntity().getNetwork();
        if (entityNetwork != null && !containsNetwork(networks, entityNetwork)) {
            networks.add(entityNetwork);
        }
        return networks;
    }

    private boolean containsNetwork(List<Network> networks, Network target) {
        return networks.stream().anyMatch(net -> net.getId() != null && net.getId().equals(target.getId()));
    }

    private List<NetworkGroup> loadNetworkGroups() {
        Set<NetworkGroup> available = new HashSet<>();
        available.addAll(networkGroupService.findByOwner(getEntity().getOwner()));
        available.addAll(getEntity().getGroups());
        return new ArrayList<>(available);
    }

    private void preselectGroups(Set<NetworkGroup> selectedGroups) {
        if (selectedGroups == null) {
            return;
        }
        selectedGroups.stream().filter(Objects::nonNull).forEach(groups::select);
    }

    @Override
    protected void afterCommit() {
        if (groups.isVisible()) {
            Set<NetworkGroup> selected = new HashSet<>(groups.getSelectedItems());
            getEntity().setGroups(selected);
        }

        if (getEntity().getAccountStatus().equals(AccountStatus.ACTIVE)) {
            getEntity().setExpiryDate(null);
        }

        if (getEntity().hasRole(Constants.ROLE_ADMINISTRATOR)) {
            getEntity().getGroups().clear();
        } else {
            Set<NetworkGroup> currentGroups = new HashSet<>(getEntity().getGroups());
            currentGroups.removeIf(group -> group.getGroupType() != null
                    && NetworkGroupType.GROUP_WIDGET.equals(group.getGroupType()));
            getEntity().setGroups(currentGroups);

            if (visualizations != null) {
                Collection<NetworkGroup> selected = visualizations.getSelectedGroups();
                getEntity().getGroups().addAll(selected);
            }
        }

        if (getEntity().hasRole(Constants.ROLE_ADMINISTRATOR)) {
            getEntity().setOwner(getEntity().getUsername());
        }

        if (getEntity().hasRole(Constants.ROLE_ADMINISTRATOR)) {
            try {
                networkService.createDefaultNetwork(getEntity());
            } catch (BackendServiceException e) {
            }
        }

    }

    @Override
    public String getWindowStyle() {
        return "user-editor";
    }

    @Override
    protected void beforeCommit() throws EditorConstraintException {
        if (isCreateBean() && networkSelect.isVisible()) {
            Role selectedRole = role.getValue();
            boolean requiresNetwork = selectedRole != null
                    && (Constants.ROLE_SUPERUSER.equals(selectedRole.getName())
                            || Constants.ROLE_USER.equals(selectedRole.getName()));
            if (requiresNetwork) {
                Network selectedNetwork = networkSelect.getValue();
                if (selectedNetwork == null) {
                    throw new EditorConstraintException(
                            "Network Not Available. User must be associated to a Network");
                }
            }
        }
    }

    @Override
    public float[] getWindowDimension() {
        return UiConstants.L_DIMENSION;
    }

}
