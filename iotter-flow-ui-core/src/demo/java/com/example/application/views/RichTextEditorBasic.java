package com.example.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.demo.domain.DataService;

public class RichTextEditorBasic extends VerticalLayout {

    public RichTextEditorBasic() {
        RichTextEditor rte = new RichTextEditor();
        add(rte);
    }

}
