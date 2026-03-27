package it.thisone.iotter.ui.anonymous;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public final class AnonymousAuthLayout {

    private AnonymousAuthLayout() {
    }

    public static Component singleColumn(String eyebrowText, String titleText, String subtitleText, Component content) {
        VerticalLayout panel = new VerticalLayout();
        panel.addClassName("login-panel");
        panel.addClassName("login-panel-single");
        panel.setSpacing(false);
        panel.setPadding(false);

        if (eyebrowText != null && !eyebrowText.trim().isEmpty()) {
            Span eyebrow = new Span(eyebrowText);
            eyebrow.addClassName("auth-card-eyebrow");
            panel.add(eyebrow);
        }

        if (titleText != null && !titleText.trim().isEmpty()) {
            H1 title = new H1(titleText);
            title.addClassName("auth-card-title");
            panel.add(title);
        }

        if (subtitleText != null && !subtitleText.trim().isEmpty()) {
            Span subtitle = new Span(subtitleText);
            subtitle.addClassName("auth-card-subtitle");
            panel.add(subtitle);
        }

        Div divider = new Div();
        divider.addClassName("auth-card-divider");
        panel.add(divider, content);

        Div stage = new Div(panel);
        stage.addClassName("auth-stage");
        stage.addClassName("auth-stage-single");
        return stage;
    }
}
