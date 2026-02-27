package it.thisone.iotter.ui.networkgroups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.NetworkGroupType;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.ifc.INetworkGroupUsers;
import it.thisone.iotter.ui.validators.AntiReDoSEmailValidator;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.PopupNotification;

public class NetworkGroupUsers extends BaseComponent implements INetworkGroupUsers {

	private static final long serialVersionUID = 4271498822795054411L;

	private Grid<User> leftGrid;
	private Grid<User> rightGrid;
	private Grid<User> adminGrid;

	private ListDataProvider<User> leftDataProvider;
	private ListDataProvider<User> rightDataProvider;
	private ListDataProvider<User> adminDataProvider;

	private NetworkGroup group;
	private final UserService userService;

	private boolean exports;
	private boolean visualization;
	private boolean alarms;

	public NetworkGroupUsers(NetworkGroup entity, String[] visibleColumns, UserService userService) {
		super("networkgroup.bindings", "networkgroup.users");
		group = entity;
		this.userService = userService;

		exports = group.getGroupType().equals(NetworkGroupType.EXPORTS);
		visualization = group.getGroupType().equals(NetworkGroupType.GROUP_WIDGET);
		alarms = group.getGroupType().equals(NetworkGroupType.ALARMS);

		HorizontalLayout content = new HorizontalLayout();
		content.setPadding(true);
		content.setSizeFull();

		List<User> rightItems = new ArrayList<>();
		List<User> leftItems = new ArrayList<>();
		List<User> adminItems = new ArrayList<>();
		rightDataProvider = new ListDataProvider<>(rightItems);
		leftDataProvider = new ListDataProvider<>(leftItems);
		adminDataProvider = new ListDataProvider<>(adminItems);

		VerticalLayout leftLayout = new VerticalLayout();
		leftLayout.setDefaultHorizontalComponentAlignment(Alignment.START);
		leftLayout.setSizeFull();

		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.setDefaultHorizontalComponentAlignment(Alignment.START);
		rightLayout.setSizeFull();

		VerticalLayout centerLayout = new VerticalLayout();
		centerLayout.setSizeFull();
		centerLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		centerLayout.addClassName(UIUtils.BUTTONS_STYLE);

		final Button addButton = new Button();
		addButton.setIcon(VaadinIcon.USER.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		addButton.setVisible(alarms || exports);
		centerLayout.add(addButton);
		centerLayout.setHorizontalComponentAlignment(Alignment.CENTER, addButton);

		final Button addAllButton = new Button();
		addAllButton.setIcon(VaadinIcon.ANGLE_DOUBLE_RIGHT.create());
		addAllButton.addThemeVariants(ButtonVariant.LUMO_ICON);

		final Button removeAllButton = new Button();
		removeAllButton.setIcon(VaadinIcon.ANGLE_DOUBLE_LEFT.create());
		removeAllButton.addThemeVariants(ButtonVariant.LUMO_ICON);

		final Button moveRightButton = new Button();
		moveRightButton.setIcon(VaadinIcon.ANGLE_RIGHT.create());
		moveRightButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		moveRightButton.setEnabled(false);

		final Button moveLeftButton = new Button();
		moveLeftButton.setIcon(VaadinIcon.ANGLE_LEFT.create());
		moveLeftButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		moveLeftButton.setEnabled(false);
		moveLeftButton.setVisible(!group.isDefaultGroup());

		VerticalLayout buttonsLayout = new VerticalLayout();
		buttonsLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		buttonsLayout.addClassName(UIUtils.BUTTONS_STYLE);
		buttonsLayout.add(addAllButton, moveRightButton, moveLeftButton, removeAllButton);

		centerLayout.add(buttonsLayout);
		centerLayout.setFlexGrow(1f, buttonsLayout);

		content.add(leftLayout, centerLayout, rightLayout);
		content.setVerticalComponentAlignment(Alignment.START, leftLayout, centerLayout, rightLayout);

		content.setFlexGrow(0.5f, leftLayout);
		content.setFlexGrow(0.05f, centerLayout);
		content.setFlexGrow(0.5f, rightLayout);

		List<User> members = userService.findByGroup(group);
		List<User> allUsers = new ArrayList<>();
		List<User> users = new ArrayList<>();
		List<User> admins = new ArrayList<>();
		String caption = visualization ? getI18nLabel("all_users")
				: String.format("%s: %s", getI18nLabel("network_users"), group.getNetwork().getName());

		if (visualization) {
			allUsers = userService.findByOwner(group.getOwner());
		} else {
			if (group.isDefaultGroup()) {
				allUsers = userService.findByNetwork(group.getNetwork());
			} else {
				NetworkGroup defaultGroup = group.getNetwork().getDefaultGroup();
				allUsers = userService.findByGroup(defaultGroup);
			}
		}

		User owner = userService.findByName(group.getOwner());
		allUsers.remove(owner);

		if (alarms || exports) {
			for (User user : allUsers) {
				if (user.getAccountStatus().equals(AccountStatus.ACTIVE)) {
					if (details(user).hasRole(Constants.ROLE_USER)) {
						if (!members.contains(user)) {
							users.add(user);
						}
					} else {
						admins.add(user);
					}
				}
			}

			if (!admins.contains(owner)) {
				admins.add(owner);
			}
		} else {
			users = allUsers.stream()
					.filter(o -> o.getAccountStatus().equals(AccountStatus.ACTIVE))
					.sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toList());
		}

		for (User user : members) {
			if (users.contains(user)) {
				users.remove(user);
			}
		}

		leftItems.addAll(users);
		leftGrid = createGrid(leftDataProvider, visibleColumns);
		Span leftTitle = new Span(caption);

		TextField filter = createFilterTextField();
		leftLayout.add(leftTitle, filter, leftGrid);
		leftLayout.setFlexGrow(1f, leftGrid);
		leftLayout.setSpacing(true);

		rightItems.addAll(members);
		rightGrid = createGrid(rightDataProvider, visibleColumns);
		Span rightTitle = new Span(getI18nLabel("users"));

		adminItems.addAll(admins);
		adminGrid = createGrid(adminDataProvider, visibleColumns);
		Span adminTitle = new Span(getI18nLabel("admins"));

		if (!admins.isEmpty()) {
			rightLayout.add(rightTitle, rightGrid, adminTitle, adminGrid);
			rightLayout.setFlexGrow(0.7f, rightGrid);
			rightLayout.setFlexGrow(0.3f, adminGrid);
			rightLayout.setSpacing(true);
		} else {
			rightLayout.add(rightTitle, rightGrid);
			rightLayout.setFlexGrow(1f, rightGrid);
		}

		setRootComposition(content);

		leftGrid.addSelectionListener(event -> {
			boolean enabled = !event.getAllSelectedItems().isEmpty();
			moveRightButton.setEnabled(enabled);
		});

		rightGrid.addSelectionListener(event -> {
			boolean enabled = !event.getAllSelectedItems().isEmpty();
			moveLeftButton.setEnabled(enabled);
		});

		moveLeftButton.addClickListener(event -> removeUserFromGroup());
		moveRightButton.addClickListener(event -> addUserToGroup());
		addAllButton.addClickListener(event -> addAllToGroup());
		removeAllButton.addClickListener(event -> removeAllFromGroup());
		addButton.addClickListener(event -> addUserByEmail());
	}

	private Grid<User> createGrid(ListDataProvider<User> dataProvider, String[] visibleColumns) {
		Grid<User> grid = new Grid<>();
		grid.setDataProvider(dataProvider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		
		grid.setColumnReorderingAllowed(true);

		for (String column : visibleColumns) {
			grid.addColumn(user -> resolveValue(user, column)).setKey(column).setHeader(getI18nLabel(column));
		}
		return grid;
	}

	private String resolveValue(User user, String column) {
		switch (column) {
		case "username":
			return user.getUsername();
		case "email":
			return user.getEmail();
		case "firstName":
			return user.getFirstName();
		case "lastName":
			return user.getLastName();
		case "network":
			Network network = user.getNetwork();
			return network != null ? network.getName() : "";
		case "role":
			Role role = user.getRole();
			return role != null ? role.getName() : "";
		case "displayName":
			return user.getDisplayName();
		default:
			return "";
		}
	}

	private TextField createFilterTextField() {
		TextField tf = new TextField();
		tf.setPlaceholder(getI18nLabel("search_users_hint"));
		tf.setWidthFull();
		tf.setValueChangeMode(ValueChangeMode.LAZY);
		tf.addValueChangeListener(event -> applyFilter(event.getValue()));
		return tf;
	}

	private void applyFilter(String text) {
		leftDataProvider.clearFilters();
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		String needle = text.toLowerCase();
		leftDataProvider.setFilter(user -> matches(user, needle));
	}

	private boolean matches(User user, String needle) {
		return containsIgnoreCase(user.getUsername(), needle)
				|| containsIgnoreCase(user.getEmail(), needle)
				|| containsIgnoreCase(user.getLastName(), needle);
	}

	private boolean containsIgnoreCase(String value, String needle) {
		return value != null && value.toLowerCase().contains(needle);
	}

	private void addUserByEmail() {
		TextField email = new TextField(getI18nLabel("email"));
		email.setWidthFull();
		email.setRequiredIndicatorVisible(true);

		Button save = new Button(getTranslation("basic.editor.confirm"));
		
		Button cancel = new Button(getTranslation("basic.editor.cancel"));
		

		HorizontalLayout buttons = new HorizontalLayout(save, cancel);
		buttons.setSpacing(true);
		buttons.setAlignItems(Alignment.CENTER);

		FormLayout form = new FormLayout(email);
		form.setSizeFull();

		VerticalLayout layout = new VerticalLayout(form, buttons);
		layout.setSpacing(true);
		layout.setPadding(true);

		Dialog dialog = new Dialog();
		dialog.setResizable(false);
		dialog.setDraggable(false);
		dialog.setCloseOnEsc(true);
		dialog.setCloseOnOutsideClick(false);
		dialog.add(layout);

		save.addClickListener(event -> {
			String value = email.getValue();
			if (value == null || value.trim().isEmpty()) {
				PopupNotification.show(getTranslation("validators.fieldgroup_errors"));
				return;
			}
			AntiReDoSEmailValidator validator = new AntiReDoSEmailValidator(getTranslation("user.editor.valid_email"));
			if (validator.apply(value, new ValueContext(email)).isError()) {
				PopupNotification.show(getTranslation("user.editor.valid_email"));
				return;
			}
			User user = lookUpUserByEmail(email.getValue());
			if (user != null) {
				leftDataProvider.getItems().remove(user);
				rightDataProvider.getItems().add(user);
				leftDataProvider.refreshAll();
				rightDataProvider.refreshAll();
			}
			dialog.close();
		});

		cancel.addClickListener(event -> dialog.close());
		dialog.open();
	}

	private void removeAllFromGroup() {
		Callback callback = new Callback() {
			@Override
			public void onDialogResult(boolean result) {
				if (!result) {
					return;
				}
				List<User> enabled = new ArrayList<>(rightDataProvider.getItems());
				for (User user : enabled) {
					if (user.getAccountStatus().equals(AccountStatus.HIDDEN)) {
						userService.deleteById(user.getId());
						rightDataProvider.getItems().remove(user);
					} else {
						boolean done = userService.removeUserFromGroup(user, group);
						if (done) {
							rightDataProvider.getItems().remove(user);
							leftDataProvider.getItems().add(user);
						}
					}
				}
				rightDataProvider.refreshAll();
				leftDataProvider.refreshAll();
			}
		};
		String caption = getTranslation("basic.editor.are_you_sure");
		String message = getI18nLabel("remove_all_members");
		Dialog dialog = new ConfirmationDialog(caption, message, callback);
		dialog.open();
	}

	private void addAllToGroup() {
		Callback callback = new Callback() {
			@Override
			public void onDialogResult(boolean result) {
				if (!result) {
					return;
				}
				List<User> users = new ArrayList<>(leftDataProvider.getItems());
				List<User> enabled = new ArrayList<>(rightDataProvider.getItems());
				List<User> admins = new ArrayList<>(adminDataProvider.getItems());

				for (User user : users) {
					if (!enabled.contains(user) && !admins.contains(user)) {
						boolean done = userService.addUserToGroup(user, group);
						if (done) {
							leftDataProvider.getItems().remove(user);
							rightDataProvider.getItems().add(user);
						}
					}
				}
				rightDataProvider.refreshAll();
				leftDataProvider.refreshAll();
			}
		};
		String caption = getTranslation("basic.editor.are_you_sure");
		String message = getI18nLabel("add_all_users");
		Dialog dialog = new ConfirmationDialog(caption, message, callback);
		dialog.open();
	}

	private void addUserToGroup() {
		User user = leftGrid.getSelectedItems().stream().findFirst().orElse(null);
		if (user == null) {
			return;
		}

		if (rightDataProvider.getItems().contains(user)) {
			PopupNotification.show(getI18nLabel("user_already_belong_to_group"));
			return;
		}

		if (group.isNew()) {
			leftDataProvider.getItems().remove(user);
			rightDataProvider.getItems().add(user);
			rightDataProvider.refreshAll();
			leftDataProvider.refreshAll();
			return;
		}

		boolean result = userService.addUserToGroup(user, group);

		if (result) {
			PopupNotification.show(getI18nLabel("user_has_been_added_to_group"));
			leftDataProvider.getItems().remove(user);
			rightDataProvider.getItems().add(user);
			rightDataProvider.refreshAll();
			leftDataProvider.refreshAll();
		} else {
			PopupNotification.show("Cannot add user, see logs.", PopupNotification.Type.ERROR);
		}
	}

	private void removeUserFromGroup() {
		User user = rightGrid.getSelectedItems().stream().findFirst().orElse(null);
		if (user == null) {
			return;
		}

		if (group.isNew()) {
			rightDataProvider.getItems().remove(user);
			leftDataProvider.getItems().add(user);
			leftDataProvider.refreshAll();
			rightDataProvider.refreshAll();
			return;
		}

		String caption = getI18nLabel("remove_user_warning");
		Span message = new Span(String.format("%s (%s)", user.getUsername(), user.getEmail()));
		Dialog dialog = new ConfirmationDialog(caption, message, new Callback() {
			@Override
			public void onDialogResult(boolean result) {
				if (!result) {
					return;
				}
				if (user.getAccountStatus().equals(AccountStatus.HIDDEN)) {
					rightDataProvider.getItems().remove(user);
					userService.deleteById(user.getId());
					PopupNotification.show(getI18nLabel("user_has_been_deleted"));
				} else {
					boolean resultRemove = userService.removeUserFromGroup(user, group);
					if (resultRemove) {
						rightDataProvider.getItems().remove(user);
						leftDataProvider.getItems().add(user);
						rightDataProvider.refreshAll();
						leftDataProvider.refreshAll();
						PopupNotification.show(getI18nLabel("user_has_been_removed_from_group"));
					} else {
						PopupNotification.show("Cannot remove user, see logs.",
								PopupNotification.Type.ERROR);
					}
				}
			}
		});
		dialog.open();
	}

	private UserDetailsAdapter details(User user) {
		return new UserDetailsAdapter(user);
	}

	@Override
	public List<User> getUsers() {
		return Collections.unmodifiableList(new ArrayList<>(rightDataProvider.getItems()));
	}

	public User lookUpUserByEmail(String email) {
		User user = null;
		List<User> users = new ArrayList<>(leftDataProvider.getItems());
		Optional<User> match = users.stream().filter(o -> email.equals(o.getEmail())).findFirst();
		if (match.isPresent()) {
			user = match.get();
			userService.addUserToGroup(user, group);
			return user;
		}

		users = new ArrayList<>(rightDataProvider.getItems());
		match = users.stream().filter(o -> email.equals(o.getEmail())).findFirst();
		if (match.isPresent()) {
			return null;
		}
		users = new ArrayList<>(adminDataProvider.getItems());
		match = users.stream().filter(o -> email.equals(o.getEmail())).findFirst();
		if (match.isPresent()) {
			return null;
		}

		String username = String.format("%s-%s", email, group.getId());
		String password = EncryptUtils.digest(username);

		user = userService.safeCreateUser(username, password, email,
				AccountStatus.HIDDEN, "", "", null, group, group.getOwner());
		return user;
	}
}
