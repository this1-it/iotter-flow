package it.thisone.iotter.ui.gridstack;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

@Tag("gridstack-board")
@NpmPackage(value = "gridstack", version = "7.2.3")
@JsModule("./src/gridstack-board.js")
@CssImport("gridstack/dist/gridstack.min.css")
@CssImport("gridstack/dist/gridstack-extra.min.css")
@CssImport("./styles/gridstack-board.css")
public class GridstackBoard extends Component implements HasSize {

    private static final long serialVersionUID = 1L;
    private final Map<String, Element> widgetElements = new HashMap<>();

    public void addWidget(String widgetId, Component content, int x, int y, int w, int h) {
        Element wrapper = new Element("div");
        wrapper.setAttribute("class", "grid-stack-item");
        wrapper.setAttribute("gs-id", widgetId);
        wrapper.setAttribute("gs-x", String.valueOf(x));
        wrapper.setAttribute("gs-y", String.valueOf(y));
        wrapper.setAttribute("gs-w", String.valueOf(w));
        wrapper.setAttribute("gs-h", String.valueOf(h));

        Element contentDiv = new Element("div");
        contentDiv.setAttribute("class", "grid-stack-item-content");
        contentDiv.appendChild(content.getElement());

        wrapper.appendChild(contentDiv);
        getElement().appendChild(wrapper);
        widgetElements.put(widgetId, wrapper);

        getElement().callJsFunction("makeWidget", widgetId);
    }

    public void removeWidget(String widgetId) {
        Element wrapper = widgetElements.remove(widgetId);
        if (wrapper != null) {
            getElement().callJsFunction("beforeRemoveWidget", widgetId);
            getElement().removeChild(wrapper);
        }
    }

    public void setLayout(String json) {
        if (json != null && !json.isEmpty()) {
            getElement().callJsFunction("loadLayout", json);
        }
    }

    public void setEditable(boolean editable) {
        getElement().callJsFunction("setEditable", editable);
    }

    public Registration addLayoutChangedListener(ComponentEventListener<LayoutChangedEvent> listener) {
        return addListener(LayoutChangedEvent.class, listener);
    }
}
