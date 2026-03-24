package it.thisone.iotter.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.ui.about.AboutView;
import it.thisone.iotter.ui.common.AuthenticatedUser;
import it.thisone.iotter.ui.deviceconfigurations.DeviceConfigurationsView;
import it.thisone.iotter.ui.devices.DevicesView;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetsView;
import it.thisone.iotter.ui.networks.NetworksView;
import it.thisone.iotter.ui.tracing.TracingView;
import it.thisone.iotter.ui.users.UsersView;

/**
 * The main layout. Contains the navigation menu.
 */
@CssImport("./styles/shared-styles.css")
public class MainLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {
        private final AuthenticatedUser authenticatedUser;
        private final H2 viewTitle;
        private final Span userEmail;
        private final Span userRole;
        private final Avatar userAvatar;
        private Component currentView;

        public MainLayout(AuthenticatedUser authenticatedUser) {
                this.authenticatedUser = authenticatedUser;
                setPrimarySection(Section.DRAWER);
                addClassName("main-layout");

                DrawerToggle drawerToggle = new DrawerToggle();
                drawerToggle.addClassName("view-toggle");

                viewTitle = new H2();
                viewTitle.addClassName("view-title");

                Div spacer = new Div();
                spacer.addClassName("navbar-spacer");

                userEmail = new Span();
                userEmail.addClassName("user-name");
                userRole = new Span();
                userAvatar = new Avatar();

                Div userInfo = new Div(userEmail, userRole);
                userInfo.addClassName("user-info");

                Button searchButton = createNavbarButton(VaadinIcon.SEARCH);
                Button notificationsButton = createNavbarButton(VaadinIcon.BELL);
                MenuBar accountMenu = createAccountMenu();

                HorizontalLayout navbarRight = new HorizontalLayout(searchButton, notificationsButton, userInfo, userAvatar,
                                accountMenu);
                navbarRight.addClassName("navbar-right");
                navbarRight.setSpacing(false);
                navbarRight.setPadding(false);
                navbarRight.setDefaultVerticalComponentAlignment(Alignment.CENTER);

                HorizontalLayout navbar = new HorizontalLayout(drawerToggle, viewTitle, spacer, navbarRight);
                navbar.addClassName("view-header");
                navbar.setWidthFull();
                navbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);
                navbar.expand(spacer);
                addToNavbar(navbar);

                Header drawerHeader = new Header(VaadinIcon.CLUSTER.create(), new Span(getTranslation("menu.title")));
                drawerHeader.addClassName("app-name");

                SideNav nav = new SideNav();
                nav.addItem(createNavItem(AboutView.VIEW_NAME, AboutView.class, VaadinIcon.INFO_CIRCLE));
                nav.addItem(createNavItem(UsersView.VIEW_NAME, UsersView.class, VaadinIcon.USERS));
                nav.addItem(createNavItem(DevicesView.VIEW_NAME, DevicesView.class, VaadinIcon.CONNECT));
                nav.addItem(createNavItem(GroupWidgetsView.VIEW_NAME, GroupWidgetsView.class, VaadinIcon.CHART_LINE));
                nav.addItem(createNavItem(NetworksView.VIEW_NAME, NetworksView.class, VaadinIcon.FILE_TREE_SUB));
                nav.addItem(createNavItem(DeviceConfigurationsView.VIEW_NAME, DeviceConfigurationsView.class, VaadinIcon.COGS));
                nav.addItem(createNavItem(TracingView.VIEW_NAME, TracingView.class, VaadinIcon.ARCHIVES));

                Scroller drawerScroller = new Scroller(nav);
                drawerScroller.addClassNames("drawer-section", "app-nav");
                addToDrawer(drawerHeader, drawerScroller);
                updateUserInfo(authenticatedUser.get().orElse(null));
        }

        @Override
        public void beforeEnter(BeforeEnterEvent event) {
                if (authenticatedUser.isLoggedIn()) {
                        return;
                }

                String target = event.getLocation().getPathWithQueryParameters();
                VaadinSession.getCurrent().setAttribute("POST_LOGIN_ROUTE", target);
                event.forwardTo("login");
        }

        @Override
        protected void onAttach(AttachEvent attachEvent) {
                super.onAttach(attachEvent);

                attachEvent.getUI().getPage().executeJs(
                        "return Intl.DateTimeFormat().resolvedOptions().timeZone;"
                ).then(String.class, tz -> {
                        VaadinSession.getCurrent().setAttribute("browserTZ", tz);
                });

                attachEvent.getUI()
                                .addShortcutListener(
                                                this::logout,
                                                Key.KEY_L, KeyModifier.CONTROL);

        }

        @Override
        public void afterNavigation(AfterNavigationEvent event) {
                viewTitle.setText(resolvePageTitle());
        }

        @Override
        public void showRouterLayoutContent(HasElement content) {
                currentView = content instanceof Component ? (Component) content : null;
                super.showRouterLayoutContent(content);
        }

        private void updateUserInfo(UserDetailsAdapter user) {
                if (user == null) {
                        userEmail.setText("guest");
                        userRole.setText("Unauthenticated");
                        userAvatar.setName("Guest");
                        return;
                }
                String displayName = hasText(user.getName()) ? user.getName() : user.getUsername();
                String displayEmail = hasText(user.getEmail()) ? user.getEmail() : user.getUsername();
                String displayRole = user.getRoles() != null && !user.getRoles().isEmpty()
                                ? user.getRoles().iterator().next().replace("ROLE_", "").replace('_', ' ')
                                : "User";
                userEmail.setText(displayEmail);
                userRole.setText(displayRole);
                userAvatar.setName(displayName);
        }

        private String resolvePageTitle() {
                PageTitle pageTitle = currentView != null ? currentView.getClass().getAnnotation(PageTitle.class) : null;
                String title = pageTitle != null ? pageTitle.value() : null;
                return hasText(title) ? title : "Dashboard";
        }

        private SideNavItem createNavItem(String label, Class<? extends Component> viewClass, VaadinIcon icon) {
                return new SideNavItem(label, viewClass, icon.create());
        }

        private Button createNavbarButton(VaadinIcon icon) {
                Button button = new Button(icon.create());
                button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                return button;
        }

        private MenuBar createAccountMenu() {
                MenuBar accountMenu = new MenuBar();
                accountMenu.addClassName("account-menu");
                accountMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

                MenuItem rootItem = accountMenu.addItem(VaadinIcon.ELLIPSIS_DOTS_V.create());
                rootItem.getSubMenu().addItem("Account", event -> {
                });
                rootItem.getSubMenu().addItem(getTranslation("menu.logout"), event -> logout());
                return accountMenu;
        }

        private void logout() {
                VaadinSession.getCurrent().getSession().invalidate();
                UI.getCurrent().navigate("login");
        }

        private boolean hasText(String value) {
                return value != null && !value.trim().isEmpty();
        }
}
