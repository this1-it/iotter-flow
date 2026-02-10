package it.thisone.iotter.ui.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.ui.main.UiConstants;


/**
 * Custom component with utility methods
 * @author tisone
 *
 */
public abstract class BaseComponent extends Composite<VerticalLayout> implements UiConstants {
	private static final long serialVersionUID = -2527235916632043029L;
	private String i18nkey;


	public BaseComponent(String i18nkey) {
		super();
		this.i18nkey = i18nkey;
		
	}
	
	public BaseComponent(String i18nkey, String id) {
		super();
		this.i18nkey = i18nkey;
		setId(id);
		
	}

	public void setRootComposition(Component component) {
        getContent().removeAll();
        getContent().setSizeFull();
        getContent().setPadding(false);
        getContent().setSpacing(false);
        getContent().add(component);
        getContent().setFlexGrow(1, component);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append(i18nkey, this.getId()).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(i18nkey).append(this.getId()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BaseComponent == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final BaseComponent otherObject = (BaseComponent) obj;
		return new EqualsBuilder().append(this.getId(), otherObject.getId()).isEquals();
	}
	
    public String getI18nLabel(String key) {
        return getTranslation(getI18nKey() + "." + key);
    }

	public String getI18nKey() {
		return i18nkey;
	}

	
	/**
	 * create dialog box used in all modal editor
	 * @param label
	 * @param content
	 * @param ratio
	 * @return
	 */
	public static Dialog createDialog(String caption, Component content) {
		SideDrawer drawer = new SideDrawer(caption);
		drawer.setDrawerContent(content);
		return drawer;
	}
	



public static Dialog createDialogWithClose(
        String title,
        Component content,
        String closeLabel
) {
    Dialog dialog = new Dialog();
    dialog.setCloseOnEsc(true);
    dialog.setCloseOnOutsideClick(true);

    // ---------- Header ----------
    H3 headerTitle = new H3(title != null ? title : "");
    headerTitle.getStyle().set("margin", "0");

    Button headerClose = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> dialog.close());
    headerClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

    HorizontalLayout header = new HorizontalLayout(headerTitle, headerClose);
    header.setWidthFull();
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.expand(headerTitle);

    // ---------- Content ----------
    VerticalLayout contentWrapper = new VerticalLayout(content);
    contentWrapper.setPadding(false);
    contentWrapper.setSpacing(true);

    // IMPORTANT: allow natural sizing
    content.getElement().getStyle().remove("width");
    content.getElement().getStyle().remove("height");

    // ---------- Footer ----------
    Button closeButton = new Button(
            closeLabel != null ? closeLabel : "Close",
            e -> dialog.close()
    );

    HorizontalLayout footer = new HorizontalLayout(closeButton);
    footer.setWidthFull();
    footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

    // ---------- Root layout ----------
    VerticalLayout layout = new VerticalLayout(header, contentWrapper, footer);
    layout.setPadding(true);
    layout.setSpacing(true);
    layout.setAlignItems(FlexComponent.Alignment.STRETCH);

    dialog.add(layout);
    return dialog;
}

}
