package it.thisone.iotter.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.PWA;

import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import it.thisone.iotter.ui.about.AboutView;
import it.thisone.iotter.ui.authentication.AccessControlFactory;

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

        add(menu);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        attachEvent.getUI()
                .addShortcutListener(
                        () -> AccessControlFactory.getInstance()
                                .createAccessControl().signOut(),
                        Key.KEY_L, KeyModifier.CONTROL);

    }
}
