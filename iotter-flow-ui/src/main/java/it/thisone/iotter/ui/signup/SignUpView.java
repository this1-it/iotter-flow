package it.thisone.iotter.ui.signup;

import com.vaadin.flow.router.Route;

import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.ui.anonymous.AnonymousAuthLayout;
import it.thisone.iotter.ui.common.BaseView;

@Route(SignUpView.NAME)
public class SignUpView extends BaseView {
    public static final String NAME = "signup";

    public SignUpView(UserService userService, DeviceService deviceService) {
        addClassName("signup-view");
        addClassName("anonymous-auth-view");
        buildLayout(userService, deviceService);
    }

    @Override
    public String getI18nKey() {
        return "register";
    }

    @Override
    public String getPageTitle() {
        return getI18nLabel("title");
    }

    private void buildLayout(UserService userService, DeviceService deviceService) {
        add(AnonymousAuthLayout.singleColumn("Iotter", getI18nLabel("title"),
                "Create your account and link it to an active plant.", new SignUpWizard(userService, deviceService)));
    }
}
