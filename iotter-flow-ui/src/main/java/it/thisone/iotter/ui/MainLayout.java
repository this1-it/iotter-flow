package it.thisone.iotter.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import it.thisone.iotter.ui.about.AboutView;
import it.thisone.iotter.ui.devices.DevicesView;
import it.thisone.iotter.ui.groupwidgets.GroupWidgetsView;
import it.thisone.iotter.ui.users.UsersView;

/**
 * The main layout. Contains the navigation menu.
 */
@Theme(value=Lumo.class)
//@Theme(themeFolder = "iotter-flow")
//@PWA(name = "Iotter Flow", shortName = "Iotter")
@CssImport("./styles/shared-styles.css")
public class MainLayout extends FlexLayout implements RouterLayout {
    private Menu menu;

    public MainLayout() {
        setSizeFull();
        setClassName("main-layout");

        menu = new Menu();
        menu.addView(AboutView.class, AboutView.VIEW_NAME,
                VaadinIcon.INFO_CIRCLE.create());
        menu.addView(UsersView.class, UsersView.VIEW_NAME,
                VaadinIcon.USERS.create());
        menu.addView(DevicesView.class, DevicesView.VIEW_NAME,
                VaadinIcon.CONNECT.create());
                menu.addView(GroupWidgetsView.class, GroupWidgetsView.VIEW_NAME,
                VaadinIcon.CHART_LINE.create());
        add(menu);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        attachEvent.getUI()
                .addShortcutListener(
                        () -> {
                            VaadinSession.getCurrent().getSession().invalidate();
                            UI.getCurrent().navigate("login");
                        },
                        Key.KEY_L, KeyModifier.CONTROL);

    }
}
