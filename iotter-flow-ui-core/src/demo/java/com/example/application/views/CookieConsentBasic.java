package com.example.application.views;

import org.vaadin.flow.components.cookieconsent.CookieConsent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


public class CookieConsentBasic extends Div {

    public CookieConsentBasic() {
        CookieConsent cookieConsent = new CookieConsent();
        add(cookieConsent);
    }

}