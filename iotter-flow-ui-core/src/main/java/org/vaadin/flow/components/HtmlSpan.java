package org.vaadin.flow.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

/**
 * Populates its contents with given html snippet. The advantage over {@link com.vaadin.flow.component.Html}
 * is that any html is accepted - it doesn't have to be wrapped in a single root element.
 * <p></p>
 * Note that it is the developer's responsibility to sanitize and remove any
 * dangerous parts of the HTML before sending it to the user through this
 * component. Passing raw input data to the user will possibly lead to
 * cross-site scripting attacks.
 * <p></p>
 * This component does not expand the HTML fragment into a server side DOM tree,
 * so you cannot traverse or modify the HTML on the server. The root element can
 * be accessed through {@link #getElement()} and the inner HTML through
 * {@link #getInnerHtml()}.
 */
public class HtmlSpan extends Span {

    public HtmlSpan() {
    }

    public HtmlSpan( String innerHtml) {
        this();
        setInnerHtml(innerHtml);
    }

    /**
     * Sets the inner html. Removes any children added via {@link #add(Component...)}.
     * @param innerHtml the inner HTML to set; if null, the entire content is removed.
     */
    public void setInnerHtml( String innerHtml) {
        removeAll();
        getElement().setProperty("innerHTML", innerHtml);
    }

    
    public String getInnerHtml() {
        return getElement().getProperty("innerHTML", "");
    }
}
